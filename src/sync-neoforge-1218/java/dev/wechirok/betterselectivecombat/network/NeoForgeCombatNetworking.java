package dev.wechirok.betterselectivecombat.network;

import dev.wechirok.betterselectivecombat.client.ClientSelectionSync;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NeoForgeCombatNetworking {
    private NeoForgeCombatNetworking() {
    }

    public static void initialize(IEventBus modEventBus) {
        modEventBus.addListener(NeoForgeCombatNetworking::register);
        NeoForge.EVENT_BUS.addListener(NeoForgeCombatNetworking::disconnect);
        NeoForge.EVENT_BUS.addListener(NeoForgeCombatNetworking::stop);
    }

    private static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToServer(CombatSelectionPayload.TYPE, CombatSelectionPayload.CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        CombatSelectionState.accept((ServerPlayer) context.player(), payload.selection())));
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
                            && Minecraft.getInstance().getConnection().hasChannel(CombatSelectionPayload.TYPE),
                    selection -> ClientPacketDistributor.sendToServer(new CombatSelectionPayload(selection)));
        }
    }
}
