package dev.wechirok.betterselectivecombat.command;

import java.util.UUID;

interface PermissionResolver {
    boolean hasPermission(UUID playerId, String permission);
}
