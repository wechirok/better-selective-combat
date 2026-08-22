package dev.wechirok.betterselectivecombat.registry;

import dev.wechirok.betterselectivecombat.BetterSelectiveCombat;
import dev.wechirok.betterselectivecombat.mixin.WeaponRegistryInvoker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class BetterCombatRegistryBridge {
    private static final Object LOCK = new Object();
    private static final Map<String, RegistryEntry> registrationBackup = new HashMap<>();
    private static final Map<String, RegistryEntry> containerBackup = new HashMap<>();

    private static Map<Object, Object> registrations;
    private static Map<Object, Object> containers;

    private BetterCombatRegistryBridge() {
    }

    public static void captureAndFilter(Map<Object, Object> currentRegistrations, Map<Object, Object> currentContainers) {
        synchronized (LOCK) {
            if (registrations != currentRegistrations) {
                registrations = currentRegistrations;
                registrationBackup.clear();
            }
            if (containers != currentContainers) {
                containers = currentContainers;
                containerBackup.clear();
            }
            apply(BetterSelectiveCombat.selections().snapshot());
        }
    }

    public static boolean refresh() {
        synchronized (LOCK) {
            if (registrations == null || containers == null) {
                return false;
            }
            apply(BetterSelectiveCombat.selections().snapshot());
            WeaponRegistryInvoker.betterSelectiveCombat$encodeRegistry();
            return true;
        }
    }

    private static void apply(Set<String> disabledWeapons) {
        restoreEnabled(registrations, registrationBackup, disabledWeapons);
        restoreEnabled(containers, containerBackup, disabledWeapons);
        suppressDisabled(registrations, registrationBackup, disabledWeapons);
        suppressDisabled(containers, containerBackup, disabledWeapons);
    }

    private static void restoreEnabled(Map<Object, Object> target, Map<String, RegistryEntry> backup, Set<String> disabledWeapons) {
        backup.entrySet().removeIf(entry -> {
            if (disabledWeapons.contains(entry.getKey())) {
                return false;
            }
            RegistryEntry value = entry.getValue();
            target.putIfAbsent(value.key(), value.value());
            return true;
        });
    }

    private static void suppressDisabled(Map<Object, Object> target, Map<String, RegistryEntry> backup, Set<String> disabledWeapons) {
        target.entrySet().removeIf(entry -> {
            String id = entry.getKey().toString();
            if (!disabledWeapons.contains(id)) {
                return false;
            }
            backup.put(id, new RegistryEntry(entry.getKey(), entry.getValue()));
            return true;
        });
    }

    private record RegistryEntry(Object key, Object value) {
    }
}
