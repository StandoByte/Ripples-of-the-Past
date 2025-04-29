package com.github.standobyte.jojo.action.stand.effect;

import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.potion.BleedingEffect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

public class GEHealingEffect extends StandEffectInstance {
    public int regenLevel;
    public int fullHpTicks;
    public EffectInstance prevEffect;
    
    public GEHealingEffect() {
        this(ModStandEffects.GE_HEALING.get());
    }
    
    public GEHealingEffect(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    @Override
    protected void start() {}
    
    @Override
    protected void tick() {
        if (!world.isClientSide()) {
            LivingEntity entity = getTargetLiving();
            if (entity == null || !entity.hasEffect(Effects.REGENERATION)) {
                remove();
                return;
            }
            
            if (entity.getHealth() >= BleedingEffect.getMaxHealthWithoutBleeding(entity) && --fullHpTicks <= 0) {
                remove();
            }
        }
    }

    @Override
    protected void stop() {
        if (!world.isClientSide()) {
            LivingEntity entity = getTargetLiving();
            if (entity != null) {
                EffectInstance regenEff = entity.getEffect(Effects.REGENERATION);
                if (regenEff != null && regenEff.getAmplifier() == regenLevel) {
                    entity.removeEffect(Effects.REGENERATION);
                    if (prevEffect != null) {
                        entity.addEffect(prevEffect);
                    }
                }
            }
        }
    }
    
    @Override
    protected boolean needsTarget() {
        return true;
    }
    
    @Override
    protected void writeAdditionalSaveData(CompoundNBT nbt) {
        nbt.putInt("RegenLvl", regenLevel);
        nbt.putInt("FullHpTime", fullHpTicks);
        if (prevEffect != null) {
            nbt.put("PrevEff", prevEffect.save(new CompoundNBT()));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        regenLevel = nbt.getInt("RegenLvl");
        fullHpTicks = nbt.getInt("FullHpTime");
        if (nbt.contains("PrevEff")) {
            prevEffect = EffectInstance.load(nbt.getCompound("PrevEff"));
        }
    }

//    @Override
//    public void writeAdditionalPacketData(PacketBuffer buf, boolean sendingToUser) {
//    }
//
//    @Override
//    public void readAdditionalPacketData(PacketBuffer buf, boolean clientIsUser) {
//    }
}
