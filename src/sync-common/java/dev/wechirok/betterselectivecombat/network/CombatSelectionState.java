package dev.wechirok.betterselectivecombat.network;

import dev.wechirok.betterselectivecombat.client.ItemIds;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public final class CombatSelectionState {
    private static final Map<UUID, CombatSelection> selections = new HashMap<>();
    private static volatile MinecraftServer server;
    private static Predicate<ItemStack> clientFilter = stack -> false;

    private CombatSelectionState() {
    }

    public static void setClientFilter(Predicate<ItemStack> filter) {
        clientFilter = filter;
    }

    public static void accept(ServerPlayer player, CombatSelection selection) {
        MinecraftServer currentServer = SelectionPlayerAccess.server(player);
        if (currentServer == null || !currentServer.isSameThread()) {
            throw new IllegalStateException("Combat selection must be updated on the server thread");
        }
        if (server != currentServer) {
            selections.clear();
            server = currentServer;
        }
        if (selection.disabled()) {
            selections.put(player.getUUID(), selection);
        } else {
            selections.remove(player.getUUID());
        }
    }

    public static void remove(ServerPlayer player) {
        selections.remove(player.getUUID());
    }

    public static void clear() {
        selections.clear();
        server = null;
    }

    public static boolean shouldIgnore(ItemStack stack) {
        MinecraftServer currentServer = server;
        if (currentServer == null || !currentServer.isSameThread()) {
            return clientFilter.test(stack);
        }
        for (var entry : selections.entrySet()) {
            ServerPlayer player = currentServer.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                continue;
            }
            ItemStack mainHand = SelectionPlayerAccess.mainHand(player);
            if (stack == mainHand || stack == SelectionPlayerAccess.offhand(player)) {
                return entry.getValue().weaponId().equals(ItemIds.get(mainHand));
            }
        }
        return false;
    }
}
