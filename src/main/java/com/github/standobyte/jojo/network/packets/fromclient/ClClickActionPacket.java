package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.network.NetworkUtil;
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
    private Optional<Action<?>> inputValidation = Optional.empty();
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
    
    public ClClickActionPacket validateInput(@Nonnull Action<?> clientClickedAction) {
        this.inputValidation = Optional.of(clientClickedAction);
        return this;
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
        NetworkUtil.writeOptional(buf, msg.inputValidation, (buffer, action) -> buffer.writeRegistryId(action));
    }

    public static ClClickActionPacket decode(PacketBuffer buf) {
        byte flags = buf.readByte();
        ClClickActionPacket packet;
        switch (flags & 3) {
        case 1:
            packet = new ClClickActionPacket(buf.readEnum(PowerClassification.class), buf.readEnum(ActionType.class), 
                    (flags & 4) > 0, buf.readVarInt(), buf.readBlockPos(), buf.readEnum(Direction.class));
            break;
        case 2:
            packet = new ClClickActionPacket(buf.readEnum(PowerClassification.class), buf.readEnum(ActionType.class), 
                    (flags & 4) > 0, buf.readVarInt(), buf.readInt());
            break;
        default: // 0
            packet = new ClClickActionPacket(buf.readEnum(PowerClassification.class), buf.readEnum(ActionType.class), 
                    (flags & 4) > 0, buf.readVarInt());
            break;
        }
        packet.inputValidation = NetworkUtil.readOptional(buf, buffer -> buffer.readRegistryIdSafe(Action.class));
        return packet;
    }

    public static void handle(ClClickActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            if (!player.isSpectator() && player.isAlive()) {
                IPower.getPowerOptional(player, msg.classification).ifPresent(power -> {
                    Entity targetEntity = msg.targetEntityId != -1 ? player.level.getEntity(msg.targetEntityId) : null;
                    ActionTarget target = targetEntity == null ? msg.targetBlock == null ? 
                            ActionTarget.EMPTY
                            : new ActionTarget(msg.targetBlock, msg.blockFace)
                            : new ActionTarget(targetEntity);
                    power.onClickAction(msg.actionType, msg.index, msg.shift, target, msg.inputValidation);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
