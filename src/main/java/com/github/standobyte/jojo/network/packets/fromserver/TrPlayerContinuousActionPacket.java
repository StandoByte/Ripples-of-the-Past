package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrPlayerContinuousActionPacket {
    private final int entityId;
    private final PacketType packetType;
    
    private final Optional<Action<?>> action;
    
    private final StandEntityAction.Phase phase;
    
    public TrPlayerContinuousActionPacket(int entityId, Optional<Action<?>> action) {
        this(entityId, PacketType.SET_ACTION, action, null);
    }
    
    public static TrPlayerContinuousActionPacket setPhase(int entityId, StandEntityAction.Phase phase) {
        return new TrPlayerContinuousActionPacket(entityId, PacketType.SET_PHASE, null, phase);
    }
    
    private TrPlayerContinuousActionPacket(int entityId, PacketType packetType, 
            Optional<Action<?>> action, StandEntityAction.Phase phase) {
        this.entityId = entityId;
        this.packetType = packetType;
        this.action = action;
        this.phase = phase;
    }
    
    public static TrPlayerContinuousActionPacket specialPacket(int entityId, PacketType type) {
        if (type == PacketType.SET_ACTION || type == PacketType.SET_PHASE) {
            throw new IllegalArgumentException();
        }
        return new TrPlayerContinuousActionPacket(entityId, type, null, null);
    }
    
    public enum PacketType {
        SET_ACTION,
        SET_PHASE
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrPlayerContinuousActionPacket> {

        @Override
        public void encode(TrPlayerContinuousActionPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeEnum(msg.packetType);
            switch (msg.packetType) {
            case SET_ACTION:
                NetworkUtil.writeOptional(buf, msg.action, buf::writeRegistryId);
                break;
            case SET_PHASE:
                buf.writeEnum(msg.phase);
                break;
            default:
                break;
            }
        }

        @Override
        public TrPlayerContinuousActionPacket decode(PacketBuffer buf) {
            int entityId = buf.readInt();
            PacketType packetType = buf.readEnum(PacketType.class);
            switch (packetType) {
            case SET_ACTION:
                return new TrPlayerContinuousActionPacket(
                        entityId, NetworkUtil.readOptional(buf, () -> buf.readRegistryIdSafe(Action.class)));
            case SET_PHASE:
                return setPhase(entityId, buf.readEnum(StandEntityAction.Phase.class));
            default:
                return specialPacket(entityId, packetType);
            }
        }

        @Override
        public void handle(TrPlayerContinuousActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof PlayerEntity) {
                LivingEntity player = (LivingEntity) entity;
                entity.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    switch (msg.packetType) {
                    case SET_ACTION:
                        if (msg.action.isPresent()) {
                            setContinuousAction(cap, player, msg.action.get());
                        }
                        else {
                            cap.stopContinuousAction();
                        }
                        break;
                    case SET_PHASE:
                        cap.getContinuousAction().ifPresent(action -> action.setPhase(msg.phase, true));
                        break;
                    }
                });
            }
        }
        
        private <P extends IPower<P, ?>> void setContinuousAction(PlayerUtilCap playerCap, LivingEntity player, Action<P> action) {
            P power = (P) IPower.getPowerOptional(player, action.getPowerClassification()).orElse(null);
            if (power != null && action instanceof IPlayerAction) {
                IPlayerAction<?, P> playerAction = (IPlayerAction<?, P>) action;
                playerCap.setContinuousAction(playerAction.createContinuousActionInstance(player, playerCap, power));
            }
        }

        @Override
        public Class<TrPlayerContinuousActionPacket> getPacketClass() {
            return TrPlayerContinuousActionPacket.class;
        }
    }
}
