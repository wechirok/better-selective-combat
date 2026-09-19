package dev.wechirok.betterselectivecombat.forge;

import dev.wechirok.betterselectivecombat.client.ClientKeyMappings;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ForgeKeyMappingRegistrar {
    private ForgeKeyMappingRegistrar() {
    }

    public static void register(IEventBus bus) {
        bus.addListener((RegisterKeyMappingsEvent event) -> {
            event.register(ClientKeyMappings.toggleAll());
            event.register(ClientKeyMappings.toggleItem());
        });
    }
}
