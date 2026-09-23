package dev.wechirok.betterselectivecombat.network;

import dev.wechirok.betterselectivecombat.client.ClientSelectionSync;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public final class FabricCombatNetworkingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSelectionSync.initialize(() -> ClientPlayNetworking.canSend(FabricCombatNetworking.CHANNEL), selection -> {
            var buffer = PacketByteBufs.create();
            selection.write(buffer);
            ClientPlayNetworking.send(FabricCombatNetworking.CHANNEL, buffer);
        });
    }
}
