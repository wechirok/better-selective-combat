package dev.wechirok.betterselectivecombat.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

final class SelectedItemAccess {
    private SelectedItemAccess() {
    }

    static ItemStack get(Minecraft minecraft) {
        return minecraft.player.getInventory().getSelected();
    }
}
