package dev.wechirok.betterselectivecombat.network;

import dev.wechirok.betterselectivecombat.BetterSelectiveCombat;
import dev.wechirok.betterselectivecombat.client.ClientSelectionSync;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = BetterSelectiveCombat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ForgeCombatNetworking {
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(BetterSelectiveCombat.MOD_ID, "selection_v1"))
            .networkProtocolVersion(() -> "1")
            .clientAcceptedVersions(version -> true)
            .serverAcceptedVersions(version -> true)
            .simpleChannel();

    private ForgeCombatNetworking() {
    }

    @SubscribeEvent
    public static void initialize(FMLCommonSetupEvent event) {
        CHANNEL.messageBuilder(CombatSelection.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CombatSelection::write)
                .decoder(CombatSelection::read)
                .consumerMainThread((selection, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) {
                        CombatSelectionState.accept(player, selection);
                    }
                })
                .add();
        MinecraftForge.EVENT_BUS.addListener(ForgeCombatNetworking::disconnect);
        MinecraftForge.EVENT_BUS.addListener(ForgeCombatNetworking::stop);
    }

    private static void disconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CombatSelectionState.remove(player);
        }
    }

    private static void stop(ServerStoppedEvent event) {
        CombatSelectionState.clear();
    }

    @Mod.EventBusSubscriber(modid = BetterSelectiveCombat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Client {
        @SubscribeEvent
        public static void initialize(FMLClientSetupEvent event) {
            event.enqueueWork(() -> ClientSelectionSync.initialize(
                    () -> Minecraft.getInstance().getConnection() != null
                            && CHANNEL.isRemotePresent(Minecraft.getInstance().getConnection().getConnection()),
                    CHANNEL::sendToServer));
        }
    }
}
