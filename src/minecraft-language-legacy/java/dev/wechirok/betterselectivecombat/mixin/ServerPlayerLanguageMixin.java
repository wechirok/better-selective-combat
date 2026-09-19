package dev.wechirok.betterselectivecombat.mixin;

import dev.wechirok.betterselectivecombat.command.PlayerLanguage;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerLanguageMixin implements PlayerLanguage {
    @Unique
    private String betterSelectiveCombat$language = "en_us";

    @Inject(method = "updateOptions", at = @At("HEAD"))
    private void betterSelectiveCombat$receiveLanguage(ServerboundClientInformationPacket packet, CallbackInfo callbackInfo) {
        betterSelectiveCombat$language = packet.language();
    }

    @Override
    public String betterSelectiveCombat$language() {
        return betterSelectiveCombat$language;
    }
}
