package dev.wechirok.betterselectivecombat.forge;

import dev.wechirok.betterselectivecombat.BetterSelectiveCombat;
import dev.wechirok.betterselectivecombat.command.BscCommands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BetterSelectiveCombat.MOD_ID)
public final class BetterSelectiveCombatForge {
    public BetterSelectiveCombatForge() {
        BetterSelectiveCombat.initialize(FMLPaths.CONFIGDIR.get());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            BetterSelectiveCombatForgeClient.initialize(FMLJavaModLoadingContext.get().getModEventBus());
        }
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        BscCommands.register(event.getDispatcher());
    }
}
