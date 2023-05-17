package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonSkillAddPacket {
    private final AbstractHamonSkill skill;

    public HamonSkillAddPacket(AbstractHamonSkill skill) {
        this.skill = skill;
    }
    
    
    
    public static class Handler implements IModPacketHandler<HamonSkillAddPacket> {

        @Override
        public void encode(HamonSkillAddPacket msg, PacketBuffer buf) {
            buf.writeRegistryId(msg.skill);
        }

        @Override
        public HamonSkillAddPacket decode(PacketBuffer buf) {
            return new HamonSkillAddPacket(buf.readRegistryIdSafe(AbstractHamonSkill.class));
        }

        @Override
        public void handle(HamonSkillAddPacket msg, Supplier<NetworkEvent.Context> ctx) {
            LivingEntity player = ClientUtil.getClientPlayer();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.addHamonSkill(player, msg.skill, false, false);
                    HamonScreen.updateTabs();
                });
            });
        }

        @Override
        public Class<HamonSkillAddPacket> getPacketClass() {
            return HamonSkillAddPacket.class;
        }
    }
}