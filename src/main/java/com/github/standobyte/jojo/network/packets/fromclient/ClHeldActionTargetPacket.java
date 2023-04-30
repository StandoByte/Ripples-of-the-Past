package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHeldActionTargetPacket {
    private final PowerClassification classification;
    private int targetEntityId = -1;
    @Nullable
    private BlockPos targetBlock;
    @Nullable
    private Direction blockFace;

    public static ClHeldActionTargetPacket withRayTraceResult(PowerClassification classification, RayTraceResult target) {
        switch (target.getType()) {
        case ENTITY:
            return new ClHeldActionTargetPacket(classification, ((EntityRayTraceResult) target).getEntity().getId());
        case BLOCK:
            BlockRayTraceResult blockTarget = (BlockRayTraceResult) target;
            return new ClHeldActionTargetPacket(classification, blockTarget.getBlockPos(), blockTarget.getDirection());
        default:
            return new ClHeldActionTargetPacket(classification);
        }
    }

    private ClHeldActionTargetPacket(PowerClassification classification) {
        this.classification = classification;
    }

    private ClHeldActionTargetPacket(PowerClassification classification, int targetEntityId) {
        this(classification);
        this.targetEntityId = targetEntityId;
    }

    private ClHeldActionTargetPacket(PowerClassification classification, BlockPos targetBlock, Direction blockFace) {
        this(classification);
        this.targetBlock = targetBlock;
        this.blockFace = blockFace;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHeldActionTargetPacket> {

        @Override
        public void encode(ClHeldActionTargetPacket msg, PacketBuffer buf) {
            byte targetType = 0;
            if (msg.targetBlock != null) {
                targetType |= 1;
            }
            else if (msg.targetEntityId > 0){
                targetType |= 2;
            }
            buf.writeByte(targetType);
            buf.writeEnum(msg.classification);
            if (msg.targetBlock != null) {
                buf.writeBlockPos(msg.targetBlock);
                buf.writeEnum(msg.blockFace);
            }
            else if (msg.targetEntityId > 0){
                buf.writeInt(msg.targetEntityId);
            }
        }

        @Override
        public ClHeldActionTargetPacket decode(PacketBuffer buf) {
            byte targetType = buf.readByte();
            switch (targetType & 3) {
            case 1:
                return new ClHeldActionTargetPacket(buf.readEnum(PowerClassification.class), buf.readBlockPos(), buf.readEnum(Direction.class));
            case 2:
                return new ClHeldActionTargetPacket(buf.readEnum(PowerClassification.class), buf.readInt());
            default: // 0
                return new ClHeldActionTargetPacket(buf.readEnum(PowerClassification.class));
            }
        }

        @Override
        public void handle(ClHeldActionTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            if (!player.isSpectator()) {
                IPower.getPowerOptional(player, msg.classification).ifPresent(power -> {
                    ActionTarget target = msg.targetEntityId == -1 ? msg.targetBlock == null ? 
                            ActionTarget.EMPTY
                            : new ActionTarget(msg.targetBlock, msg.blockFace)
                            : new ActionTarget(player.level.getEntity(msg.targetEntityId));
                    power.setMouseTarget(target);
                });
            }
        }

        @Override
        public Class<ClHeldActionTargetPacket> getPacketClass() {
            return ClHeldActionTargetPacket.class;
        }
    }

}
