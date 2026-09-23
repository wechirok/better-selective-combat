package dev.wechirok.betterselectivecombat.network;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class FabricCombatNetworking implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(CombatSelectionPayload.TYPE, CombatSelectionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CombatSelectionPayload.TYPE,
                (payload, context) -> CombatSelectionState.accept(context.player(), payload.selection()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> CombatSelectionState.remove(handler.player));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> CombatSelectionState.clear());
    }
}
