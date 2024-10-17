package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.command.configpack.ActionFieldsConfig;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ActionConfigDataPacket {
    private List<Action<?>> actions;
    
    public ActionConfigDataPacket(List<Action<?>> actions) {
        this.actions = actions;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ActionConfigDataPacket> {

        @Override
        public void encode(ActionConfigDataPacket msg, PacketBuffer buf) {
            NetworkUtil.writeCollection(buf, msg.actions, id -> buf.writeResourceLocation(id.getRegistryName()), false);
            for (Action<?> action : msg.actions) {
                action.getOrCreateConfigs().toBuf(buf);
            }
        }

        @Override
        public ActionConfigDataPacket decode(PacketBuffer buf) {
            List<ResourceLocation> actionIds = NetworkUtil.readCollection(buf, PacketBuffer::readResourceLocation);

            List<Action<?>> actions = new ArrayList<>();
            if (!actionIds.isEmpty()) {
                IForgeRegistry<Action<?>> registry = JojoCustomRegistries.ACTIONS.getRegistry();
                for (ResourceLocation id : actionIds) {
                    if (registry.containsKey(id)) {
                        Action<?> action = registry.getValue(id);
                        if (action != null) {
                            actions.add(action);
                            action.getOrCreateConfigs().applyFromBuf(buf);
                        }
                    }
                }
            }
            
            return new ActionConfigDataPacket(actions);
        }
        
        @Override
        public void handle(ActionConfigDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ActionFieldsConfig.getInstance().clHandlePacket(msg.actions);
        }

        @Override
        public Class<ActionConfigDataPacket> getPacketClass() {
            return ActionConfigDataPacket.class;
        }
    }
}
