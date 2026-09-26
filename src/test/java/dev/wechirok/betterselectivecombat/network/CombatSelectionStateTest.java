package dev.wechirok.betterselectivecombat.network;

import dev.wechirok.betterselectivecombat.client.ItemIds;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CombatSelectionStateTest {
    private MinecraftServer server;
    private PlayerList players;
    private ServerPlayer player;
    private Inventory inventory;
    private ItemStack main;
    private ItemStack offhand;
    private MockedStatic<ItemIds> ids;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void setUp() {
        CombatSelectionState.clear();
        CombatSelectionState.setClientFilter(stack -> false);
        server = mock(MinecraftServer.class);
        players = mock(PlayerList.class);
        player = mock(ServerPlayer.class);
        inventory = new Inventory(player);
        main = mock(ItemStack.class);
        offhand = mock(ItemStack.class);
        inventory.items.set(0, main);
        inventory.offhand.set(0, offhand);
        UUID uuid = UUID.randomUUID();
        when(server.isSameThread()).thenReturn(true);
        when(server.getPlayerList()).thenReturn(players);
        when(player.getServer()).thenReturn(server);
        when(player.getUUID()).thenReturn(uuid);
        when(player.getInventory()).thenReturn(inventory);
        when(player.getOffhandItem()).thenReturn(offhand);
        when(players.getPlayer(uuid)).thenReturn(player);
        ids = mockStatic(ItemIds.class);
        ids.when(() -> ItemIds.get(main)).thenReturn("example:greatsword");
    }

    @AfterEach
    void tearDown() {
        ids.close();
        CombatSelectionState.clear();
    }

    @Test
    void disablesBothHandsForOnlyTheSendingPlayer() {
        CombatSelectionState.accept(player, new CombatSelection("example:greatsword", true));
        assertTrue(CombatSelectionState.shouldIgnore(main));
        assertTrue(CombatSelectionState.shouldIgnore(offhand));
        assertFalse(CombatSelectionState.shouldIgnore(mock(ItemStack.class)));
    }

    @Test
    void identicalWeaponsRemainEnabledForOtherPlayers() {
        ServerPlayer other = mock(ServerPlayer.class);
        Inventory otherInventory = new Inventory(other);
        ItemStack otherWeapon = mock(ItemStack.class);
        ItemStack otherOffhand = mock(ItemStack.class);
        otherInventory.items.set(0, otherWeapon);
        otherInventory.offhand.set(0, otherOffhand);
        UUID otherId = UUID.randomUUID();
        when(other.getInventory()).thenReturn(otherInventory);
        when(other.getOffhandItem()).thenReturn(otherOffhand);
        when(other.getUUID()).thenReturn(otherId);
        when(players.getPlayer(otherId)).thenReturn(other);
        ids.when(() -> ItemIds.get(otherWeapon)).thenReturn("example:greatsword");

        CombatSelectionState.accept(player, new CombatSelection("example:greatsword", true));
        assertTrue(CombatSelectionState.shouldIgnore(main));
        assertFalse(CombatSelectionState.shouldIgnore(otherWeapon));
        assertFalse(CombatSelectionState.shouldIgnore(otherOffhand));
    }

    @Test
    void mismatchedOrStaleSelectionCannotDisableAnotherWeapon() {
        CombatSelectionState.accept(player, new CombatSelection("example:greatsword", true));
        ids.when(() -> ItemIds.get(main)).thenReturn("example:spear");
        assertFalse(CombatSelectionState.shouldIgnore(main));
        assertFalse(CombatSelectionState.shouldIgnore(offhand));
    }

    @Test
    void changingSlotsDoesNotKeepFilteringOldStack() {
        CombatSelectionState.accept(player, new CombatSelection("example:greatsword", true));
        ItemStack next = mock(ItemStack.class);
        inventory.items.set(0, next);
        ids.when(() -> ItemIds.get(next)).thenReturn("example:spear");
        assertFalse(CombatSelectionState.shouldIgnore(main));
        assertFalse(CombatSelectionState.shouldIgnore(next));
        assertFalse(CombatSelectionState.shouldIgnore(offhand));
    }

    @Test
    void reenableLogoutAndShutdownRemovePersonalState() {
        CombatSelection selection = new CombatSelection("example:greatsword", true);
        CombatSelectionState.accept(player, selection);
        CombatSelectionState.accept(player, new CombatSelection("example:greatsword", false));
        assertFalse(CombatSelectionState.shouldIgnore(main));
        CombatSelectionState.accept(player, selection);
        CombatSelectionState.remove(player);
        assertFalse(CombatSelectionState.shouldIgnore(main));
        CombatSelectionState.accept(player, selection);
        CombatSelectionState.clear();
        assertFalse(CombatSelectionState.shouldIgnore(main));
    }

    @Test
    void clientThreadCannotUseServerPreferences() {
        CombatSelectionState.accept(player, new CombatSelection("example:greatsword", true));
        when(server.isSameThread()).thenReturn(false);
        assertFalse(CombatSelectionState.shouldIgnore(main));
        assertThrows(IllegalStateException.class, () ->
                CombatSelectionState.accept(player, new CombatSelection("example:greatsword", false)));
    }

    @Test
    void respawnUsesCurrentPlayerInstance() {
        CombatSelectionState.accept(player, new CombatSelection("example:greatsword", true));
        ServerPlayer replacement = mock(ServerPlayer.class);
        Inventory replacementInventory = new Inventory(replacement);
        ItemStack replacementWeapon = mock(ItemStack.class);
        replacementInventory.items.set(0, replacementWeapon);
        ids.when(() -> ItemIds.get(replacementWeapon)).thenReturn("example:greatsword");
        when(replacement.getInventory()).thenReturn(replacementInventory);
        when(players.getPlayer(player.getUUID())).thenReturn(replacement);
        assertTrue(CombatSelectionState.shouldIgnore(replacementWeapon));
        assertFalse(CombatSelectionState.shouldIgnore(main));
    }
}
