package dev.wechirok.betterselectivecombat.neoforge;

import dev.wechirok.betterselectivecombat.BetterSelectiveCombat;
import dev.wechirok.betterselectivecombat.command.BscCommands;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(BetterSelectiveCombat.MOD_ID)
public final class BetterSelectiveCombatNeoForge {
    public BetterSelectiveCombatNeoForge() {
        BetterSelectiveCombat.initialize(FMLPaths.CONFIGDIR.get());
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        BscCommands.register(event.getDispatcher());
    }
}
