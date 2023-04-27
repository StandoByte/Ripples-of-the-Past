package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonSkillType;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonSkillsResetPacket {
    private final HamonSkillType type;

    public HamonSkillsResetPacket(HamonSkillType type) {
        this.type = type;
    }

    public static void encode(HamonSkillsResetPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.type);
    }

    public static HamonSkillsResetPacket decode(PacketBuffer buf) {
        return new HamonSkillsResetPacket(buf.readEnum(HamonSkillType.class));
    }

    public static void handle(HamonSkillsResetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            INonStandPower.getNonStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.resetHamonSkills(msg.type);
                    HamonScreen.updateTabs();
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }
}