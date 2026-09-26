package dev.wechirok.betterselectivecombat.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CombatSelectionPayload(CombatSelection selection) implements CustomPacketPayload {
    public static final Type<CombatSelectionPayload> TYPE = new Type<>(
            Identifier.parse("better_selective_combat:selection_v1"));
    public static final StreamCodec<FriendlyByteBuf, CombatSelectionPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> payload.selection().write(buffer),
            buffer -> new CombatSelectionPayload(CombatSelection.read(buffer)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
