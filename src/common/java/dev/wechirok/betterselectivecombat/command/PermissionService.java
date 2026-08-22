package dev.wechirok.betterselectivecombat.command;

import java.util.UUID;

public final class PermissionService {
    public static final String MANAGE = "better_selective_combat.manage";
    private static final PermissionResolver RESOLVER = createResolver();

    private PermissionService() {
    }

    public static boolean canManage(UUID playerId) {
        return RESOLVER.hasPermission(playerId, MANAGE);
    }

    private static PermissionResolver createResolver() {
        try {
            ClassLoader loader = PermissionService.class.getClassLoader();
            Class.forName("net.luckperms.api.LuckPermsProvider", false, loader);
            Class<?> type = Class.forName(
                    "dev.wechirok.betterselectivecombat.command.LuckPermsPermissionResolver",
                    true,
                    loader
            );
            return (PermissionResolver) type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | LinkageError exception) {
            return (playerId, permission) -> false;
        }
    }
}
