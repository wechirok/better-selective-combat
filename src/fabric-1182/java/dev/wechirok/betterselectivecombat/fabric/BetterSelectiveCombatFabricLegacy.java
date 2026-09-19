package dev.wechirok.betterselectivecombat.fabric;

import dev.wechirok.betterselectivecombat.BetterSelectiveCombat;
import dev.wechirok.betterselectivecombat.command.BscCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

public final class BetterSelectiveCombatFabricLegacy implements ModInitializer {
    @Override
    public void onInitialize() {
        BetterSelectiveCombat.initialize(FabricLoader.getInstance().getConfigDir());
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> BscCommands.register(dispatcher));
    }
}
