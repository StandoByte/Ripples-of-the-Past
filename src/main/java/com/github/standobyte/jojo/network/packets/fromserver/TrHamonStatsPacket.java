package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.toasts.HamonSkillToast;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonStatsPacket {
    private final int entityId;
    private final boolean showToasts;
    private Stat stat;
    private int strength;
    private int control;
    private float breathing;

    public TrHamonStatsPacket(int entityId, boolean showToasts, int strength, int control, float breathing) {
        this.entityId = entityId;
        this.showToasts = showToasts;
        this.stat = Stat.ALL;
        this.strength = strength;
        this.control = control;
        this.breathing = breathing;
    }

    public TrHamonStatsPacket(int entityId, boolean showToasts, BaseHamonSkill.HamonStat stat, int value) {
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

    public TrHamonStatsPacket(int entityId, boolean showToasts, float breathing) {
        this.entityId = entityId;
        this.showToasts = showToasts;
        this.stat = Stat.BREATHING;
        this.breathing = breathing;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonStatsPacket> {

        @Override
        public void encode(TrHamonStatsPacket msg, PacketBuffer buf) {
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

        @Override
        public TrHamonStatsPacket decode(PacketBuffer buf) {
            Stat stat = buf.readEnum(Stat.class);
            switch (stat) {
            case BREATHING:
                return new TrHamonStatsPacket(buf.readInt(), buf.readBoolean(), buf.readFloat());
            case STRENGTH:
                return new TrHamonStatsPacket(buf.readInt(), buf.readBoolean(), BaseHamonSkill.HamonStat.STRENGTH, buf.readShort());
            case CONTROL:
                return new TrHamonStatsPacket(buf.readInt(), buf.readBoolean(), BaseHamonSkill.HamonStat.CONTROL, buf.readShort());
            default:
                return new TrHamonStatsPacket(buf.readInt(), buf.readBoolean(), buf.readShort(), buf.readShort(), buf.readFloat());
            }
        }

        @Override
        public void handle(TrHamonStatsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                LivingEntity user = (LivingEntity) entity;
                INonStandPower.getNonStandPowerOptional(user).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        final boolean showToasts = msg.showToasts && entity == ClientUtil.getClientPlayer();
                        Predicate<AbstractHamonSkill> canBeLearned = skill -> !hamon.isSkillLearned(skill) && hamon.canLearnSkillTeacherIrrelevant(user, skill).isPositive();
                        List<AbstractHamonSkill> oldSkills = Collections.emptyList();
                        if (showToasts) {
                            oldSkills = JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValues().stream()
                                    .filter(skill -> canBeLearned.test(skill)).collect(Collectors.toList());
                        }

                        if (msg.stat == Stat.ALL || msg.stat == Stat.STRENGTH) {
                            hamon.setHamonStatPoints(BaseHamonSkill.HamonStat.STRENGTH, msg.strength, true, true, true, showToasts);
                        }
                        if (msg.stat == Stat.ALL || msg.stat == Stat.CONTROL) {
                            hamon.setHamonStatPoints(BaseHamonSkill.HamonStat.CONTROL, msg.control, true, true, true, showToasts);
                        }
                        if (msg.stat == Stat.ALL || msg.stat == Stat.BREATHING) {
                            hamon.setBreathingLevel(msg.breathing, showToasts);
                        }

                        ToastGui toastGui = Minecraft.getInstance().getToasts();
                        if (showToasts) {
                            for (AbstractHamonSkill skill : JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValues()) {
                                if (canBeLearned.test(skill) && !oldSkills.contains(skill)) {
                                    HamonSkillToast.Type toastType = skill instanceof BaseHamonSkill ? 
                                            ((BaseHamonSkill) skill).getStat() == BaseHamonSkill.HamonStat.STRENGTH
                                            ? HamonSkillToast.Type.STRENGTH : HamonSkillToast.Type.CONTROL
                                                    : HamonSkillToast.Type.TECHNIQUE;
                                    HamonSkillToast.addOrUpdate(toastGui, toastType, skill);
                                }
                            }
                        }
                    });
                });
            }
        }

        @Override
        public Class<TrHamonStatsPacket> getPacketClass() {
            return TrHamonStatsPacket.class;
        }
    }

    public enum Stat {
        STRENGTH,
        CONTROL,
        BREATHING,
        ALL
    }
}
