package dev.wechirok.betterselectivecombat.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CombatSelectionPayload(CombatSelection selection) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("better_selective_combat", "selection_v1");

    public CombatSelectionPayload(FriendlyByteBuf buffer) {
        this(CombatSelection.read(buffer));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        selection.write(buffer);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
