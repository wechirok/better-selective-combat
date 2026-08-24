package dev.wechirok.betterselectivecombat.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public final class NeoForgeEnvironment {
    private NeoForgeEnvironment() {
    }

    public static boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }
}
