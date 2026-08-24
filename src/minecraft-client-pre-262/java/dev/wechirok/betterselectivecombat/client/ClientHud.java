package dev.wechirok.betterselectivecombat.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientHud {
    private ClientHud() {
    }

    public static void show(Minecraft minecraft, Component message) {
        minecraft.gui.setOverlayMessage(message, false);
    }
}
