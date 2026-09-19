package dev.wechirok.betterselectivecombat.client;

import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;

public final class ItemIds {
    private ItemIds() {
    }

    public static String get(ItemStack stack) {
        return Registry.ITEM.getKey(stack.getItem()).toString();
    }
}
