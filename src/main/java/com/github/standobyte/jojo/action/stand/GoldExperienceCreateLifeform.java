package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class GoldExperienceCreateLifeform extends StandAction {

    public GoldExperienceCreateLifeform(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (user.level.isClientSide() && getChosenEntityType(ClientUtil.getClientPlayer()) == null) {
            return ActionConditionResult.NEGATIVE;
        }
        
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void clWriteExtraData(PacketBuffer buf) {
        NetworkUtil.writeOptionally(buf, 
                getChosenEntityType(ClientUtil.getClientPlayer()), 
                type -> buf.writeRegistryId(type));
    }
    
    @Nullable
    private static EntityType<?> getChosenEntityType(PlayerEntity player) {
        return player.getCapability(PlayerUtilCapProvider.CAPABILITY).resolve()
                .map(playerData -> playerData.getGEChosenLifeformType()).orElse(null);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (!world.isClientSide() && extraInput != null) {
            EntityType<?> type = (EntityType<?>) NetworkUtil.readOptional(extraInput, 
                    () -> extraInput.readRegistryIdSafe(EntityType.class)).orElse(null);
            if (type != null) {
                Entity lifeFormCreated = type.create(world);
                CompoundNBT nbt = new CompoundNBT();
                nbt.putString("DeathLootTable", "empty");
                lifeFormCreated.load(nbt);
                
                if (lifeFormCreated instanceof MobEntity) {
                    ((MobEntity) lifeFormCreated).finalizeSpawn((ServerWorld) world, 
                            world.getCurrentDifficultyAt(user.blockPosition()), 
                            SpawnReason.COMMAND, null, null);
                    if (lifeFormCreated instanceof SlimeEntity) {
                        CompoundNBT additionalNbt = lifeFormCreated.serializeNBT();
                        additionalNbt.putInt("Size", 0);
                        lifeFormCreated.load(additionalNbt);
                    }
                }
                int ticks = getTicksToCreate(user, power, lifeFormCreated);
                
                Entity performer = user;
                if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
                    StandEntity stand = (StandEntity) power.getStandManifestation();
                    if (stand.isManuallyControlled()) {
                        performer = stand;
                    }
                }
                
                GETransformationEntity tf = new GETransformationEntity(world)
                        .withTransformationTarget(lifeFormCreated)
                        .withDuration(ticks)
                        .withOwner(user);
                if (!user.getItemInHand(Hand.OFF_HAND).isEmpty()) {
                    tf.withTransformationSource(new ItemEntity(world, 0, 0, 0, new ItemStack(user.getItemInHand(Hand.OFF_HAND).getItem())));
                }
                
                Vector3d pos = performer.position();
                Vector3d lookVec = performer.getLookAngle();
                double distScale = lifeFormCreated.getBbWidth() + 1;
                pos = pos.add(lookVec.x * distScale, 0, lookVec.z * distScale);
                tf.moveTo(pos.x, pos.y, pos.z, performer.yRot, 0);
                
                lifeFormCreated.copyPosition(tf);
                lifeFormCreated.setYHeadRot(performer.yRot);
                world.addFreshEntity(tf);
                
                if (!power.isUserCreative()) {
                    power.setCooldownTimer(this, ticks);
                }
            }
            else if (user instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) user).displayClientMessage(new TranslationTextComponent("jojo.message.action_condition.choose_lifeform"), true);
            }
        }
    }
    
    public static int getTicksToCreate(LivingEntity user, IStandPower power, Entity targetEntity) {
        double entityStrength = getAttackStrength(targetEntity);
        float volume = getVolume(targetEntity);
        double standSpeed = 0;
        if (power != null && power.hasPower()) {
            StandStats stats = power.getType().getStats();
            standSpeed = stats.getBaseAttackSpeed() + stats.getDevAttackSpeed(power.getStatsDevelopment());
        }
        
        return (int) (240 / Math.max(standSpeed, 1)
                + MathHelper.ceil(volume * (1 + entityStrength * 0.125) * MathHelper.clamp(100 - standSpeed * 2, 0, 100)));
    }
    
    public float getStaminaCostTicking(IStandPower stand, LivingEntity lifeform) {
        float costMultiplier = getStaminaCostTicking(stand);
        
        return costMultiplier;
    }
    
    public static float getVolume(Entity entity) {
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        return width * width * height;
    }
    
    public static double getAttackStrength(Entity entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            if (living.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
                return living.getAttributeValue(Attributes.ATTACK_DAMAGE);
            }
        }
        return 0;
    }
    
    
    
    public static void onTransformationFinish(Entity entity) {
        if (entity instanceof MobEntity) {
            ((MobEntity) entity).playAmbientSound();
        }
    }
    
    @Override
    public IFormattableTextComponent getTranslatedName(IStandPower power, String key) {
        EntityType<?> chosenEntityType = getChosenEntityType(ClientUtil.getClientPlayer());
        if (chosenEntityType != null) {
            return new TranslationTextComponent(key + ".param", chosenEntityType.getDescription());
        }
        else {
            return super.getTranslatedName(power, key);
        }
    }
}
