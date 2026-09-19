package dev.wechirok.betterselectivecombat.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class GameText {
    private GameText() {
    }

    public static MutableComponent literal(String text) {
        return Component.literal(text);
    }

    public static MutableComponent translatable(String key, Object... arguments) {
        return Component.translatable(key, arguments);
    }
}
