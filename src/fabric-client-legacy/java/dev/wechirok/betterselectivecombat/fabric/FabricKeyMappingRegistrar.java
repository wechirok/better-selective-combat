package dev.wechirok.betterselectivecombat.fabric;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public final class FabricKeyMappingRegistrar {
    private FabricKeyMappingRegistrar() {
    }

    public static void register(KeyMapping keyMapping) {
        KeyBindingHelper.registerKeyBinding(keyMapping);
    }
}
