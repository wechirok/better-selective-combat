package dev.wechirok.betterselectivecombat.client;

import dev.wechirok.betterselectivecombat.network.CombatSelection;
import dev.wechirok.betterselectivecombat.network.CombatSelectionState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class ClientSelectionSync {
    private static BooleanSupplier available = () -> false;
    private static Consumer<CombatSelection> sender = selection -> {};
    private static Object connection;
    private static CombatSelection sent;

    private ClientSelectionSync() {
    }

    public static void initialize(BooleanSupplier channelAvailable, Consumer<CombatSelection> send) {
        available = channelAvailable;
        sender = send;
        CombatSelectionState.setClientFilter(ClientSelectionSync::ignoreOffhand);
    }

    public static void synchronize() {
        Minecraft minecraft = Minecraft.getInstance();
        Object currentConnection = minecraft.getConnection();
        if (connection != currentConnection) {
            connection = currentConnection;
            sent = null;
        }
        if (minecraft.player == null || currentConnection == null || !available.getAsBoolean()) {
            sent = null;
            return;
        }
        ItemStack stack = SelectedItemAccess.get(minecraft);
        String id = ItemIds.get(stack);
        if (id.length() > CombatSelection.MAX_ID_LENGTH) {
            id = "minecraft:air";
        }
        CombatSelection selection = new CombatSelection(id, BetterSelectiveCombatClient.shouldIgnore(stack));
        if (!selection.equals(sent)) {
            sender.accept(selection);
            sent = selection;
        }
    }

    private static boolean ignoreOffhand(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.isSameThread() && minecraft.player != null
                && stack == minecraft.player.getInventory().offhand.get(0)
                && BetterSelectiveCombatClient.shouldIgnore(SelectedItemAccess.get(minecraft));
    }
}
