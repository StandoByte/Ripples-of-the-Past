package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClClickActionPacket {
    private final PowerClassification power;
    private final Action<?> action;
    
    private int targetEntityId = -1;
    @Nullable
    private BlockPos targetBlock;
    @Nullable
    private Direction blockFace;
    private boolean sneak;
    
    public ClClickActionPacket(PowerClassification power, Action<?> action, RayTraceResult target, boolean sneak) {
        this.power = power;
        this.action = action;
        this.sneak = sneak;
        
        if (target != null) {
            switch (target.getType()) {
            case ENTITY:
                this.targetEntityId = ((EntityRayTraceResult) target).getEntity().getId();
                break;
            case BLOCK:
                BlockRayTraceResult blockTarget = (BlockRayTraceResult) target;
                this.targetBlock = blockTarget.getBlockPos();
                this.blockFace = blockTarget.getDirection();
                break;
            default:
                break;
            }
        }
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClClickActionPacket> {
    
        @Override
        public void encode(ClClickActionPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.power);
            buf.writeRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry(), msg.action);
            
            byte flags = 0;
            if (msg.targetBlock != null) {
                flags |= 1;
            }
            else if (msg.targetEntityId > 0) {
                flags |= 2;
            }
            if (msg.sneak) {
                flags |= 4;
            }
            buf.writeByte(flags);
            if (msg.targetBlock != null) {
                buf.writeBlockPos(msg.targetBlock);
                buf.writeEnum(msg.blockFace);
            }
            else if (msg.targetEntityId > 0){
                buf.writeInt(msg.targetEntityId);
            }
        }

        @Override
        public ClClickActionPacket decode(PacketBuffer buf) {
            PowerClassification power = buf.readEnum(PowerClassification.class);
            Action<?> action = buf.readRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry());
            
            byte flags = buf.readByte();
            
            ClClickActionPacket packet = new ClClickActionPacket(power, action, null, (flags & 4) > 0);
            switch (flags & 3) {
            case 1:
                packet.targetBlock = buf.readBlockPos();
                packet.blockFace = buf.readEnum(Direction.class);
                break;
            case 2:
                packet.targetEntityId = buf.readInt();
            default: // 0
                break;
            }
            return packet;
        }

        @Override
        public void handle(ClClickActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
            if (msg.action == null) return;
            PlayerEntity player = ctx.get().getSender();
            if (player.isSpectator() || !player.isAlive()) return;
            
            IPower.getPowerOptional(player, msg.power).ifPresent(power -> {
                Entity targetEntity = msg.targetEntityId != -1 ? player.level.getEntity(msg.targetEntityId) : null;
                ActionTarget target = targetEntity == null ? msg.targetBlock == null ? 
                        ActionTarget.EMPTY
                        : new ActionTarget(msg.targetBlock, msg.blockFace)
                        : new ActionTarget(targetEntity);
                clickAction(power, msg.action, msg.sneak, target);
            });
        }
        
        private <P extends IPower<P, ?>> void clickAction(IPower<?, ?> power, Action<P> action, boolean sneak, ActionTarget target) {
            ((P) power).clickAction(action, sneak, target);
        }
        
        @Override
        public Class<ClClickActionPacket> getPacketClass() {
            return ClClickActionPacket.class;
        }
    }
}
