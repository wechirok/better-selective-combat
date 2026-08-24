package dev.wechirok.betterselectivecombat.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;

public final class ClientHud {
    private ClientHud() {
    }

    public static void show(Minecraft minecraft, Component message) {
        if (minecraft.getConnection() != null) {
            minecraft.getConnection().setActionBarText(new ClientboundSetActionBarTextPacket(message));
        }
    }
}
