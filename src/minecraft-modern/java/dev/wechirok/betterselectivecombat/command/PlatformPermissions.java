package dev.wechirok.betterselectivecombat.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public final class PlatformPermissions {
    private PlatformPermissions() {
    }

    public static boolean canManage(CommandSourceStack source) {
        if (source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return true;
        }
        ServerPlayer player = source.getPlayer();
        return player != null && source.getServer().isSingleplayerOwner(player.nameAndId());
    }
}
