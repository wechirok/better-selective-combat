package dev.wechirok.betterselectivecombat.config;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class ConfigPaths {
    private static final String DIRECTORY = "betterselectivecombat";
    private static final String LEGACY_DIRECTORY = "better-selective-combat";

    private ConfigPaths() {
    }

    public static Path serverFile(Path configDirectory) {
        Path root = Objects.requireNonNull(configDirectory);
        return migrate(
                root.resolve(LEGACY_DIRECTORY).resolve("disabled-weapons.json"),
                root.resolve(DIRECTORY).resolve("server.json")
        );
    }

    public static Path clientFile(Path configDirectory) {
        Path root = Objects.requireNonNull(configDirectory);
        return migrate(
                root.resolve(LEGACY_DIRECTORY).resolve("client.json"),
                root.resolve(DIRECTORY).resolve("client.json")
        );
    }

    private static Path migrate(Path legacyFile, Path currentFile) {
        if (Files.exists(currentFile) || Files.notExists(legacyFile)) {
            return currentFile;
        }
        try {
            Files.createDirectories(currentFile.getParent());
            try {
                Files.move(legacyFile, currentFile, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(legacyFile, currentFile);
            }
            removeLegacyDirectory(legacyFile.getParent());
            return currentFile;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot migrate Better Selective Combat configuration", exception);
        }
    }

    private static void removeLegacyDirectory(Path directory) throws IOException {
        try {
            Files.deleteIfExists(directory);
        } catch (DirectoryNotEmptyException ignored) {
        }
    }
}
