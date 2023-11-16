package com.github.standobyte.jojo.action.stand.effect;

import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

public class GECreatedLifeformEffect extends StandEffectInstance {
    private CompoundNBT originalEntityNbt = null;
    private Entity originalEntity;
    
    public GECreatedLifeformEffect(Entity originalEntity) {
        this(ModStandEffects.GE_CREATED_LIFEFORM.get());
        this.originalEntity = originalEntity;
    }
    
    public GECreatedLifeformEffect(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    @Override
    protected void start() {}

    @Override
    protected void tickTarget(LivingEntity target) {}
    
    @Override
    protected void tick() {
        if (!world.isClientSide()) {
            LivingEntity target = getTarget();
            if (target == null) {
                remove();
                return;
            }
            else {
                float staminaCost = ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().getStaminaCostTicking(userPower, target);
                if (!userPower.consumeStamina(staminaCost)) {
                    remove();
                }
            }
        }
    }

    @Override
    protected void stop() {
        if (!world.isClientSide()) {
            Entity target = getTarget();
            if (target != null) {
                if (originalEntity != null) {
                    GETransformationEntity.turnEntityBack(target, originalEntity, user);
                }
                else {
                    target.remove();
                }
            }
        }
    }
    
    @Override
    protected boolean needsTarget() {
        return true;
    }
    
    @Override
    public void onTick() {
        if (originalEntityNbt != null) {
            originalEntity = EntityType.create(originalEntityNbt, world).orElse(null);
            originalEntityNbt = null;
        }
        super.onTick();
    }
    
    protected void writeAdditionalSaveData(CompoundNBT nbt) {
        if (originalEntity != null) {
            CompoundNBT entityNbt = originalEntity.serializeNBT();
            nbt.put("OrigEntity", entityNbt);
        }
    }
    
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        if (nbt.contains("OrigEntity", MCUtil.getNbtId(CompoundNBT.class))) {
            originalEntityNbt = nbt.getCompound("OrigEntity");
        }
    }
}
