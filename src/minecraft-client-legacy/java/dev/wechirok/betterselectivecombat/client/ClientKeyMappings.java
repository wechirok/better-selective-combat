package dev.wechirok.betterselectivecombat.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ClientKeyMappings {
    private static final KeyMapping TOGGLE_ALL = new KeyMapping(
            "key.better_selective_combat.toggle_all",
            GLFW.GLFW_KEY_PERIOD,
            "key.categories.better_selective_combat"
    );
    private static final KeyMapping TOGGLE_ITEM = new KeyMapping(
            "key.better_selective_combat.toggle_item",
            GLFW.GLFW_KEY_COMMA,
            "key.categories.better_selective_combat"
    );

    private ClientKeyMappings() {
    }

    public static KeyMapping toggleAll() {
        return TOGGLE_ALL;
    }

    public static KeyMapping toggleItem() {
        return TOGGLE_ITEM;
    }
}
