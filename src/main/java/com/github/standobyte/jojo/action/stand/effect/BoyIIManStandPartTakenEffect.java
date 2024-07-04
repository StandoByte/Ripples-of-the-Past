package com.github.standobyte.jojo.action.stand.effect;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class BoyIIManStandPartTakenEffect extends StandEffectInstance {
    private StandInstance partsTaken;
    
    public BoyIIManStandPartTakenEffect(StandInstance partsTaken) {
        this(ModStandEffects.BOY_II_MAN_PART_TAKE.get());
        this.partsTaken = partsTaken;
    }
    
    public BoyIIManStandPartTakenEffect(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    public StandInstance getPartsTaken() {
        return partsTaken;
    }
    
    @Override
    protected void start() {}
    
    @Override
    protected void tick() {}

    @Override
    protected void stop() {
        if (!world.isClientSide() && partsTaken != null) {
            LivingEntity target = getTargetLiving();
            if (target != null) {
                IStandPower.getStandPowerOptional(target).ifPresent(power -> {
                    if (!power.hasPower()) {
                        power.giveStandFromInstance(partsTaken, true);
                    }
                    else {
                        power.getStandInstance().ifPresent(stand -> {
                            if (stand.getType() == partsTaken.getType()) {
                                partsTaken.getAllParts().forEach(part -> {
                                    if (!stand.hasPart(part)) {
                                        stand.addPart(part);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
    }
    
    @Override
    protected boolean shouldClearTarget(Entity target, @Nullable LivingEntity targetLiving) {
        return targetLiving != null && targetLiving.isDeadOrDying()
                && !JojoModConfig.getCommonConfigInstance(world.isClientSide()).keepStandOnDeath.get();
    }
    
    @Override
    protected boolean needsTarget() {
        return true;
    }

    

    @Override
    public void writeAdditionalPacketData(PacketBuffer buf, boolean sendingToUser) {
        buf.writeBoolean(partsTaken != null);
        if (partsTaken != null) {
            partsTaken.toBuf(buf);
        }
    }
    
    @Override
    public void readAdditionalPacketData(PacketBuffer buf, boolean clientIsUser) {
        partsTaken = buf.readBoolean() ? StandInstance.fromBuf(buf) : null;
    }

    @Override
    protected void writeAdditionalSaveData(CompoundNBT nbt) {
        if (partsTaken != null) {
            nbt.put("PartsTaken", partsTaken.writeNBT());
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        if (nbt.contains("PartsTaken", MCUtil.getNbtId(CompoundNBT.class))) {
            partsTaken = StandInstance.fromNBT(nbt.getCompound("PartsTaken"));
        }
    }
}
