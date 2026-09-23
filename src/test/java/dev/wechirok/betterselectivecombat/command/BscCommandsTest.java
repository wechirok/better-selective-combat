package dev.wechirok.betterselectivecombat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.wechirok.betterselectivecombat.BetterSelectiveCombat;
import dev.wechirok.betterselectivecombat.client.ItemIds;
import dev.wechirok.betterselectivecombat.lang.Translations;
import dev.wechirok.betterselectivecombat.selection.WeaponSelectionService;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BscCommandsTest {
    private CommandDispatcher<CommandSourceStack> dispatcher;
    private CommandSourceStack source;
    private ServerPlayer player;
    private ItemStack held;
    private WeaponSelectionService selections;
    private MockedStatic<BetterSelectiveCombat> mod;
    private MockedStatic<PlatformPermissions> permissions;
    private MockedStatic<ItemIds> ids;
    private MockedStatic<CommandFeedback> feedback;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void setUp() throws CommandSyntaxException {
        dispatcher = new CommandDispatcher<>();
        source = mock(CommandSourceStack.class);
        player = mock(ServerPlayer.class);
        held = mock(ItemStack.class);
        selections = mock(WeaponSelectionService.class);
        mod = mockStatic(BetterSelectiveCombat.class);
        permissions = mockStatic(PlatformPermissions.class);
        ids = mockStatic(ItemIds.class);
        feedback = mockStatic(CommandFeedback.class);
        mod.when(BetterSelectiveCombat::selections).thenReturn(selections);
        mod.when(BetterSelectiveCombat::translations).thenReturn(new Translations());
        permissions.when(() -> PlatformPermissions.canManage(source)).thenReturn(true);
        when(source.getPlayerOrException()).thenReturn(player);
        when(player.getMainHandItem()).thenReturn(held);
        ids.when(() -> ItemIds.get(held)).thenReturn("example:greatsword");
        when(selections.disable(anyString())).thenReturn(WeaponSelectionService.ChangeResult.CHANGED);
        when(selections.enable(anyString())).thenReturn(WeaponSelectionService.ChangeResult.CHANGED);
        BscCommands.register(dispatcher);
    }

    @AfterEach
    void tearDown() {
        feedback.close();
        ids.close();
        permissions.close();
        mod.close();
    }

    @Test
    void heldUsesMainHandForBothAliasesAndOperations() throws Exception {
        for (String alias : new String[]{"bsc", "betterselectivecombat"}) {
            assertEquals(1, dispatcher.execute(alias + " disable held", source));
            assertEquals(1, dispatcher.execute(alias + " enable held", source));
            assertEquals(1, dispatcher.execute(alias + " status held", source));
        }
        verify(selections, times(2)).disable("example:greatsword");
        verify(selections, times(2)).enable("example:greatsword");
        verify(selections, times(2)).isDisabled("example:greatsword");
        verify(player, never()).getOffhandItem();
    }

    @Test
    void heldDoesNotChangePermissions() throws Exception {
        permissions.when(() -> PlatformPermissions.canManage(source)).thenReturn(false);
        assertEquals(0, dispatcher.execute("bsc disable held", source));
        assertEquals(0, dispatcher.execute("bsc enable held", source));
        verifyNoInteractions(selections);
        verify(source, never()).getPlayerOrException();
        assertEquals(1, dispatcher.execute("bsc status held", source));
    }

    @Test
    void emptyHandDoesNotStoreAir() throws Exception {
        when(held.isEmpty()).thenReturn(true);
        for (String operation : new String[]{"disable", "enable", "status"}) {
            assertEquals(0, dispatcher.execute("bsc " + operation + " held", source));
        }
        verifyNoInteractions(selections);
        verify(source, times(3)).sendFailure(argThat(message ->
                message.getString().equals("Hold an item in your main hand")));
    }

    @Test
    void consoleCannotUseHeld() throws Exception {
        when(source.getPlayerOrException()).thenThrow(CommandSyntaxException.BUILT_IN_EXCEPTIONS
                .dispatcherUnknownCommand().create());
        assertThrows(CommandSyntaxException.class, () -> dispatcher.execute("bsc disable held", source));
        verifyNoInteractions(selections);
    }

    @Test
    void explicitIdsStillWorkIncludingAnItemNamedHeld() throws Exception {
        assertEquals(1, dispatcher.execute("bsc disable example:held", source));
        assertEquals(1, dispatcher.execute("bsc enable minecraft:diamond_sword", source));
        verify(selections).disable("example:held");
        verify(selections).enable("minecraft:diamond_sword");
        verify(source, never()).getPlayerOrException();
    }

    @Test
    void heldIsSuggestedWithoutLookingUpAnId() throws Exception {
        var suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("bsc disable he", source)).get();
        assertTrue(suggestions.getList().stream().anyMatch(suggestion -> suggestion.getText().equals("held")));
    }
}
