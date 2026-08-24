package dev.wechirok.betterselectivecombat.neoforge;

import dev.wechirok.betterselectivecombat.client.BetterSelectiveCombatClient;
import dev.wechirok.betterselectivecombat.client.ClientKeyMappings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public final class BetterSelectiveCombatNeoForgeClient {
    private BetterSelectiveCombatNeoForgeClient() {
    }

    public static void initialize(IEventBus modEventBus) {
        BetterSelectiveCombatClient.initialize(FMLPaths.CONFIGDIR.get());
        modEventBus.addListener(BetterSelectiveCombatNeoForgeClient::registerKeyMappings);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientKeyMappings.toggleAll());
        event.register(ClientKeyMappings.toggleItem());
    }
}
