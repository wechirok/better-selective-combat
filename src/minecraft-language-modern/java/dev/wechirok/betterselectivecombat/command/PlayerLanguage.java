package dev.wechirok.betterselectivecombat.command;

import net.minecraft.server.level.ServerPlayer;

public final class PlayerLanguage {
    private PlayerLanguage() {
    }

    public static String get(ServerPlayer player) {
        return player.clientInformation().language();
    }
}
