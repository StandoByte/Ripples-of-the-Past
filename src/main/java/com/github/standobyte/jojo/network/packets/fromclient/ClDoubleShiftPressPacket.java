package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClDoubleShiftPressPacket {
    
    
    
    public static class Handler implements IModPacketHandler<ClDoubleShiftPressPacket> {

        @Override
        public void encode(ClDoubleShiftPressPacket msg, PacketBuffer buf) {}

        @Override
        public ClDoubleShiftPressPacket decode(PacketBuffer buf) {
            return new ClDoubleShiftPressPacket();
        }
    
        @Override
        public void handle(ClDoubleShiftPressPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setDoubleShiftPress());
        }

        @Override
        public Class<ClDoubleShiftPressPacket> getPacketClass() {
            return ClDoubleShiftPressPacket.class;
        }
        
        public static boolean sendOnPress(PlayerEntity player) {
            return player.isAlive() && player.isOnGround() && GeneralUtil.orElseFalse(
                    INonStandPower.getPlayerNonStandPower(player).getTypeSpecificData(ModPowers.HAMON.get()), 
                    hamon -> hamon.isSkillLearned(ModHamonSkills.LIQUID_WALKING.get()));
        }
    }

}
