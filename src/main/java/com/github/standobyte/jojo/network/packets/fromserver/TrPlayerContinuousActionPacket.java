package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.player.IPlayerAction;
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
    private final Optional<Action<?>> action;
    
    public TrPlayerContinuousActionPacket(int entityId, Optional<Action<?>> action) {
        this.entityId = entityId;
        this.action = action;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrPlayerContinuousActionPacket> {

        @Override
        public void encode(TrPlayerContinuousActionPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            NetworkUtil.writeOptional(buf, msg.action, action -> buf.writeRegistryId(action));
        }

        @Override
        public TrPlayerContinuousActionPacket decode(PacketBuffer buf) {
            return new TrPlayerContinuousActionPacket(
                    buf.readInt(), 
                    NetworkUtil.readOptional(buf, () -> buf.readRegistryIdSafe(Action.class)));
        }

        @Override
        public void handle(TrPlayerContinuousActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof PlayerEntity) {
                LivingEntity player = (LivingEntity) entity;
                entity.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    if (msg.action.isPresent()) {
                        setContinuousAction(cap, player, msg.action.get());
                    }
                    else {
                        cap.stopContinuousAction();
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
