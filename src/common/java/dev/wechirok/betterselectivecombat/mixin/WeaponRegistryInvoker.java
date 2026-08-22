package dev.wechirok.betterselectivecombat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.bettercombat.logic.WeaponRegistry", remap = false)
public interface WeaponRegistryInvoker {
    @Invoker(value = "encodeRegistry", remap = false)
    static void betterSelectiveCombat$encodeRegistry() {
        throw new AssertionError();
    }
}
