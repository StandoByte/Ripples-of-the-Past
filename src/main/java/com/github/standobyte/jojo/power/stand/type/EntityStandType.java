package com.github.standobyte.jojo.power.stand.type;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.actions.StandAction;
import com.github.standobyte.jojo.action.actions.StandEntityHeavyAttack;
import com.github.standobyte.jojo.action.actions.StandEntityLightAttack;
import com.github.standobyte.jojo.action.actions.StandEntityMeleeBarrage;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.power.stand.IStandManifestation;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.stats.StandStats;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent;

public class EntityStandType<T extends StandStats> extends StandType<T> {
    private final Supplier<? extends StandEntityType<? extends StandEntity>> entityTypeSupplier;
    private final boolean hasHeavyAttack;
    private final boolean hasFastAttack;

    public EntityStandType(int tier, int color, ITextComponent partName, 
            StandAction[] attacks, StandAction[] abilities, 
            Class<T> statsClass, T defaultStats, 
            Supplier<? extends StandEntityType<? extends StandEntity>> entityTypeSupplier) {
        super(tier, color, partName, attacks, abilities, statsClass, defaultStats);
        this.entityTypeSupplier = entityTypeSupplier;
        
        hasHeavyAttack = Arrays.stream(attacks).anyMatch(
                attack -> attack instanceof StandEntityHeavyAttack || attack.getShiftVariationIfPresent() instanceof StandEntityHeavyAttack);
        hasFastAttack = Arrays.stream(attacks).anyMatch(
                attack -> attack instanceof StandEntityLightAttack || attack instanceof StandEntityMeleeBarrage);
    }

    public StandEntityType<? extends StandEntity> getEntityType() {
        return entityTypeSupplier.get();
    }
    
    @Override
    public RayTraceResult clientHitResult(IStandPower power, Entity cameraEntity, RayTraceResult vanillaHitResult) {
        if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            if (JojoModUtil.isAnotherEntityTargeted(vanillaHitResult, stand)) {
                return super.clientHitResult(power, cameraEntity, vanillaHitResult);
            }

            RayTraceResult standHitResult = stand.precisionRayTrace(cameraEntity);

            if (JojoModUtil.isAnotherEntityTargeted(standHitResult, stand)) {
                return standHitResult;
            }
        }
        return super.clientHitResult(power, cameraEntity, vanillaHitResult);
    }
    
    @Override
    public boolean usesStamina() {
        return true;
    }
    
    @Override
    public float getStaminaRegen(IStandPower power) {
        if (power.isActive()) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            return stand.hasTask() && !stand.getCurrentTaskAction().canStaminaRegen(power, stand) ? 0 : 1F;
        }
        return super.getStaminaRegen(power);
    }

    @Override
    public boolean usesResolve() {
        return true;
    }
    
    @Override
    public void onNewResolveLevel(IStandPower power) {
        super.onNewResolveLevel(power);
        if (power.isActive()) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            stand.modifiersFromResolveLevel(power.getStatsDevelopment());
        }
    }
    
    @Override
    public boolean usesStandComboMechanic() {
        return hasHeavyAttack && hasFastAttack;
    }
    
    @Override
    public void toggleSummon(IStandPower standPower) {
        if (!standPower.isActive()) {
            summon(standPower.getUser(), standPower, false);
        }
        else {
            StandEntity standEntity = (StandEntity) standPower.getStandManifestation();
            if (standEntity.isArmsOnlyMode()) {
                standEntity.fullSummonFromArms();
                triggerAdvancement(standPower, standPower.getStandManifestation());
            }
            else {
                unsummon(standPower.getUser(), standPower);
            }
        }
    }

    @Override
    public boolean summon(LivingEntity user, IStandPower standPower, boolean withoutNameVoiceLine) {
        return summon(user, standPower, entity -> {}, withoutNameVoiceLine);
    }

    public boolean summon(LivingEntity user, IStandPower standPower, Consumer<StandEntity> beforeTheSummon, boolean withoutNameVoiceLine) {
        if (!super.summon(user, standPower, withoutNameVoiceLine)) {
            return false;
        }
        if (!user.level.isClientSide()) {
            StandEntity standEntity = getEntityType().create(user.level);
            standEntity.copyPosition(user);
            standPower.setStandManifestation(standEntity);
            beforeTheSummon.accept(standEntity);
            user.level.addFreshEntity(standEntity);
            
            List<Effect> effectsToCopy = standEntity.getEffectsSharedToStand();
            for (Effect effect : effectsToCopy) {
                EffectInstance userEffectInstance = user.getEffect(effect);
                if (userEffectInstance != null) {
                    standEntity.addEffect(new EffectInstance(userEffectInstance));
                }
            }
            
            standEntity.playStandSummonSound();
            
            PacketManager.sendToClientsTrackingAndSelf(new TrSetStandEntityPacket(user.getId(), standEntity.getId()), user);
            
            triggerAdvancement(standPower, standPower.getStandManifestation());
        }
        return true;
    }
    
    protected void triggerAdvancement(IStandPower standPower, IStandManifestation stand) {
        if (stand instanceof StandEntity && !((StandEntity) stand).isArmsOnlyMode()) {
            super.triggerAdvancement(standPower, stand);
        }
    }

    @Override
    public void unsummon(LivingEntity user, IStandPower standPower) {
        if (!user.level.isClientSide()) {
            StandEntity standEntity = ((StandEntity) standPower.getStandManifestation());
            if (standEntity != null) {
                standEntity.retractStand(true);
            }
        }
    }

    @Override
    public void tickUser(LivingEntity user, IStandPower power) {
        super.tickUser(user, power);
        IStandManifestation stand = power.getStandManifestation();
        if (stand instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) stand;
            if (standEntity.level != user.level) {
                forceUnsummon(user, power);
            }
        }
    }

    @Override
    public void forceUnsummon(LivingEntity user, IStandPower standPower) {
        if (!user.level.isClientSide()) {
            IStandManifestation stand = standPower.getStandManifestation();
            if (stand instanceof StandEntity) {
                StandEntity standEntity = (StandEntity) stand;
                standPower.setStandManifestation(null);
                PacketManager.sendToClientsTrackingAndSelf(new TrSetStandEntityPacket(user.getId(), -1), user);
                standEntity.remove();
            }
        }
    }
    

    
    public static void giveSharedEffectsFromUser(PotionAddedEvent event) {
        IStandPower.getStandPowerOptional(event.getEntityLiving()).ifPresent(power -> {
            if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
                EffectInstance effectInstance = event.getPotionEffect();
                StandEntity stand = (StandEntity) power.getStandManifestation();
                if (stand.getEffectsSharedToStand().contains(effectInstance.getEffect())) {
                    stand.addEffect(new EffectInstance(effectInstance));
                }
            }
        });
    }
}
