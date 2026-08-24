package dev.wechirok.betterselectivecombat.mixin.client;

import dev.wechirok.betterselectivecombat.client.BetterSelectiveCombatClient;
import net.bettercombat.api.WeaponAttributes;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.bettercombat.logic.WeaponRegistry", remap = false)
public abstract class WeaponRegistryClientMixin {
    @Inject(
            method = "getAttributes(Lnet/minecraft/class_1799;)Lnet/bettercombat/api/WeaponAttributes;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void betterSelectiveCombat$applyClientPreference(
            ItemStack stack,
            CallbackInfoReturnable<WeaponAttributes> callbackInfo
    ) {
        if (callbackInfo.getReturnValue() != null && BetterSelectiveCombatClient.shouldIgnore(stack)) {
            callbackInfo.setReturnValue(null);
        }
    }
}
