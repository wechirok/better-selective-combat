package dev.wechirok.betterselectivecombat.network;

import dev.wechirok.betterselectivecombat.client.ClientSelectionSync;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public final class NeoForgeCombatNetworking {
    private NeoForgeCombatNetworking() {
    }

    public static void initialize(IEventBus modEventBus) {
        modEventBus.addListener(NeoForgeCombatNetworking::register);
        NeoForge.EVENT_BUS.addListener(NeoForgeCombatNetworking::disconnect);
        NeoForge.EVENT_BUS.addListener(NeoForgeCombatNetworking::stop);
    }

    private static void register(RegisterPayloadHandlerEvent event) {
        event.registrar("1").optional().play(CombatSelectionPayload.ID, CombatSelectionPayload::new,
                builder -> builder.server((payload, context) -> context.workHandler().execute(() ->
                        context.player().filter(ServerPlayer.class::isInstance).map(ServerPlayer.class::cast)
                                .ifPresent(player -> CombatSelectionState.accept(player, payload.selection())))));
    }

    private static void disconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CombatSelectionState.remove(player);
        }
    }

    private static void stop(ServerStoppedEvent event) {
        CombatSelectionState.clear();
    }

    public static void initializeClient() {
        Client.initialize();
    }

    private static final class Client {
        private static void initialize() {
            ClientSelectionSync.initialize(
                    () -> Minecraft.getInstance().getConnection() != null
                            && NetworkRegistry.getInstance().isConnected(Minecraft.getInstance().getConnection(), CombatSelectionPayload.ID),
                    selection -> PacketDistributor.SERVER.noArg().send(new CombatSelectionPayload(selection)));
        }
    }
}
