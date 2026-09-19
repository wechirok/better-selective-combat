package dev.wechirok.betterselectivecombat.compat;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public final class GameText {
    private GameText() {
    }

    public static MutableComponent literal(String text) {
        return new TextComponent(text);
    }

    public static MutableComponent translatable(String key, Object... arguments) {
        return new TranslatableComponent(key, arguments);
    }
}
