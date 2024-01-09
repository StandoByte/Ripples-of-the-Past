package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.effect.GECreatedLifeformEffect;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.entity.ObjectEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class GoldExperienceToothLifeform extends StandEntityActionModifier {
    
    public GoldExperienceToothLifeform(Builder builder) {
        super(builder);
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().isUnlocked(power);
    }
    
    @Override
    public void clWriteExtraData(PacketBuffer buf) {
        NetworkUtil.writeOptionally(buf, 
                GoldExperienceCreateLifeform.getChosenEntityType(ClientUtil.getClientPlayer()), 
                type -> buf.writeRegistryId(type));
    }
    
    @Override
    public void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        super.perform(world, user, power, target, extraInput);
        if (!world.isClientSide() && extraInput != null && power.isActive()) {
            EntityType<?> type = (EntityType<?>) NetworkUtil.readOptional(extraInput, 
                    () -> extraInput.readRegistryIdSafe(EntityType.class)).orElse(null);
            if (type != null
                    && GeneralUtil.orElseFalse(user.getCapability(PlayerUtilCapProvider.CAPABILITY), 
                            cap -> cap.didPlayerMeetEntityType(type))
                    && GoldExperienceChooseLifeform.isValidLifeform(type, world)) {
                
                StandEntity stand = (StandEntity) power.getStandManifestation();
                stand.getCurrentTask().ifPresent(task -> task.getAdditionalData().push(EntityType.class, type));
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
                    EntityType<?> targetType = task.getAdditionalData().popOrNull(EntityType.class);
                    if (targetType != null) {
                        LivingEntity user = userPower.getUser();
                        
                        
                        Entity lifeFormCreated = targetType.create(world);
                        CompoundNBT nbt = new CompoundNBT();
                        nbt.putString("DeathLootTable", "empty");
                        lifeFormCreated.load(nbt);
                        
                        if (lifeFormCreated instanceof MobEntity) {
                            ((MobEntity) lifeFormCreated).finalizeSpawn((ServerWorld) world, 
                                    world.getCurrentDifficultyAt(user.blockPosition()), 
                                    SpawnReason.COMMAND, null, null);
                            for (EquipmentSlotType slot : EquipmentSlotType.values()) {
                                lifeFormCreated.setItemSlot(slot, ItemStack.EMPTY);
                            }
                            if (lifeFormCreated instanceof SlimeEntity) {
                                CompoundNBT additionalNbt = lifeFormCreated.serializeNBT();
                                additionalNbt.putInt("Size", 0);
                                lifeFormCreated.load(additionalNbt);
                            }
                        }
                        
                        int ticks = GoldExperienceCreateLifeform.getTicksToCreate(user, userPower, lifeFormCreated);
                        
                        
                        GETransformationEntity tf = new GETransformationEntity(world);
                        
                        
                        Entity targetEntity = objEntity;
                        MCUtil.cloneEntity(targetEntity).ifPresent(entity -> tf.getTfSourceData()
                                .withEntitySource(entity).withAggroTarget(toothEntity.getOwner()));
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
                EntityType<?> chosenEntityType = GoldExperienceCreateLifeform.getChosenEntityType(ClientUtil.getClientPlayer());
                if (chosenEntityType != null) {
                    return new TranslationTextComponent(key + ".param", chosenEntityType.getDescription());
                }
            }
        }
        
        return super.getTranslatedName(power, key);
    }
}
