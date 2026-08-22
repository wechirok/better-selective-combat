package dev.wechirok.betterselectivecombat.mixin;

import dev.wechirok.betterselectivecombat.registry.BetterCombatRegistryBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(targets = "net.bettercombat.logic.WeaponRegistry", remap = false)
public abstract class WeaponRegistryMixin {
    @Shadow(remap = false)
    private static Map<Object, Object> registrations;

    @Shadow(remap = false)
    private static Map<Object, Object> containers;

    @Inject(method = "encodeRegistry()V", at = @At("HEAD"), remap = false)
    private static void betterSelectiveCombat$filterRegistry(CallbackInfo callbackInfo) {
        BetterCombatRegistryBridge.captureAndFilter(registrations, containers);
    }
}
