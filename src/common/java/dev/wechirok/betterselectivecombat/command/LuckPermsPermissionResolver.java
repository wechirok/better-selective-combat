package dev.wechirok.betterselectivecombat.command;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public final class LuckPermsPermissionResolver implements PermissionResolver {
    @Override
    public boolean hasPermission(UUID playerId, String permission) {
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(playerId);
            return user != null
                    && user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        } catch (IllegalStateException exception) {
            return false;
        }
    }
}
