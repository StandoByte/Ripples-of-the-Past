package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.effect.GECreatedLifeformEffect;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.stand.ge.EntityTypeIcon;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.entity.ObjectEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.monster.PatrollerEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class GoldExperienceToothLifeform extends StandEntityActionModifier {
    
    public GoldExperienceToothLifeform(Builder builder) {
        super(builder);
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().isUnlocked(power);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        int mobsCreated = (int) StandEffectsTracker.getEffectsOfType(power, ModStandEffects.GE_CREATED_LIFEFORM.get(), -1).count();
        if (mobsCreated >= 16) {
            return conditionMessage("ge_too_many_mobs");
        }
        
        return super.checkSpecificConditions(user, power, target);
    }
    
    public static ObjectEntity.Type getToothObject(LivingEntity punchTarget) {
        EntityType<?> type = punchTarget.getType();
        if (type == EntityType.PLAYER
                || punchTarget instanceof AbstractSkeletonEntity
                || punchTarget instanceof ZombieEntity
                || punchTarget instanceof AbstractVillagerEntity
                || punchTarget instanceof AbstractPiglinEntity
                || punchTarget instanceof PatrollerEntity
                || type == EntityType.BAT
                || punchTarget instanceof AbstractHorseEntity
                || punchTarget instanceof CowEntity
                || punchTarget instanceof FoxEntity
                || punchTarget instanceof HoglinEntity
                || punchTarget instanceof OcelotEntity
                || punchTarget instanceof PandaEntity
                || punchTarget instanceof PigEntity
                || punchTarget instanceof PolarBearEntity
                || punchTarget instanceof RabbitEntity
                || punchTarget instanceof SheepEntity
                || punchTarget instanceof CatEntity
                || punchTarget instanceof WolfEntity
                || punchTarget instanceof WaterMobEntity) {
            return ObjectEntity.Type.TOOTH;
        }
        
        return null;
    }
    
    @Override
    public void clWriteExtraData(PacketBuffer buf) {
        NetworkUtil.writeOptionally(buf, 
                GoldExperienceCreateLifeform.getChosenEntityType(ClientUtil.getClientPlayer()), 
                EntitySubtype::toBuf);
    }
    
    @Override
    public void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        super.perform(world, user, power, target, extraInput);
        if (!world.isClientSide() && extraInput != null && power.isActive()) {
            EntitySubtype<?> type = NetworkUtil.readOptional(extraInput, EntitySubtype::fromBuf).orElse(null);
            if (type != null
                    && GeneralUtil.orElseFalse(user.getCapability(PlayerUtilCapProvider.CAPABILITY), 
                            cap -> cap.metEntityType(type))
                    && GoldExperienceChooseLifeform.isValidLifeform(type, world)) {
                
                StandEntity stand = (StandEntity) power.getStandManifestation();
                stand.getCurrentTask().ifPresent(task -> task.getAdditionalData().push(EntitySubtype.class, type));
            }
        }
    }
    
    @Override
    public void standTickRecovery(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        boolean triggerEffect = task.getTicksLeft() <= 1;
        if (task.getAdditionalData().isEmpty(TriggeredFlag.class)) {
            if (!world.isClientSide() && !task.getAdditionalData().isEmpty(Integer.class) && triggerEffect) {
                int toothEntityId = task.getAdditionalData().pop(Integer.class);
                Entity objEntity = world.getEntity(toothEntityId);
                if (objEntity instanceof ObjectEntity) {
                    ObjectEntity toothEntity = (ObjectEntity) objEntity;
                    EntitySubtype<?> targetType = task.getAdditionalData().popOrNull(EntitySubtype.class);
                    if (targetType != null) {
                        LivingEntity user = userPower.getUser();
                        
                        Entity lifeFormCreated = GoldExperienceCreateLifeform.createEntity(targetType, world, user);
                        int ticks = GoldExperienceCreateLifeform.getTicksToCreate(user, userPower, lifeFormCreated);
                        
                        GETransformationEntity tf = new GETransformationEntity(world);
                        
                        
                        Entity targetEntity = objEntity;
                        MCUtil.cloneEntity(targetEntity).ifPresent(entity -> tf.getTfSourceData()
                                .withEntitySource(entity).withFollowTarget(toothEntity.getOwner()));
                        targetEntity.remove();

                        Vector3d pos = targetEntity.position();
                        tf.moveTo(pos.x, pos.y, pos.z, targetEntity.yRot, targetEntity.xRot);

                        if (targetEntity.isOnFire()) {
                            tf.setSecondsOnFire((targetEntity.getRemainingFireTicks() + 19) / 20);
                        }
                        tf.setDeltaMovement(targetEntity.getDeltaMovement());
                        
                        
                        tf.withTransformationTarget(lifeFormCreated)
                        .withDuration(ticks)
                        .withOwner(user);
                        
                        

                        GECreatedLifeformEffect effect = new GECreatedLifeformEffect();
                        effect.withStand(userPower).withTarget(tf);
                        effect.setSource(tf.getTfSourceData());
                        userPower.getContinuousEffects().addEffect(effect);

                        lifeFormCreated.copyPosition(tf);
                        lifeFormCreated.setYHeadRot(lifeFormCreated.yRot);
                        world.addFreshEntity(tf);

//                        if (!userPower.isUserCreative()) {
//                            int cooldown = Math.max(ticks / 2, 1);
//                            tf.actionCooldown = cooldown;
//                            userPower.setCooldownTimer(this, cooldown);
//                        }
                    }
//                    else if (user instanceof ServerPlayerEntity) {
//                        ((ServerPlayerEntity) user).displayClientMessage(new TranslationTextComponent("jojo.message.action_condition.choose_lifeform"), true);
//                    }
                    
                    
                }
            }
            
            if (triggerEffect) {
                task.getAdditionalData().push(TriggeredFlag.class, new TriggeredFlag());
            }
        }
    }
    
    @Override
    public IFormattableTextComponent getTranslatedName(IStandPower power, String key) {
        if (power.isActive()) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            if (stand.getCurrentTask().map(task -> task.getTarget().getType() == TargetType.ENTITY).orElse(false)) {
                EntitySubtype<?> chosenEntityType = GoldExperienceCreateLifeform.getChosenEntityType(ClientUtil.getClientPlayer());
                if (chosenEntityType != null) {
                    return new TranslationTextComponent(key + ".param", chosenEntityType.getDescription());
                }
            }
        }
        
        return super.getTranslatedName(power, key);
    }
    
    @Override
    public void renderActionIcon(MatrixStack matrixStack, IStandPower power, float x, float y) {
        EntitySubtype<?> selectedMob = GoldExperienceCreateLifeform.getChosenEntityType(ClientUtil.getClientPlayer());
        if (selectedMob != null) {
            EntityTypeIcon.renderIcon(selectedMob, matrixStack, x, y);
        }
        else {
            super.renderActionIcon(matrixStack, power, x, y);
        }
    }
}
