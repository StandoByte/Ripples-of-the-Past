package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandEffectsTracker;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrStandEffectPacket {
    private final PacketType packetType;
    private final int userId;
    private final int effectId;
    private final int[] targetsId;
    private final StandEffectType<?> effectFactory;
    private final StandEffectInstance effect;
    private final PacketBuffer buf;
    
    public static TrStandEffectPacket add(StandEffectInstance effect) {
        return new TrStandEffectPacket(PacketType.ADD, effect.getStandUser().getId(), 
                effect.getId(), effect.getTargets().stream().mapToInt(Entity::getId).toArray(), effect.effectType, effect, null);
    }
    
    public static TrStandEffectPacket remove(StandEffectInstance effect) {
        return new TrStandEffectPacket(PacketType.REMOVE, effect.getStandUser().getId(), effect.getId(), 
                null, null, null, null);
    }
    
    private TrStandEffectPacket(PacketType packetType, int userId, int effectId, int[] targetsId, 
            StandEffectType<?> effectFactory, StandEffectInstance effect, PacketBuffer buf) {
        this.packetType = packetType;
        this.userId = userId;
        this.effectId = effectId;
        this.targetsId = targetsId;
        this.effectFactory = effectFactory;
        this.effect = effect;
        this.buf = buf;
    }

    public static void encode(TrStandEffectPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.packetType);
        switch (msg.packetType) {
        case ADD:
            buf.writeInt(msg.userId);
            buf.writeInt(msg.effectId);
            NetworkUtil.writeIntArray(buf, msg.targetsId);
            buf.writeRegistryId(msg.effectFactory);
            msg.effect.writeAdditionalPacketData(buf);
            break;
        case REMOVE:
            buf.writeInt(msg.userId);
            buf.writeInt(msg.effectId);
            break;
        }
    }

    public static TrStandEffectPacket decode(PacketBuffer buf) {
        PacketType type = buf.readEnum(PacketType.class);
        switch (type) {
        case ADD:
            return new TrStandEffectPacket(type, buf.readInt(), buf.readInt(), NetworkUtil.readIntArray(buf), 
                    buf.readRegistryIdSafe(StandEffectType.class), null, buf);
        case REMOVE:
            return new TrStandEffectPacket(type, buf.readInt(), buf.readInt(), null, 
                    null, null, null);
        }
        return null;
    }

    public static void handle(TrStandEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.userId);
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                IStandPower.getStandPowerOptional(livingEntity).ifPresent(stand -> {
                    switch (msg.packetType) {
                    case ADD:
                        StandEffectInstance effect = msg.effectFactory.create().withId(msg.effectId).withStand(stand);
                        effect.readAdditionalPacketData(msg.buf);
                        stand.getContinuousEffects().addEffect(effect);
                        break;
                    case REMOVE:
                        StandEffectsTracker effects = stand.getContinuousEffects();
                        effects.removeEffect(effects.getById(msg.effectId));
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    private enum PacketType {
        ADD,
        REMOVE
    }
}
