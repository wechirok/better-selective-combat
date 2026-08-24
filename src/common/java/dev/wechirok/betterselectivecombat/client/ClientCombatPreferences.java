package dev.wechirok.betterselectivecombat.client;

import dev.wechirok.betterselectivecombat.selection.WeaponId;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ClientCombatPreferences {
    private final ClientCombatConfigRepository repository;
    private volatile boolean enabled = true;
    private volatile Set<String> ignoredWeapons = Set.of();

    public ClientCombatPreferences(ClientCombatConfigRepository repository) {
        this.repository = repository;
    }

    public synchronized void initialize() {
        ClientCombatConfigRepository.LoadResult result = repository.load();
        if (!result.successful()) {
            throw new IllegalStateException("Cannot load Better Selective Combat client configuration");
        }
        enabled = result.enabled();
        ignoredWeapons = result.ignoredWeapons();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean ignores(String weaponId) {
        return ignoredWeapons.contains(WeaponId.normalize(weaponId));
    }

    public boolean shouldIgnore(String weaponId) {
        return !enabled || ignores(weaponId);
    }

    public synchronized ToggleResult toggleEnabled() {
        boolean changed = !enabled;
        if (!repository.save(changed, ignoredWeapons)) {
            return new ToggleResult(false, enabled);
        }
        enabled = changed;
        return new ToggleResult(true, enabled);
    }

    public synchronized ToggleResult toggleWeapon(String weaponId) {
        String normalized = WeaponId.normalize(weaponId);
        if (!WeaponId.isValid(normalized)) {
            return new ToggleResult(false, !ignoredWeapons.contains(normalized));
        }
        LinkedHashSet<String> changed = new LinkedHashSet<>(ignoredWeapons);
        boolean weaponEnabled;
        if (changed.remove(normalized)) {
            weaponEnabled = true;
        } else {
            changed.add(normalized);
            weaponEnabled = false;
        }
        if (!repository.save(enabled, changed)) {
            return new ToggleResult(false, !ignoredWeapons.contains(normalized));
        }
        ignoredWeapons = Set.copyOf(changed);
        return new ToggleResult(true, weaponEnabled);
    }

    public record ToggleResult(boolean successful, boolean enabled) {
    }
}
