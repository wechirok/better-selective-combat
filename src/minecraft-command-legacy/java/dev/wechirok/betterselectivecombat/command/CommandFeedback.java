package dev.wechirok.betterselectivecombat.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public final class CommandFeedback {
    private CommandFeedback() {
    }

    public static void success(CommandSourceStack source, Component message) {
        source.sendSuccess(message, false);
    }
}
