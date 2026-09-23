package dev.wechirok.betterselectivecombat.mixin.client;

import dev.wechirok.betterselectivecombat.client.ClientSelectionSync;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = 1100)
public abstract class CombatSelectionClientMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void betterSelectiveCombat$syncTick(CallbackInfo callbackInfo) {
        ClientSelectionSync.synchronize();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void betterSelectiveCombat$syncUse(CallbackInfo callbackInfo) {
        ClientSelectionSync.synchronize();
    }

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void betterSelectiveCombat$syncAttack(CallbackInfoReturnable<Boolean> callbackInfo) {
        ClientSelectionSync.synchronize();
    }
}
