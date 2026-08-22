package dev.wechirok.betterselectivecombat.selection;

import dev.wechirok.betterselectivecombat.config.ConfigRepository;
import dev.wechirok.betterselectivecombat.registry.BetterCombatRegistryBridge;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class WeaponSelectionService {
    private final ConfigRepository repository;
    private volatile Set<String> disabledWeapons = Set.of();

    public WeaponSelectionService(ConfigRepository repository) {
        this.repository = repository;
    }

    public synchronized void initialize() {
        ConfigRepository.LoadResult result = repository.load();
        if (!result.successful()) {
            throw new IllegalStateException("Cannot load Better Selective Combat configuration");
        }
        disabledWeapons = result.disabledWeapons();
    }

    public Set<String> snapshot() {
        return disabledWeapons;
    }

    public List<String> sortedSnapshot() {
        return disabledWeapons.stream().sorted().toList();
    }

    public boolean isDisabled(String weaponId) {
        return disabledWeapons.contains(WeaponId.normalize(weaponId));
    }

    public synchronized ChangeResult disable(String weaponId) {
        String normalized = WeaponId.normalize(weaponId);
        if (!WeaponId.isValid(normalized)) {
            return ChangeResult.INVALID_ID;
        }
        if (disabledWeapons.contains(normalized)) {
            return ChangeResult.UNCHANGED;
        }
        LinkedHashSet<String> changed = new LinkedHashSet<>(disabledWeapons);
        changed.add(normalized);
        if (!repository.save(changed)) {
            return ChangeResult.WRITE_FAILED;
        }
        disabledWeapons = Set.copyOf(changed);
        BetterCombatRegistryBridge.refresh();
        return ChangeResult.CHANGED;
    }

    public synchronized ChangeResult enable(String weaponId) {
        String normalized = WeaponId.normalize(weaponId);
        if (!WeaponId.isValid(normalized)) {
            return ChangeResult.INVALID_ID;
        }
        if (!disabledWeapons.contains(normalized)) {
            return ChangeResult.UNCHANGED;
        }
        LinkedHashSet<String> changed = new LinkedHashSet<>(disabledWeapons);
        changed.remove(normalized);
        if (!repository.save(changed)) {
            return ChangeResult.WRITE_FAILED;
        }
        disabledWeapons = Set.copyOf(changed);
        BetterCombatRegistryBridge.refresh();
        return ChangeResult.CHANGED;
    }

    public synchronized boolean reload() {
        ConfigRepository.LoadResult result = repository.load();
        if (!result.successful()) {
            return false;
        }
        disabledWeapons = result.disabledWeapons();
        BetterCombatRegistryBridge.refresh();
        return true;
    }

    public enum ChangeResult {
        CHANGED,
        UNCHANGED,
        INVALID_ID,
        WRITE_FAILED
    }
}
