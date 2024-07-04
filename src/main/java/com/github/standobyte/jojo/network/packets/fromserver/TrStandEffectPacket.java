package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrStandEffectPacket {
    private final PacketType packetType;
    private final int userId;
    private final int effectId;
    private final int targetId;
    private final StandEffectType<?> effectFactory;
    private final StandEffectInstance effect;
    private final boolean isUser;
    private final PacketBuffer buf;
    
    public static TrStandEffectPacket add(StandEffectInstance effect, boolean sentToOwner) {
        return new TrStandEffectPacket(PacketType.ADD, effect.getStandUser().getId(), effect.getId(), 
                Optional.ofNullable(effect.getTarget()).map(Entity::getId).orElse(-1), effect.effectType, effect, sentToOwner, 
                null);
    }
    
    public static TrStandEffectPacket remove(StandEffectInstance effect) {
        return new TrStandEffectPacket(PacketType.REMOVE, effect.getStandUser().getId(), effect.getId(), 
                -1, null, null, false, 
                null);
    }
    
    public static TrStandEffectPacket updateTarget(StandEffectInstance effect) {
        return new TrStandEffectPacket(PacketType.UPDATE_TARGET, effect.getStandUser().getId(), effect.getId(), 
                Optional.ofNullable(effect.getTarget()).map(Entity::getId).orElse(-1), 
                null, null, false, 
                null);
    }
    
    private TrStandEffectPacket(PacketType packetType, int userId, int effectId, int targetId, 
            StandEffectType<?> effectFactory, StandEffectInstance effect, boolean isUser, PacketBuffer buf) {
        this.packetType = packetType;
        this.userId = userId;
        this.effectId = effectId;
        this.targetId = targetId;
        this.effectFactory = effectFactory;
        this.effect = effect;
        this.isUser = isUser;
        this.buf = buf;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrStandEffectPacket> {

        @Override
        public void encode(TrStandEffectPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.packetType);
            switch (msg.packetType) {
            case ADD:
                buf.writeInt(msg.userId);
                buf.writeInt(msg.effectId);
                buf.writeInt(msg.targetId);
                buf.writeRegistryId(msg.effectFactory);
                buf.writeBoolean(msg.isUser);
                
                buf.writeVarInt(msg.effect.tickCount);
                msg.effect.writeAdditionalPacketData(buf, msg.isUser);
                break;
            case REMOVE:
                buf.writeInt(msg.userId);
                buf.writeInt(msg.effectId);
                break;
            case UPDATE_TARGET:
                buf.writeInt(msg.userId);
                buf.writeInt(msg.effectId);
                buf.writeInt(msg.targetId);
                break;
            }
        }

        @Override
        public TrStandEffectPacket decode(PacketBuffer buf) {
            PacketType type = buf.readEnum(PacketType.class);
            switch (type) {
            case ADD:
                return new TrStandEffectPacket(type, buf.readInt(), buf.readInt(), 
                        buf.readInt(), buf.readRegistryIdSafe(StandEffectType.class), null, buf.readBoolean(), buf);
            case REMOVE:
                return new TrStandEffectPacket(type, buf.readInt(), buf.readInt(), 
                        -1, null, null, false, null);
            case UPDATE_TARGET:
                return new TrStandEffectPacket(type, buf.readInt(), buf.readInt(), 
                        buf.readInt(), null, null, false, null);
            }
            return null;
        }

        @Override
        public void handle(TrStandEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.userId);
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                IStandPower.getStandPowerOptional(livingEntity).ifPresent(stand -> {
                    switch (msg.packetType) {
                    case ADD:
                        StandEffectInstance newEffect = msg.effectFactory.create().withId(msg.effectId).withStand(stand);
                        if (msg.targetId != -1) {
                            newEffect.withTargetEntityId(msg.targetId);
                        }
                        
                        newEffect.tickCount = msg.buf.readVarInt();
                        newEffect.readAdditionalPacketData(msg.buf, msg.isUser);
                        stand.getContinuousEffects().addEffect(newEffect);
                        break;
                    case REMOVE:
                        StandEffectsTracker effects = stand.getContinuousEffects();
                        effects.removeEffect(effects.getById(msg.effectId));
                    case UPDATE_TARGET:
                        StandEffectInstance effect = stand.getContinuousEffects().getById(msg.effectId);
                        if (effect != null) {
                            effect.withTargetEntityId(msg.targetId);
                        }
                    }
                });
            }
        }

        @Override
        public Class<TrStandEffectPacket> getPacketClass() {
            return TrStandEffectPacket.class;
        }
    }
    
    private enum PacketType {
        ADD,
        REMOVE,
        UPDATE_TARGET
    }
}
