package dev.wechirok.betterselectivecombat.network;

import dev.wechirok.betterselectivecombat.selection.WeaponId;
import net.minecraft.network.FriendlyByteBuf;

public record CombatSelection(String weaponId, boolean disabled) {
    public static final int MAX_ID_LENGTH = 256;

    public CombatSelection {
        if (weaponId == null || weaponId.length() > MAX_ID_LENGTH || !WeaponId.isValid(weaponId)) {
            throw new IllegalArgumentException("Invalid weapon ID");
        }
    }

    public static CombatSelection read(FriendlyByteBuf buffer) {
        return new CombatSelection(buffer.readUtf(MAX_ID_LENGTH), buffer.readBoolean());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(weaponId, MAX_ID_LENGTH);
        buffer.writeBoolean(disabled);
    }
}
