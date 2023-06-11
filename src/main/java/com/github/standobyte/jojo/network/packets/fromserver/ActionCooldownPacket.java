package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ActionCooldownPacket {
    private final int entityId;
    private final PowerClassification classification;
    private final boolean resetAll;
    private final Action<?> action;
    private final int value;
    private final int totalCooldown;

    public ActionCooldownPacket(int entityId, PowerClassification classification, Action<?> action, int value) {
        this(entityId, classification, action, value, value);
    }

    public ActionCooldownPacket(int entityId, PowerClassification classification, Action<?> action, int value, int totalCooldown) {
        this(entityId, classification, action, value, totalCooldown, false);
    }
    
    public static ActionCooldownPacket resetAll(int entityId, PowerClassification classification) {
        return new ActionCooldownPacket(entityId, classification, null, 0, 0, true);
    }
    
    private ActionCooldownPacket(int entityId, PowerClassification classification, Action<?> action, int value, int totalCooldown, boolean resetAll) {
        this.entityId = entityId;
        this.classification = classification;
        this.resetAll = resetAll;
        this.action = action;
        this.value = value;
        this.totalCooldown = totalCooldown;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ActionCooldownPacket> {

        public void encode(ActionCooldownPacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.resetAll);
            buf.writeInt(msg.entityId);
            buf.writeEnum(msg.classification);
            if (!msg.resetAll) {
                buf.writeRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry(), msg.action);
                buf.writeVarInt(msg.value);
                buf.writeVarInt(msg.totalCooldown);
            }
        }
    
        public ActionCooldownPacket decode(PacketBuffer buf) {
            boolean resetAll = buf.readBoolean();
            if (resetAll) {
                return resetAll(buf.readInt(), buf.readEnum(PowerClassification.class));
            }
            return new ActionCooldownPacket(buf.readInt(), buf.readEnum(PowerClassification.class), 
                    buf.readRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry()), buf.readVarInt(), buf.readVarInt());
        }
    
        public void handle(ActionCooldownPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                IPower.getPowerOptional((LivingEntity) entity, msg.classification).ifPresent(power -> {
                    if (msg.resetAll) {
                        power.getCooldowns().resetCooldowns();
                    }
                    else {
                        power.updateCooldownTimer(msg.action, msg.value, msg.totalCooldown);
                    }
                });
            }
        }

        @Override
        public Class<ActionCooldownPacket> getPacketClass() {
            return ActionCooldownPacket.class;
        }
    }
}
