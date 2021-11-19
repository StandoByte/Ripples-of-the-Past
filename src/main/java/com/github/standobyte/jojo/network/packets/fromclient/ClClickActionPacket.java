package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
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
    private final PowerClassification classification;
    private final ActionType actionType;
    private final boolean shift;
    private final int index;
    private int targetEntityId = -1;
    @Nullable
    private BlockPos targetBlock;
    @Nullable
    private Direction blockFace;

    private ClClickActionPacket(PowerClassification classification, ActionType actionType, boolean shift, int index) {
        this.classification = classification;
        this.actionType = actionType;
        this.shift = shift;
        this.index = index;
    }

    private ClClickActionPacket(PowerClassification classification, ActionType actionType, boolean shift, int index, int targetEntityId) {
        this(classification, actionType, shift, index);
        this.targetEntityId = targetEntityId;
    }

    private ClClickActionPacket(PowerClassification classification, ActionType actionType, boolean shift, int index, BlockPos targetBlock, Direction blockFace) {
        this(classification, actionType, shift, index);
        this.targetBlock = targetBlock;
        this.blockFace = blockFace;
    }

    public static ClClickActionPacket withRayTraceResult(PowerClassification classification, ActionType actionType, boolean shift, int index, RayTraceResult target) {
        switch (target.getType()) {
        case ENTITY:
            return new ClClickActionPacket(classification, actionType, shift, index, ((EntityRayTraceResult) target).getEntity().getId());
        case BLOCK:
            BlockRayTraceResult blockTarget = (BlockRayTraceResult) target;
            return new ClClickActionPacket(classification, actionType, shift, index, blockTarget.getBlockPos(), blockTarget.getDirection());
        default:
            return new ClClickActionPacket(classification, actionType, shift, index);
        }
    }

    public static void encode(ClClickActionPacket msg, PacketBuffer buf) {
        byte flags = 0;
        if (msg.shift) {
            flags |= 4;
        }
        if (msg.targetBlock != null) {
            flags |= 1;
        }
        else if (msg.targetEntityId > 0) {
            flags |= 2;
        }
        buf.writeByte(flags);
        buf.writeEnum(msg.classification);
        buf.writeEnum(msg.actionType);
        buf.writeVarInt(msg.index);
        if (msg.targetBlock != null) {
            buf.writeBlockPos(msg.targetBlock);
            buf.writeEnum(msg.blockFace);
        }
        else if (msg.targetEntityId > 0){
            buf.writeInt(msg.targetEntityId);
        }
    }

    public static ClClickActionPacket decode(PacketBuffer buf) {
        byte flags = buf.readByte();
        switch (flags & 3) {
        case 1:
            return new ClClickActionPacket(buf.readEnum(PowerClassification.class), buf.readEnum(ActionType.class), 
                    (flags & 4) > 0, buf.readVarInt(), buf.readBlockPos(), buf.readEnum(Direction.class));
        case 2:
            return new ClClickActionPacket(buf.readEnum(PowerClassification.class), buf.readEnum(ActionType.class), 
                    (flags & 4) > 0, buf.readVarInt(), buf.readInt());
        default: // 0
            return new ClClickActionPacket(buf.readEnum(PowerClassification.class), buf.readEnum(ActionType.class), 
                    (flags & 4) > 0, buf.readVarInt());
        }
    }

    public static void handle(ClClickActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            if (!player.isSpectator()) {
                IPower.getPowerOptional(player, msg.classification).ifPresent(power -> {
                    Entity targetEntity = msg.targetEntityId != -1 ? player.level.getEntity(msg.targetEntityId) : null;
                    ActionTarget target = targetEntity == null ? msg.targetBlock == null ? 
                            ActionTarget.EMPTY
                            : new ActionTarget(msg.targetBlock, msg.blockFace)
                            : new ActionTarget(targetEntity);
                    power.onClickAction(msg.actionType, msg.index, msg.shift, target);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
