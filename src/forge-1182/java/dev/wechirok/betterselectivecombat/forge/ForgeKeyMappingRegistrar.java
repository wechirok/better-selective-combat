package dev.wechirok.betterselectivecombat.forge;

import dev.wechirok.betterselectivecombat.client.ClientKeyMappings;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ForgeKeyMappingRegistrar {
    private ForgeKeyMappingRegistrar() {
    }

    public static void register(IEventBus bus) {
        bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            ClientRegistry.registerKeyBinding(ClientKeyMappings.toggleAll());
            ClientRegistry.registerKeyBinding(ClientKeyMappings.toggleItem());
        }));
    }
}
