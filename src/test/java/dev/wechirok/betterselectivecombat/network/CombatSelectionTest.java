package dev.wechirok.betterselectivecombat.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatSelectionTest {
    @Test
    void packetRoundTripsBothStates() {
        for (boolean disabled : new boolean[]{false, true}) {
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                var selection = new CombatSelection("example:greatsword", disabled);
                selection.write(buffer);
                assertEquals(selection, CombatSelection.read(buffer));
                assertEquals(0, buffer.readableBytes());
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void rejectsInvalidAndOversizedIds() {
        assertThrows(IllegalArgumentException.class, () -> new CombatSelection("invalid id", true));
        assertThrows(IllegalArgumentException.class, () -> new CombatSelection(null, true));
        assertThrows(IllegalArgumentException.class, () -> new CombatSelection("example:" + "x".repeat(256), true));
    }
}
