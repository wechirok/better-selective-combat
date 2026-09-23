package dev.wechirok.betterselectivecombat.network;

import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OffhandIntegrationTest {
    private Field registrationsField;
    private Object originalRegistrations;
    private Map<ResourceLocation, WeaponAttributes> registrations;
    private ServerPlayer player;
    private Inventory inventory;
    private ItemStack weapon;
    private WeaponAttributes attributes;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void setUp() throws Exception {
        CombatSelectionState.clear();
        registrationsField = WeaponRegistry.class.getDeclaredField("registrations");
        registrationsField.setAccessible(true);
        originalRegistrations = registrationsField.get(null);
        registrations = new HashMap<>();
        registrationsField.set(null, registrations);
        attributes = new WeaponAttributes(0, 0, null, null, true, null, null, null);
        WeaponRegistry.register(BuiltInRegistries.ITEM.getKey(Items.DIAMOND_SWORD), attributes);
        var server = mock(MinecraftServer.class);
        var players = mock(PlayerList.class);
        player = mock(ServerPlayer.class, CALLS_REAL_METHODS);
        inventory = new Inventory(player);
        weapon = new ItemStack(Items.DIAMOND_SWORD);
        inventory.items.set(0, weapon);
        var field = Player.class.getDeclaredField("inventory");
        field.setAccessible(true);
        field.set(player, inventory);
        doReturn(inventory).when(player).getInventory();
        doReturn(server).when(player).getServer();
        doReturn(UUID.randomUUID()).when(player).getUUID();
        when(server.isSameThread()).thenReturn(true);
        when(server.getPlayerList()).thenReturn(players);
        when(players.getPlayer(player.getUUID())).thenReturn(player);
    }

    @AfterEach
    void tearDown() throws Exception {
        registrationsField.set(null, originalRegistrations);
        CombatSelectionState.clear();
    }

    @Test
    void restoresOffhandThroughActualBetterCombatMixins() {
        for (var item : new net.minecraft.world.item.Item[]{Items.TORCH, Items.SHIELD, Items.APPLE}) {
            ItemStack stack = new ItemStack(item);
            inventory.offhand.set(0, stack);
            assertTrue(player.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty());
            CombatSelectionState.accept(player, new CombatSelection("minecraft:diamond_sword", true));
            assertNull(WeaponRegistry.getAttributes(weapon));
            assertSame(stack, player.getItemBySlot(EquipmentSlot.OFFHAND));
            assertSame(attributes, WeaponRegistry.getAttributes(new ItemStack(Items.DIAMOND_SWORD)));
            CombatSelectionState.accept(player, new CombatSelection("minecraft:diamond_sword", false));
            assertSame(attributes, WeaponRegistry.getAttributes(weapon));
            assertTrue(player.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty());
        }
    }

    @Test
    void personalReenableCannotRestoreAdministrativelyRemovedAttributes() {
        registrations.clear();
        CombatSelectionState.accept(player, new CombatSelection("minecraft:diamond_sword", true));
        assertNull(WeaponRegistry.getAttributes(weapon));
        CombatSelectionState.accept(player, new CombatSelection("minecraft:diamond_sword", false));
        assertNull(WeaponRegistry.getAttributes(weapon));
        assertTrue(registrations.isEmpty());
    }
}
