package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
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
    private final boolean quickAccess;
    private final ActionType actionType;
    private final boolean shift;
    private final int index;
    private Optional<Action<?>> inputValidation = Optional.empty();
    private int targetEntityId = -1;
    @Nullable
    private BlockPos targetBlock;
    @Nullable
    private Direction blockFace;

    private ClClickActionPacket(PowerClassification classification, boolean quickAccess, ActionType actionType, boolean shift, int index) {
        this.classification = classification;
        this.quickAccess = quickAccess;
        this.actionType = actionType;
        this.shift = shift;
        this.index = index;
    }

    private static ClClickActionPacket actionClicked(PowerClassification classification, ActionType actionType, boolean shift, int index) {
        return new ClClickActionPacket(classification, false, actionType, shift, index);
    }

    private static ClClickActionPacket quickAccess(PowerClassification classification, boolean shift) {
        return new ClClickActionPacket(classification, true, null, shift, -1);
    }
    
    private ClClickActionPacket withTarget(RayTraceResult target) {
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
        return this;
    }

    public static ClClickActionPacket actionClicked(PowerClassification classification, ActionType actionType, boolean shift, int index, RayTraceResult target) {
        return actionClicked(classification, actionType, shift, index).withTarget(target);
    }

    public static ClClickActionPacket quickAccess(PowerClassification classification, boolean shift, RayTraceResult target) {
        return quickAccess(classification, shift).withTarget(target);
    }
    
    public ClClickActionPacket validateInput(@Nonnull Action<?> clientClickedAction) {
        this.inputValidation = Optional.of(clientClickedAction);
        return this;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClClickActionPacket> {
    
        @Override
        public void encode(ClClickActionPacket msg, PacketBuffer buf) {
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
            buf.writeBoolean(msg.quickAccess);
            buf.writeEnum(msg.classification);
            if (!msg.quickAccess) {
                buf.writeEnum(msg.actionType);
                buf.writeVarInt(msg.index);
            }
            if (msg.targetBlock != null) {
                buf.writeBlockPos(msg.targetBlock);
                buf.writeEnum(msg.blockFace);
            }
            else if (msg.targetEntityId > 0){
                buf.writeInt(msg.targetEntityId);
            }
            NetworkUtil.writeOptional(buf, msg.inputValidation, action -> buf.writeRegistryId(action));
        }

        @Override
        public ClClickActionPacket decode(PacketBuffer buf) {
            byte flags = buf.readByte();
            boolean quickAccess = buf.readBoolean();
            
            ClClickActionPacket packet = quickAccess ? 
                    ClClickActionPacket.quickAccess(buf.readEnum(PowerClassification.class), (flags & 4) > 0) 
                    : ClClickActionPacket.actionClicked(buf.readEnum(PowerClassification.class), buf.readEnum(ActionType.class), (flags & 4) > 0, buf.readVarInt());
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
            packet.inputValidation = NetworkUtil.readOptional(buf, () -> buf.readRegistryIdSafe(Action.class));
            return packet;
        }

        @Override
        public void handle(ClClickActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            if (!player.isSpectator() && player.isAlive()) {
                IPower.getPowerOptional(player, msg.classification).ifPresent(power -> {
                    Entity targetEntity = msg.targetEntityId != -1 ? player.level.getEntity(msg.targetEntityId) : null;
                    ActionTarget target = targetEntity == null ? msg.targetBlock == null ? 
                            ActionTarget.EMPTY
                            : new ActionTarget(msg.targetBlock, msg.blockFace)
                            : new ActionTarget(targetEntity);
                    if (msg.quickAccess) {
                        power.onClickQuickAccess(msg.shift, target, msg.inputValidation);
                    }
                    else {
                        power.onClickAction(msg.actionType, msg.index, msg.shift, target, msg.inputValidation);
                    }
                });
            }
        }

        @Override
        public Class<ClClickActionPacket> getPacketClass() {
            return ClClickActionPacket.class;
        }
    }
}
