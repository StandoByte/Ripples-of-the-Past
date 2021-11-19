package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ui.screen.HamonScreen;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonTeachersSkillsPacket {
    private static final HamonSkill[] TAUGHT_SKILLS = Arrays.stream(HamonSkill.values()).filter(HamonSkill::requiresTeacher).toArray(HamonSkill[]::new);
    private static final int SKILL_SET_BYTES = ((TAUGHT_SKILLS.length - 1) >> 3) + 1;
    private static final int LAST_BYTE_SHIFT = 8 - ((TAUGHT_SKILLS.length - 1) % 8 + 1);
    
    private final boolean teacherNearby;
    private byte[] skillsByteArray;
    
    public HamonTeachersSkillsPacket() {
        teacherNearby = false;
    }
    
    public HamonTeachersSkillsPacket(byte[] skillsByteArray) {
        teacherNearby = true;
        this.skillsByteArray = skillsByteArray;
    }
    
    public static void encode(HamonTeachersSkillsPacket msg, PacketBuffer buf) {
        buf.writeBoolean(msg.teacherNearby);
        if (msg.teacherNearby) {
            buf.writeByteArray(msg.skillsByteArray);
        }
    }
    
    public static HamonTeachersSkillsPacket decode(PacketBuffer buf) {
        return buf.readBoolean() ? new HamonTeachersSkillsPacket(buf.readByteArray()) : new HamonTeachersSkillsPacket();
    }
    
    public static void handle(HamonTeachersSkillsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.teacherNearby) {
                HamonScreen.setTeacherSkills(decodeSkills(msg.skillsByteArray));
                HamonScreen.updateTabs();
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static byte[] encodeSkills(EnumSet<HamonSkill> skills) {
        byte[] bytes = new byte[SKILL_SET_BYTES];
        int i = 0;
        for (HamonSkill skill : TAUGHT_SKILLS) {
            i = skill.ordinal() / 8;
            bytes[i] <<= 1;
            if (skills.contains(skill)) {
                bytes[i] |= 1;
            }
        }
        bytes[i] <<= LAST_BYTE_SHIFT;
        return bytes;
    }
    
    public static EnumSet<HamonSkill> decodeSkills(byte[] bytes) {
        int i = 0;
        EnumSet<HamonSkill> skills = EnumSet.noneOf(HamonSkill.class);
        for (byte eightSkills : bytes) {
            for (int bit = 0; bit < 8; bit++) {
                if ((eightSkills & 0x10000000) != 0) {
                    skills.add(TAUGHT_SKILLS[i]);
                }
                eightSkills <<= 1;
                i++;
            }
        }
        return skills;
    }

}
