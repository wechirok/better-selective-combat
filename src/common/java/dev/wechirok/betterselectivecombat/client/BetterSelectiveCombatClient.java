package dev.wechirok.betterselectivecombat.client;

import dev.wechirok.betterselectivecombat.config.ConfigPaths;
import net.bettercombat.api.MinecraftClient_BetterCombat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;

public final class BetterSelectiveCombatClient {
    private static ClientCombatPreferences preferences;

    private BetterSelectiveCombatClient() {
    }

    public static synchronized void initialize(Path configDirectory) {
        if (preferences != null) {
            return;
        }
        Path file = ConfigPaths.clientFile(configDirectory);
        ClientCombatPreferences created = new ClientCombatPreferences(new ClientCombatConfigRepository(file));
        created.initialize();
        preferences = created;
    }

    public static boolean shouldIgnore(ItemStack stack) {
        ClientCombatPreferences current = preferences;
        Minecraft minecraft = Minecraft.getInstance();
        if (current == null || !minecraft.isSameThread() || minecraft.player == null || stack != selectedItem(minecraft)) {
            return false;
        }
        return current.shouldIgnore(itemId(stack));
    }

    public static void tick(Minecraft minecraft) {
        if (preferences == null || minecraft.player == null) {
            return;
        }
        while (ClientKeyMappings.toggleAll().consumeClick()) {
            toggleAll(minecraft);
        }
        while (ClientKeyMappings.toggleItem().consumeClick()) {
            toggleItem(minecraft);
        }
    }

    private static void toggleAll(Minecraft minecraft) {
        ClientCombatPreferences.ToggleResult result = preferences.toggleEnabled();
        if (!result.successful()) {
            show(minecraft, "bsc.client.error.write", ChatFormatting.RED);
            return;
        }
        cancelAttack(minecraft);
        show(minecraft, result.enabled() ? "bsc.client.global.enabled" : "bsc.client.global.disabled",
                result.enabled() ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private static void toggleItem(Minecraft minecraft) {
        ItemStack stack = selectedItem(minecraft);
        if (stack.isEmpty()) {
            show(minecraft, "bsc.client.item.empty", ChatFormatting.YELLOW);
            return;
        }
        ClientCombatPreferences.ToggleResult result = preferences.toggleWeapon(itemId(stack));
        if (!result.successful()) {
            show(minecraft, "bsc.client.error.write", ChatFormatting.RED);
            return;
        }
        cancelAttack(minecraft);
        Component message = Component.translatable(
                result.enabled() ? "bsc.client.item.enabled" : "bsc.client.item.disabled",
                stack.getHoverName()
        ).withStyle(result.enabled() ? ChatFormatting.GREEN : ChatFormatting.RED);
        ClientHud.show(minecraft, message);
    }

    private static void cancelAttack(Minecraft minecraft) {
        ((MinecraftClient_BetterCombat) minecraft).cancelUpswing();
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static ItemStack selectedItem(Minecraft minecraft) {
        return SelectedItemAccess.get(minecraft);
    }

    private static void show(Minecraft minecraft, String key, ChatFormatting color) {
        ClientHud.show(minecraft, Component.translatable(key).withStyle(color));
    }
}
