package dev.wechirok.betterselectivecombat.command;

import net.minecraft.server.level.ServerPlayer;

public interface PlayerLanguage {
    String betterSelectiveCombat$language();

    static String get(ServerPlayer player) {
        return ((PlayerLanguage) player).betterSelectiveCombat$language();
    }
}
