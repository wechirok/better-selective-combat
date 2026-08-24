package dev.wechirok.betterselectivecombat.mixin.client;

import dev.wechirok.betterselectivecombat.client.BetterSelectiveCombatClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void betterSelectiveCombat$clientTick(CallbackInfo callbackInfo) {
        BetterSelectiveCombatClient.tick((Minecraft) (Object) this);
    }
}
