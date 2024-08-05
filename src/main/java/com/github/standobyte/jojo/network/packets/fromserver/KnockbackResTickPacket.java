package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.mod.NoKnockbackOnBlocking;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class KnockbackResTickPacket {
    private final int entityId;
    
    public KnockbackResTickPacket(int entityId) {
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<KnockbackResTickPacket> {

        @Override
        public void encode(KnockbackResTickPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public KnockbackResTickPacket decode(PacketBuffer buf) {
            return new KnockbackResTickPacket(buf.readInt());
        }

        @Override
        public void handle(KnockbackResTickPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                ModifiableAttributeInstance kbRes = living.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
                if (!kbRes.hasModifier(NoKnockbackOnBlocking.ONE_TICK_KB_RES)) {
                    kbRes.addTransientModifier(NoKnockbackOnBlocking.ONE_TICK_KB_RES);
                }
                if (living == ClientUtil.getClientPlayer()) {
                    NoKnockbackOnBlocking.clCancelHurtBob = true;
                    NoKnockbackOnBlocking.clDidHurtWithNoBob = false;
                }
            }
        }

        @Override
        public Class<KnockbackResTickPacket> getPacketClass() {
            return KnockbackResTickPacket.class;
        }
    }
}
