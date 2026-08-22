package dev.wechirok.betterselectivecombat.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public final class PlatformPermissions {
    private PlatformPermissions() {
    }

    public static boolean canManage(CommandSourceStack source) {
        if (source.hasPermission(2)) {
            return true;
        }
        ServerPlayer player = source.getPlayer();
        return player != null
                && (source.getServer().isSingleplayerOwner(player.getGameProfile())
                || PermissionService.canManage(player.getUUID()));
    }
}
