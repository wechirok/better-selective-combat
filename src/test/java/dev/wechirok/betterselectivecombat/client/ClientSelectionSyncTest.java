package dev.wechirok.betterselectivecombat.client;

import dev.wechirok.betterselectivecombat.network.CombatSelection;
import dev.wechirok.betterselectivecombat.network.CombatSelectionState;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientSelectionSyncTest {
    private Minecraft minecraft;
    private ItemStack weapon;
    private AtomicBoolean available;
    private List<CombatSelection> sent;
    private MockedStatic<Minecraft> game;
    private MockedStatic<SelectedItemAccess> inventory;
    private MockedStatic<ItemIds> ids;
    private MockedStatic<BetterSelectiveCombatClient> preferences;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void setUp() {
        minecraft = mock(Minecraft.class);
        minecraft.player = mock(LocalPlayer.class);
        weapon = mock(ItemStack.class);
        game = mockStatic(Minecraft.class);
        inventory = mockStatic(SelectedItemAccess.class);
        ids = mockStatic(ItemIds.class);
        preferences = mockStatic(BetterSelectiveCombatClient.class);
        game.when(Minecraft::getInstance).thenReturn(minecraft);
        inventory.when(() -> SelectedItemAccess.get(minecraft)).thenReturn(weapon);
        ids.when(() -> ItemIds.get(weapon)).thenReturn("example:greatsword");
        when(minecraft.getConnection()).thenReturn(mock(ClientPacketListener.class));
        available = new AtomicBoolean(true);
        sent = new ArrayList<>();
        ClientSelectionSync.initialize(available::get, sent::add);
    }

    @AfterEach
    void tearDown() {
        when(minecraft.getConnection()).thenReturn(null);
        ClientSelectionSync.synchronize();
        CombatSelectionState.setClientFilter(stack -> false);
        preferences.close();
        ids.close();
        inventory.close();
        game.close();
    }

    @Test
    void serverWithoutChannelReceivesNoPackets() {
        available.set(false);
        preferences.when(() -> BetterSelectiveCombatClient.shouldIgnore(weapon)).thenReturn(true);
        ClientSelectionSync.synchronize();
        assertTrue(sent.isEmpty());
        available.set(true);
        ClientSelectionSync.synchronize();
        assertEquals(List.of(new CombatSelection("example:greatsword", true)), sent);
    }

    @Test
    void unchangedTicksDoNotSendDuplicates() {
        for (int i = 0; i < 100; i++) {
            ClientSelectionSync.synchronize();
        }
        assertEquals(List.of(new CombatSelection("example:greatsword", false)), sent);
    }

    @Test
    void togglesAndWeaponChangesSendNewSelection() {
        ClientSelectionSync.synchronize();
        preferences.when(() -> BetterSelectiveCombatClient.shouldIgnore(weapon)).thenReturn(true);
        ClientSelectionSync.synchronize();
        ids.when(() -> ItemIds.get(weapon)).thenReturn("example:spear");
        ClientSelectionSync.synchronize();
        preferences.when(() -> BetterSelectiveCombatClient.shouldIgnore(weapon)).thenReturn(false);
        ClientSelectionSync.synchronize();
        assertEquals(List.of(
                new CombatSelection("example:greatsword", false),
                new CombatSelection("example:greatsword", true),
                new CombatSelection("example:spear", true),
                new CombatSelection("example:spear", false)), sent);
    }

    @Test
    void reconnectResendsEvenWhenWeaponHasNotChanged() {
        ClientSelectionSync.synchronize();
        when(minecraft.getConnection()).thenReturn(mock(ClientPacketListener.class));
        ClientSelectionSync.synchronize();
        assertEquals(2, sent.size());
        assertEquals(sent.get(0), sent.get(1));
    }

    @Test
    void noPlayerDoesNotSendAnInventoryUpdate() {
        minecraft.player = null;
        ClientSelectionSync.synchronize();
        assertTrue(sent.isEmpty());
    }
}
