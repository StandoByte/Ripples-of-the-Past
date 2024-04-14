package com.github.standobyte.jojo.action.stand;

import java.util.Stack;

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
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BucketItem;
import net.minecraft.item.FishBucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;

public class GoldExperienceCreateLifeform extends StandAction {

    public GoldExperienceCreateLifeform(StandAction.Builder builder) {
        super(builder);
        voiceLineDelay = Integer.MAX_VALUE;
    }
    
    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        switch (target.getType()) {
        case ENTITY:
            // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! use more types of inanimate entities as targets?
            Entity entity = target.getEntity();
            return ActionConditionResult.noMessage(
                    entity instanceof TNTEntity || 
                    entity instanceof RoadRollerEntity || 
                    entity instanceof EnderCrystalEntity || 
                    entity instanceof BoatEntity);
        case BLOCK:
            if (!JojoModUtil.breakingBlocksEnabled(user.level)) {
                return ActionConditionResult.NEGATIVE;
            }
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
        if (target.getType() == TargetType.ENTITY) {
            return ActionConditionResult.POSITIVE;
        }
        
        int mobsCreated = (int) StandEffectsTracker.getEffectsOfType(power, ModStandEffects.GE_CREATED_LIFEFORM.get(), -1).count();
        if (mobsCreated >= 16) {
            return conditionMessage("ge_too_many_mobs");
        }
        
        boolean hasAnItem = false;
        boolean itemFits = false;
        boolean hasABlock = false;
        boolean blockFits = false;
        boolean canUseBlock = JojoModUtil.breakingBlocksEnabled(user.level);
        
        ItemStack item = user.getItemInHand(Hand.OFF_HAND);
        if (!item.isEmpty()) {
            hasAnItem = true;
            itemFits = canGiveLifeTo(item);
        }
        
        if (canUseBlock && target.getType() == TargetType.BLOCK) {
            hasABlock = true;
            BlockPos blockPos = target.getBlockPos();
            BlockState blockState = user.level.getBlockState(blockPos);
            blockFits = !HamonOrganismInfusion.isBlockLiving(blockState);
        }
        
        if (!hasAnItem && !hasABlock) {
            return canUseBlock ? conditionMessage("ge_lifeform_material") : conditionMessage("ge_lifeform_material_only_item");
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
    
    public static boolean canGiveLifeTo(ItemStack item) {
        return !HamonUtil.isItemLivingMatter(item) || item.getItem() instanceof FishBucketItem;
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
                .map(playerData -> playerData.getGELifeformsUIState().getGEChosenLifeformType()).orElse(null);
    }
    
    public static Entity createEntity(EntityType<?> type, World world, LivingEntity standUser) {
        Entity lifeFormCreated = type.create(world);
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("DeathLootTable", "empty");
        lifeFormCreated.load(nbt);
        
        if (lifeFormCreated instanceof MobEntity) {
            ((MobEntity) lifeFormCreated).finalizeSpawn((ServerWorld) world, 
                    world.getCurrentDifficultyAt(standUser.blockPosition()), 
                    SpawnReason.COMMAND, null, null);
            for (EquipmentSlotType slot : EquipmentSlotType.values()) {
                lifeFormCreated.setItemSlot(slot, ItemStack.EMPTY);
            }
            
            if (lifeFormCreated instanceof AgeableEntity) {
                standUser.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(playerData -> {
                    playerData.animalAgeCd += 3000;
                    ((AgeableEntity) lifeFormCreated).setAge(Math.max(playerData.animalAgeCd - 3000, 0));
                });
                if (lifeFormCreated instanceof AbstractHorseEntity) {
                    ((AbstractHorseEntity) lifeFormCreated).setTemper(0);
                }
            }
            else if (lifeFormCreated instanceof SlimeEntity) {
                CompoundNBT additionalNbt = lifeFormCreated.serializeNBT();
                additionalNbt.putInt("Size", 0);
                lifeFormCreated.load(additionalNbt);
            }
        }
        
        return lifeFormCreated;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (!world.isClientSide() && extraInput != null) {
            EntityType<?> type = (EntityType<?>) NetworkUtil.readOptional(extraInput, 
                    () -> extraInput.readRegistryIdSafe(EntityType.class)).orElse(null);
            if (type != null
                    && GeneralUtil.orElseFalse(user.getCapability(PlayerUtilCapProvider.CAPABILITY), 
                            cap -> cap.didPlayerMeetEntityType(type))
                    && GoldExperienceChooseLifeform.isValidLifeform(type, world)) {
                
                Entity lifeFormCreated = createEntity(type, world, user);
                int ticks = getTicksToCreate(user, power, lifeFormCreated);
                
                Entity performer = getControlledEntity(user, power);
                GETransformationEntity tf = new GETransformationEntity(world);
                
                ITextComponent customName = null;
                boolean tfTargetFound = false;
                if (target.getType() == TargetType.ENTITY) {
                    Entity targetEntity = target.getEntity();
                    MCUtil.cloneEntity(targetEntity).ifPresent(entity -> tf.getTfSourceData().withEntitySource(entity));
                    targetEntity.remove();
                    tfTargetFound = true;
                    
                    Vector3d pos = targetEntity.position();
                    tf.moveTo(pos.x, pos.y, pos.z, targetEntity.yRot, targetEntity.xRot);
                    
                    if (targetEntity.isOnFire()) {
                        tf.setSecondsOnFire((targetEntity.getRemainingFireTicks() + 19) / 20);
                    }
                    tf.setDeltaMovement(targetEntity.getDeltaMovement());
                }
                if (!tfTargetFound) {
                    ItemStack heldItem = user.getItemInHand(Hand.OFF_HAND);
                    if (!heldItem.isEmpty() && canGiveLifeTo(heldItem)) {
                        Entity itemEntity;
                        ItemStack transformedItem;
                        if (heldItem.getItem() instanceof BucketItem) {
                            BucketItem bucketType = (BucketItem) heldItem.getItem();
                            Fluid fluid = bucketType.getFluid();
                            transformedItem = new ItemStack(fluid.getBucket());
                            bucketType.checkExtraContent(world, heldItem, performer.blockPosition());
                        }
                        else {
                            transformedItem = heldItem.copy();
                        }
                        transformedItem.setCount(1);
                        if (heldItem.getItem() instanceof ThrowablePotionItem) {
                            PotionEntity potionEntity = new PotionEntity(world, user);
                            potionEntity.setItem(transformedItem);
                            itemEntity = potionEntity;
                        }
                        else if (heldItem.getItem() == Items.ENDER_PEARL) {
                            EnderPearlEntity pearlEntity = new EnderPearlEntity(world, user);
                            itemEntity = pearlEntity;
                        }
                        else {
                            itemEntity = new ItemEntity(world, 0, 0, 0, transformedItem);
                        }
                        if (heldItem.hasCustomHoverName()) {
                            customName = heldItem.getHoverName();
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
                }
                if (!tfTargetFound && target.getType() == TargetType.BLOCK
                        && JojoModUtil.breakingBlocksEnabled(user.level)) {
                    BlockPos blockPos = target.getBlockPos();
                    BlockState blockState = world.getBlockState(blockPos);
                    
                    if (!HamonOrganismInfusion.isBlockLiving(blockState)) {
                        TileEntity tileEntity = world.getBlockEntity(blockPos);
                        
                        if (tileEntity instanceof IInventory) {
                            KEEP_ITEMS.add(tileEntity);
                        }
                        world.removeBlock(blockPos, false);
                        KEEP_ITEMS.remove(tileEntity);
                        
                        tf.getTfSourceData().withBlockSource(blockState, blockPos, tileEntity);
                        tfTargetFound = true;
                        
                        tf.moveTo(blockPos, performer.yRot, 0);
                    }
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
                    if (customName != null) {
                        lifeFormCreated.setCustomName(customName);
                    }
                    world.addFreshEntity(tf);
                    
                    if (!power.isUserCreative()) {
                        int cooldown = Math.max(ticks / 2, 1);
                        tf.actionCooldown = cooldown;
                        power.setCooldownTimer(this, cooldown);
                    }
                }
            }
            else if (user instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) user).displayClientMessage(new TranslationTextComponent("jojo.message.action_condition.choose_lifeform"), true);
            }
        }
    }
    
    public static final Stack<TileEntity> KEEP_ITEMS = new Stack<>();
    
    
    
    public static int getTicksToCreate(LivingEntity user, IStandPower power, Entity targetEntity) {
        double entityStrength = getAttackStrength(targetEntity);
        float volume = getVolume(targetEntity);
        double standSpeed = 0;
        if (power != null && power.hasPower()) {
            StandStats stats = power.getType().getStats();
            standSpeed = stats.getBaseAttackSpeed() + stats.getDevAttackSpeed(power.getStatsDevelopment());
        }
        
        double value = 240 / Math.max(standSpeed, 1)
                + MathHelper.ceil(volume * (1 + entityStrength * 0.125) * MathHelper.clamp(100 - standSpeed * 2, 0, 100));
        if (correctBiome(targetEntity, user.level, user.blockPosition())) {
            value *= 0.6;
        }
        return (int) value;
    }
    
    public static boolean correctBiome(Entity mobInstance, World world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        return biome.getMobSettings().getMobs(mobInstance.getClassification(false))
                .stream().anyMatch(spawners -> spawners.type == mobInstance.getType());
    }
    
    public float getStaminaCostTicking(IStandPower stand, Entity lifeform) {
        float baseCost = getStaminaCostTicking(stand);
        
        if (lifeform != null) {
            double entityStrength = getAttackStrength(lifeform);
            float volume = getVolume(lifeform);
            
            float entityMultiplier = MathHelper.clamp(volume, 1, 3);
            if (entityStrength > 0) {
                entityMultiplier *= MathHelper.clamp(entityStrength, 2, 6) * 0.45 + 0.3;
            }
            
            return baseCost * entityMultiplier;
        }
        
        return baseCost;
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
