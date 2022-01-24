package com.github.standobyte.jojo.entity.stand;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandEntitySwingsPacket;

import net.minecraft.util.HandSide;

public class ArmSwings {
    private static final int LIMIT = 64;
    private int swings;
    private long handSideBits;
    
    void addSwing(HandSide side) {
        if (swings < LIMIT) {
            swings++;
            handSideBits <<= 1;
            if (side == HandSide.RIGHT) {
                handSideBits |= 1;
            }
        }
    }
    
    void broadcastSwings(StandEntity entity) {
        if (swings > 0) {
            PacketManager.sendToClientsTracking(new TrStandEntitySwingsPacket(entity.getId(), swings, handSideBits), entity);
            reset();
        }
    }
    
    public void clAddValues(int swings, long handSideBits) {
        swings = Math.min(LIMIT - this.swings, swings);
        this.swings += swings;
        this.handSideBits <<= swings;
        this.handSideBits |= handSideBits;
    }
    
    public int getSwingsCount() {
        return swings;
    }
    
    public long getHandSideBits() {
        return handSideBits;
    }
    
    public void reset() {
        swings = 0;
        handSideBits = 0;
    }
}
