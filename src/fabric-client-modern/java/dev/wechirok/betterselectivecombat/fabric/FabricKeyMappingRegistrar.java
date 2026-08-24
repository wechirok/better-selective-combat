package dev.wechirok.betterselectivecombat.fabric;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public final class FabricKeyMappingRegistrar {
    private FabricKeyMappingRegistrar() {
    }

    public static void register(KeyMapping keyMapping) {
        KeyMappingHelper.registerKeyMapping(keyMapping);
    }
}
