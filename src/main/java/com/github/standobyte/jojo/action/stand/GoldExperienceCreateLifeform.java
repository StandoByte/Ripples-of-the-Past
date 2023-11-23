package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.non_stand.HamonOrganismInfusion;
import com.github.standobyte.jojo.action.stand.effect.GECreatedLifeformEffect;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.entity.RoadRollerEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
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
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        switch (target.getType()) {
        case ENTITY:
            // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! use more types of inanimate entities as targets
            Entity entity = target.getEntity();
            return ActionConditionResult.noMessage(
                    entity instanceof TNTEntity || 
                    entity instanceof RoadRollerEntity || 
                    entity instanceof EnderCrystalEntity || 
                    entity instanceof BoatEntity);
        case BLOCK:
            if (!power.isUserCreative()) {
                World world = user.level;
                BlockPos blockPos = target.getBlockPos();
                BlockState blockState = world.getBlockState(blockPos);
                
                float blockHardness = blockState.getDestroySpeed(world, blockPos);
                if (blockHardness < 0) {
                    return ActionConditionResult.NEGATIVE;
                }
            }
        default:
            break;
        }
        
        return super.checkTarget(target, user, power);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (user.level.isClientSide() && getChosenEntityType(ClientUtil.getClientPlayer()) == null) {
            return ActionConditionResult.NEGATIVE;
        }
        
        boolean hasAnItem = false;
        boolean itemFits = false;
        boolean hasABlock = false;
        boolean blockFits = false;
        
        ItemStack item = user.getItemInHand(Hand.OFF_HAND);
        if (!item.isEmpty()) {
            hasAnItem = true;
            itemFits = !HamonUtil.isItemLivingMatter(item);
        }
        
        if (target.getType() == TargetType.BLOCK) {
            hasABlock = true;
            BlockPos blockPos = target.getBlockPos();
            BlockState blockState = user.level.getBlockState(blockPos);
            blockFits = !HamonOrganismInfusion.isBlockLiving(blockState);
        }
        
        if (!hasAnItem && !hasABlock) {
            return conditionMessage("ge_lifeform_material");
        }
        if (!itemFits && !blockFits) {
            if (hasAnItem) {
                return conditionMessage("ge_lifeform_material_item");
            }
            else {
                return conditionMessage("ge_lifeform_material_block");
            }
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
    public static EntityType<?> getChosenEntityType(PlayerEntity player) {
//        ItemStack heldItem = player.getItemInHand(Hand.OFF_HAND);
//        if (!heldItem.isEmpty()) {
//            Item item = heldItem.getItem();
//            if (item == Items.SLIME_BALL || item == Items.SLIME_BLOCK) {
//                return EntityType.SLIME;
//            }
//            if (item == Items.MAGMA_CREAM || item == Items.MAGMA_BLOCK) {
//                return EntityType.MAGMA_CUBE;
//            }
//        }
//
//        if (target.getType() == TargetType.BLOCK) {
//            BlockPos blockPos = target.getBlockPos();
//            BlockState blockState = world.getBlockState(blockPos);
//            Block block = blockState.getBlock();
//            if (block == Blocks.SLIME_BLOCK) {
//                return EntityType.SLIME;
//            }
//            if (block == Blocks.MAGMA_BLOCK) {
//                return EntityType.MAGMA_CUBE;
//            }
//        }
        
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
                
                GETransformationEntity tf = new GETransformationEntity(world);
                
                boolean tfTargetFound = false;
                if (target.getType() == TargetType.ENTITY) {
                    Entity targetEntity = target.getEntity();
                    tf.getTfSourceData().withEntitySource(targetEntity);
                    targetEntity.remove();
                    tfTargetFound = true;
                    
                    Vector3d pos = targetEntity.position();
                    tf.moveTo(pos.x, pos.y, pos.z, targetEntity.yRot, targetEntity.xRot);
                    
                    if (targetEntity.isOnFire()) {
                        tf.setSecondsOnFire((targetEntity.getRemainingFireTicks() + 19) / 20);
                    }
                }
                if (!tfTargetFound && !user.getItemInHand(Hand.OFF_HAND).isEmpty()) {
                    ItemStack heldItem = user.getItemInHand(Hand.OFF_HAND);
                    Entity itemEntity;
                    ItemStack transformedItem = heldItem.copy();
                    transformedItem.setCount(1);
                    if (heldItem.getItem() instanceof ThrowablePotionItem) {
                        PotionEntity potionEntity = new PotionEntity(world, user);
                        potionEntity.setItem(transformedItem);
                        itemEntity = potionEntity;
                    }
                    else {
                        itemEntity = new ItemEntity(world, 0, 0, 0, transformedItem);
                    }
                    if (!power.isUserCreative()) heldItem.shrink(1);
                    
                    tf.getTfSourceData().withEntitySource(itemEntity);
                    tfTargetFound = true;
                    
                    Vector3d pos = performer.position();
                    Vector3d lookVec = performer.getLookAngle();
                    double distScale = lifeFormCreated.getBbWidth() + 1;
                    pos = pos.add(lookVec.x * distScale, 0, lookVec.z * distScale);
                    tf.moveTo(pos.x, pos.y, pos.z, performer.yRot, 0);
                }
                if (!tfTargetFound && target.getType() == TargetType.BLOCK) {
                    BlockPos blockPos = target.getBlockPos();
                    BlockState blockState = world.getBlockState(blockPos);
                    
                    tf.getTfSourceData().withBlockSource(blockState, blockPos);
                    world.removeBlock(blockPos, false);
                    tfTargetFound = true;
                    
                    tf.moveTo(blockPos, performer.yRot, 0);
                }
                
                if (tfTargetFound) {
                    tf.withTransformationTarget(lifeFormCreated)
                    .withDuration(ticks)
                    .withOwner(user);
                    
                    GECreatedLifeformEffect effect = new GECreatedLifeformEffect();
                    effect.withStand(power).withTarget(tf);
                    effect.setSource(tf.getTfSourceData());
                    power.getContinuousEffects().addEffect(effect);
                    
                    lifeFormCreated.copyPosition(tf);
                    lifeFormCreated.setYHeadRot(lifeFormCreated.yRot);
                    world.addFreshEntity(tf);
                    
                    if (!power.isUserCreative()) {
                        power.setCooldownTimer(this, ticks);
                    }
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
//        float costMultiplier = getStaminaCostTicking(stand);
        
        return 0;
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
