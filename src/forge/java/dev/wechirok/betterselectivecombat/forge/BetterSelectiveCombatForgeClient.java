package dev.wechirok.betterselectivecombat.forge;

import dev.wechirok.betterselectivecombat.client.BetterSelectiveCombatClient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLPaths;

public final class BetterSelectiveCombatForgeClient {
    private BetterSelectiveCombatForgeClient() {
    }

    public static void initialize(IEventBus modEventBus) {
        BetterSelectiveCombatClient.initialize(FMLPaths.CONFIGDIR.get());
        ForgeKeyMappingRegistrar.register(modEventBus);
    }
}
