package dev.wechirok.betterselectivecombat.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class SelectionPlayerAccess {
    private SelectionPlayerAccess() {
    }

    public static MinecraftServer server(ServerPlayer player) {
        return player.getServer();
    }

    public static ItemStack mainHand(ServerPlayer player) {
        return player.getInventory().getSelected();
    }

    public static ItemStack offhand(ServerPlayer player) {
        return player.getOffhandItem();
    }
}
