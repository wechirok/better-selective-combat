package dev.wechirok.betterselectivecombat.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.wechirok.betterselectivecombat.selection.WeaponId;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ClientCombatConfigRepository {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;

    public ClientCombatConfigRepository(Path file) {
        this.file = file;
    }

    public LoadResult load() {
        if (Files.notExists(file)) {
            ClientConfig defaults = new ClientConfig(true, List.of());
            return save(defaults.enabled, defaults.ignored_weapons)
                    ? new LoadResult(true, defaults.enabled, Set.of())
                    : new LoadResult(false, true, Set.of());
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            ClientConfig config = GSON.fromJson(reader, ClientConfig.class);
            if (config == null || config.ignored_weapons == null) {
                return new LoadResult(false, true, Set.of());
            }
            LinkedHashSet<String> normalized = new LinkedHashSet<>();
            for (String value : config.ignored_weapons) {
                String id = WeaponId.normalize(value);
                if (!WeaponId.isValid(id)) {
                    return new LoadResult(false, true, Set.of());
                }
                normalized.add(id);
            }
            return new LoadResult(true, config.enabled, Set.copyOf(normalized));
        } catch (IOException | JsonParseException exception) {
            return new LoadResult(false, true, Set.of());
        }
    }

    public boolean save(boolean enabled, Collection<String> ignoredWeapons) {
        Path parent = file.getParent();
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(parent);
            List<String> sorted = new ArrayList<>(ignoredWeapons);
            sorted.sort(String::compareTo);
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(new ClientConfig(enabled, sorted), writer);
            }
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException exception) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
            }
            return false;
        }
    }

    public record LoadResult(boolean successful, boolean enabled, Set<String> ignoredWeapons) {
    }

    private static final class ClientConfig {
        private boolean enabled;
        private List<String> ignored_weapons;

        private ClientConfig(boolean enabled, List<String> ignoredWeapons) {
            this.enabled = enabled;
            this.ignored_weapons = ignoredWeapons;
        }
    }
}
