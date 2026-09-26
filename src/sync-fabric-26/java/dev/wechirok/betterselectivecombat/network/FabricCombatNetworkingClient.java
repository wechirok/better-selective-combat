package dev.wechirok.betterselectivecombat.network;

import dev.wechirok.betterselectivecombat.client.ClientSelectionSync;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class FabricCombatNetworkingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSelectionSync.initialize(() -> ClientPlayNetworking.canSend(CombatSelectionPayload.TYPE),
                selection -> ClientPlayNetworking.send(new CombatSelectionPayload(selection)));
    }
}
