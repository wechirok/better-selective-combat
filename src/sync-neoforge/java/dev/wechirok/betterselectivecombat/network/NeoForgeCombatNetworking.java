package dev.wechirok.betterselectivecombat.network;

import dev.wechirok.betterselectivecombat.BetterSelectiveCombat;
import dev.wechirok.betterselectivecombat.client.ClientSelectionSync;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = BetterSelectiveCombat.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class NeoForgeCombatNetworking {
    private NeoForgeCombatNetworking() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToServer(CombatSelectionPayload.TYPE, CombatSelectionPayload.CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        CombatSelectionState.accept((ServerPlayer) context.player(), payload.selection())));
    }

    @EventBusSubscriber(modid = BetterSelectiveCombat.MOD_ID)
    public static final class Server {
        @SubscribeEvent
        public static void disconnect(PlayerEvent.PlayerLoggedOutEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                CombatSelectionState.remove(player);
            }
        }

        @SubscribeEvent
        public static void stop(ServerStoppedEvent event) {
            CombatSelectionState.clear();
        }
    }

    @EventBusSubscriber(modid = BetterSelectiveCombat.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Client {
        @SubscribeEvent
        public static void initialize(FMLClientSetupEvent event) {
            event.enqueueWork(() -> ClientSelectionSync.initialize(
                    () -> Minecraft.getInstance().getConnection() != null
                            && Minecraft.getInstance().getConnection().hasChannel(CombatSelectionPayload.TYPE),
                    selection -> PacketDistributor.sendToServer(new CombatSelectionPayload(selection))));
        }
    }
}
