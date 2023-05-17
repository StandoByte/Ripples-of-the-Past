package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Collection;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonTeachersSkillsPacket {
    private final boolean teacherNearby;
    private Collection<AbstractHamonSkill> skills;
    
    public HamonTeachersSkillsPacket() {
        teacherNearby = false;
    }
    
    public HamonTeachersSkillsPacket(Collection<AbstractHamonSkill> skills) {
        teacherNearby = true;
        this.skills = skills;
    }
    
    
    
    public static class Handler implements IModPacketHandler<HamonTeachersSkillsPacket> {

        @Override
        public void encode(HamonTeachersSkillsPacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.teacherNearby);
            if (msg.teacherNearby) {
                NetworkUtil.writeCollection(buf, msg.skills, skill -> buf.writeRegistryId(skill), false);
            }
        }

        @Override
        public HamonTeachersSkillsPacket decode(PacketBuffer buf) {
            return buf.readBoolean() ? new HamonTeachersSkillsPacket(NetworkUtil.readCollection(buf, 
                    () -> buf.readRegistryIdSafe(AbstractHamonSkill.class))) : new HamonTeachersSkillsPacket();
        }

        @Override
        public void handle(HamonTeachersSkillsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            if (msg.teacherNearby) {
                HamonScreen.setTeacherSkills(msg.skills);
                HamonScreen.updateTabs();
            }
        }

        @Override
        public Class<HamonTeachersSkillsPacket> getPacketClass() {
            return HamonTeachersSkillsPacket.class;
        }
    }

}
