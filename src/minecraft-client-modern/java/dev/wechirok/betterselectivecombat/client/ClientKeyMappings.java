package dev.wechirok.betterselectivecombat.client;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

public final class ClientKeyMappings {
    private static final KeyMapping TOGGLE_ALL = new KeyMapping(
            "key.better_selective_combat.toggle_all",
            InputConstants.KEY_PERIOD,
            KeyMapping.Category.GAMEPLAY
    );
    private static final KeyMapping TOGGLE_ITEM = new KeyMapping(
            "key.better_selective_combat.toggle_item",
            InputConstants.KEY_COMMA,
            KeyMapping.Category.GAMEPLAY
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
