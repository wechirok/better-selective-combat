package dev.wechirok.betterselectivecombat.config;

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

public final class ConfigRepository {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;

    public ConfigRepository(Path file) {
        this.file = file;
    }

    public LoadResult load() {
        if (Files.notExists(file)) {
            Set<String> empty = Set.of();
            return save(empty) ? new LoadResult(true, empty) : new LoadResult(false, empty);
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StoredConfig config = GSON.fromJson(reader, StoredConfig.class);
            if (config == null || config.disabled_weapons == null) {
                return new LoadResult(false, Set.of());
            }
            LinkedHashSet<String> normalized = new LinkedHashSet<>();
            for (String value : config.disabled_weapons) {
                String id = WeaponId.normalize(value);
                if (!WeaponId.isValid(id)) {
                    return new LoadResult(false, Set.of());
                }
                normalized.add(id);
            }
            return new LoadResult(true, Set.copyOf(normalized));
        } catch (IOException | JsonParseException exception) {
            return new LoadResult(false, Set.of());
        }
    }

    public boolean save(Collection<String> disabledWeapons) {
        Path parent = file.getParent();
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(parent);
            List<String> sorted = new ArrayList<>(disabledWeapons);
            sorted.sort(String::compareTo);
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(new StoredConfig(sorted), writer);
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

    public record LoadResult(boolean successful, Set<String> disabledWeapons) {
    }

    private static final class StoredConfig {
        private List<String> disabled_weapons;

        private StoredConfig(List<String> disabledWeapons) {
            this.disabled_weapons = disabledWeapons;
        }
    }
}
