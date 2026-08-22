package dev.wechirok.betterselectivecombat;

import dev.wechirok.betterselectivecombat.config.ConfigRepository;
import dev.wechirok.betterselectivecombat.lang.Translations;
import dev.wechirok.betterselectivecombat.selection.WeaponSelectionService;

import java.nio.file.Path;
import java.util.Objects;

public final class BetterSelectiveCombat {
    public static final String MOD_ID = "better_selective_combat";
    public static final String NAME = "Better Selective Combat";
    public static final String VERSION = "1.0.0";

    private static WeaponSelectionService selections;
    private static Translations translations;

    private BetterSelectiveCombat() {
    }

    public static synchronized void initialize(Path configDirectory) {
        if (selections != null) {
            return;
        }
        Path directory = Objects.requireNonNull(configDirectory).resolve("better-selective-combat");
        translations = new Translations();
        selections = new WeaponSelectionService(new ConfigRepository(directory.resolve("disabled-weapons.json")));
        selections.initialize();
    }

    public static WeaponSelectionService selections() {
        return Objects.requireNonNull(selections, "Better Selective Combat is not initialized");
    }

    public static Translations translations() {
        return Objects.requireNonNull(translations, "Better Selective Combat is not initialized");
    }
}
