package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonSkillRemovePacket {
    private final AbstractHamonSkill skill;
    
    public HamonSkillRemovePacket(AbstractHamonSkill skill) {
        this.skill = skill;
    }
    
    
    
    public static class Handler implements IModPacketHandler<HamonSkillRemovePacket> {
        
        @Override
        public void encode(HamonSkillRemovePacket msg, PacketBuffer buf) {
            buf.writeRegistryId(msg.skill);
        }
        
        @Override
        public HamonSkillRemovePacket decode(PacketBuffer buf) {
            return new HamonSkillRemovePacket(buf.readRegistryIdSafe(AbstractHamonSkill.class));
        }
        
        @Override
        public void handle(HamonSkillRemovePacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ClientUtil.getClientPlayer();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.removeHamonSkill(msg.skill);
                    HamonScreen.updateTabs();
                });
            });
        }
        
        @Override
        public Class<HamonSkillRemovePacket> getPacketClass() {
            return HamonSkillRemovePacket.class;
        }
    }
}