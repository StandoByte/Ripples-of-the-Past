package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.toasts.HamonSkillToast;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrSyncHamonStatsPacket {
    private final int entityId;
    private final boolean showToasts;
    private Stat stat;
    private int strength;
    private int control;
    private float breathing;

    public TrSyncHamonStatsPacket(int entityId, boolean showToasts, int strength, int control, float breathing) {
        this.entityId = entityId;
        this.showToasts = showToasts;
        this.stat = Stat.ALL;
        this.strength = strength;
        this.control = control;
        this.breathing = breathing;
    }

    public TrSyncHamonStatsPacket(int entityId, boolean showToasts, HamonSkill.HamonStat stat, int value) {
        this.entityId = entityId;
        this.showToasts = showToasts;
        switch (stat) {
        case STRENGTH:
            this.stat = Stat.STRENGTH;
            this.strength = value;
            break;
        case CONTROL:
            this.stat = Stat.CONTROL;
            this.control = value;
            break;
        }
    }

    public TrSyncHamonStatsPacket(int entityId, boolean showToasts, float breathing) {
        this.entityId = entityId;
        this.showToasts = showToasts;
        this.stat = Stat.BREATHING;
        this.breathing = breathing;
    }

    public static void encode(TrSyncHamonStatsPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.stat);
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.showToasts);
        switch (msg.stat) {
        case BREATHING:
            buf.writeFloat(msg.breathing);
            break;
        case STRENGTH:
            buf.writeShort(msg.strength);
            break;
        case CONTROL:
            buf.writeShort(msg.control);
            break;
        case ALL:
            buf.writeShort(msg.strength);
            buf.writeShort(msg.control);
            buf.writeFloat(msg.breathing);
            break;
        }
    }

    public static TrSyncHamonStatsPacket decode(PacketBuffer buf) {
        Stat stat = buf.readEnum(Stat.class);
        switch (stat) {
        case BREATHING:
            return new TrSyncHamonStatsPacket(buf.readInt(), buf.readBoolean(), buf.readFloat());
        case STRENGTH:
            return new TrSyncHamonStatsPacket(buf.readInt(), buf.readBoolean(), HamonSkill.HamonStat.STRENGTH, buf.readShort());
        case CONTROL:
            return new TrSyncHamonStatsPacket(buf.readInt(), buf.readBoolean(), HamonSkill.HamonStat.CONTROL, buf.readShort());
        default:
            return new TrSyncHamonStatsPacket(buf.readInt(), buf.readBoolean(), buf.readShort(), buf.readShort(), buf.readFloat());
        }
    }

    public static void handle(TrSyncHamonStatsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                        final boolean showToasts = msg.showToasts && entity == ClientUtil.getClientPlayer();
                        Predicate<HamonSkill> canBeLearned = skill -> !hamon.isSkillLearned(skill) && hamon.canLearnSkill(skill, null);
                        List<HamonSkill> oldSkills = Collections.emptyList();
                        if (showToasts) {
                            oldSkills = Arrays.stream(HamonSkill.values()).filter(skill -> canBeLearned.test(skill)).collect(Collectors.toList());
                        }

                        if (msg.stat == Stat.ALL || msg.stat == Stat.STRENGTH) {
                            hamon.setHamonStatPoints(HamonSkill.HamonStat.STRENGTH, msg.strength, true, true);
                        }
                        if (msg.stat == Stat.ALL || msg.stat == Stat.CONTROL) {
                            hamon.setHamonStatPoints(HamonSkill.HamonStat.CONTROL, msg.control, true, true);
                        }
                        if (msg.stat == Stat.ALL || msg.stat == Stat.BREATHING) {
                            hamon.setBreathingLevel(msg.breathing);
                        }

                        ToastGui toastGui = Minecraft.getInstance().getToasts();
                        if (showToasts) {
                            for (HamonSkill skill : HamonSkill.values()) {
                                if (canBeLearned.test(skill) && !oldSkills.contains(skill)) {
                                    HamonSkillToast.Type toastType = skill.getTechnique() != null ? HamonSkillToast.Type.TECHNIQUE : 
                                        skill.getStat() == HamonSkill.HamonStat.STRENGTH ? HamonSkillToast.Type.STRENGTH : HamonSkillToast.Type.CONTROL;
                                    HamonSkillToast.addOrUpdate(toastGui, toastType, skill);
                                }
                            }
                        }
                    });
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private enum Stat {
        STRENGTH,
        CONTROL,
        BREATHING,
        ALL
    }
}
