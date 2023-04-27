package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonSkillLearnPacket {
    private final HamonSkill skill;

    public HamonSkillLearnPacket(HamonSkill skill) {
        this.skill = skill;
    }
    
    
    
    public static class Handler implements IModPacketHandler<HamonSkillLearnPacket> {

        @Override
        public void encode(HamonSkillLearnPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.skill);
        }

        @Override
        public HamonSkillLearnPacket decode(PacketBuffer buf) {
            return new HamonSkillLearnPacket(buf.readEnum(HamonSkill.class));
        }

        @Override
        public void handle(HamonSkillLearnPacket msg, Supplier<NetworkEvent.Context> ctx) {
            INonStandPower.getNonStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.learnHamonSkill(msg.skill, false);
                    HamonScreen.updateTabs();
                });
            });
        }

        @Override
        public Class<HamonSkillLearnPacket> getPacketClass() {
            return HamonSkillLearnPacket.class;
        }
    }
}