package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonSkillLearnPacket {
    private final HamonSkill skill;

    public HamonSkillLearnPacket(HamonSkill skill) {
        this.skill = skill;
    }

    public static void encode(HamonSkillLearnPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.skill);
    }

    public static HamonSkillLearnPacket decode(PacketBuffer buf) {
        return new HamonSkillLearnPacket(buf.readEnum(HamonSkill.class));
    }

    public static void handle(HamonSkillLearnPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            INonStandPower.getNonStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.learnHamonSkill(msg.skill, false);
                    HamonScreen.updateTabs();
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }
}