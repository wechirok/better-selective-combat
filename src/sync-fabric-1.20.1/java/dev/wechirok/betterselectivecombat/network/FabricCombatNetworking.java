package dev.wechirok.betterselectivecombat.network;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class FabricCombatNetworking implements ModInitializer {
    public static final ResourceLocation CHANNEL = new ResourceLocation("better_selective_combat", "selection_v1");

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, (server, player, handler, buffer, responseSender) -> {
            CombatSelection selection = CombatSelection.read(buffer);
            server.execute(() -> CombatSelectionState.accept(player, selection));
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> CombatSelectionState.remove(handler.player));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> CombatSelectionState.clear());
    }
}
