package dev.wechirok.betterselectivecombat.fabric;

import dev.wechirok.betterselectivecombat.client.BetterSelectiveCombatClient;
import dev.wechirok.betterselectivecombat.client.ClientKeyMappings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class BetterSelectiveCombatFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BetterSelectiveCombatClient.initialize(FabricLoader.getInstance().getConfigDir());
        FabricKeyMappingRegistrar.register(ClientKeyMappings.toggleAll());
        FabricKeyMappingRegistrar.register(ClientKeyMappings.toggleItem());
    }
}
