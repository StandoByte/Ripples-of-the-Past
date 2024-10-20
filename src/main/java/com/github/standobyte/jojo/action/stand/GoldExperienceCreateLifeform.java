package com.github.standobyte.jojo.action.stand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.config.ActionConfigField;
import com.github.standobyte.jojo.action.non_stand.HamonOrganismInfusion;
import com.github.standobyte.jojo.action.stand.effect.GECreatedLifeformEffect;
import com.github.standobyte.jojo.capability.entity.LifeformsMetMobs;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.stand.ge.EntityTypeIcon;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.entity.RoadRollerEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.MolotovEntity;
import com.github.standobyte.jojo.entity.itemprojectile.KnifeEntity;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.item.MolotovItem;
import com.github.standobyte.jojo.itemtracking.SidedItemTrackerMap;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.MrPresidentStandType;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.ObjectWrapper;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.github.standobyte.jojo.world.dimension.mr_president.MrPresidentWorldData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
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
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BucketItem;
import net.minecraft.item.FishBucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;

public class GoldExperienceCreateLifeform extends StandAction {
    @ActionConfigField public double maxLifeformDistance = 128;

    public GoldExperienceCreateLifeform(StandAction.Builder builder) {
        super(builder);
        voiceLineDelay = Integer.MAX_VALUE;
    }
    
    @Override
    public void overrideVanillaMouseTarget(ObjectWrapper<ActionTarget> targetContainer, World world, LivingEntity user, IStandPower power) {
        Entity aimingEntity = StandUtil.getStandIfInManualControl(power);
        Vector3d startPos = aimingEntity.getEyePosition(1.0F);
        double distance = Math.sqrt(getMaxRangeSqBlockTarget());
        Vector3d rtVec = aimingEntity.getViewVector(1.0F).scale(distance);
        Vector3d endPos = startPos.add(rtVec);
        AxisAlignedBB aabb = aimingEntity.getBoundingBox().expandTowards(rtVec).inflate(1);
        RayTraceResult rayTrace = JojoModUtil.rayTraceMultipleEntities(startPos, endPos, aabb, 
                distance, world, aimingEntity, 
                e -> e instanceof ItemEntity, false, RayTraceContext.BlockMode.COLLIDER, 
                0, 0)[0];
        if (rayTrace.getType() == RayTraceResult.Type.ENTITY) {
            targetContainer.set(ActionTarget.fromRayTraceResult(rayTrace));
        }
    }
    
    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        switch (target.getType()) {
        case ENTITY:
            Entity entity = target.getEntity();
            if (entity instanceof ItemEntity) {
                ItemStack item = ((ItemEntity) entity).getItem();
                return HamonUtil.isItemLivingMatter(item) ? conditionMessage("ge_lifeform_material_item") : ActionConditionResult.POSITIVE;
            }
            // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! use more types of inanimate entities as targets?
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
        
        int mobsCreated = (int) StandEffectsTracker.getEffectsOfType(power, ModStandEffects.GE_CREATED_LIFEFORM.get(), -1).count();
        if (mobsCreated >= 16) {
            return conditionMessage("ge_too_many_mobs");
        }

        if (target.getType() == TargetType.ENTITY
                || GoldExperienceMarkItem.getTargetedMarkedItem(power, user).isPresent()) {
            return ActionConditionResult.POSITIVE;
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
        PlayerEntity player = ClientUtil.getClientPlayer();
        NetworkUtil.writeOptionally(buf, 
                getChosenEntityType(player), 
                EntitySubtype::toBuf);
        
        Optional<UUID> trackedItemUUID = GoldExperienceMarkItem
                .getTargetedMarkedItem(IStandPower.getPlayerStandPower(player), player)
                .map(TrackerItemStack::getTrackerId);
        NetworkUtil.writeOptional(buf, trackedItemUUID, buf::writeUUID);
    }
    
    @Nullable
    public static EntitySubtype<?> getChosenEntityType(PlayerEntity player) {
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
    
    public static Entity createEntity(EntitySubtype<?> type, World world, LivingEntity standUser) {
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
            EntitySubtype<?> type = NetworkUtil.readOptional(extraInput, EntitySubtype::fromBuf).orElse(null);
            Optional<UUID> itemTrackerId = NetworkUtil.readOptional(extraInput, extraInput::readUUID);
            if (type != null
                    && GeneralUtil.orElseFalse(user.getCapability(PlayerUtilCapProvider.CAPABILITY), 
                            cap -> cap.metEntityType(type))
                    && GoldExperienceChooseLifeform.isValidLifeform(type, world)) {
                
                Entity lifeFormCreated = createEntity(type, world, user);
                int ticks = getTicksToCreate(user, power, lifeFormCreated);
                
                Entity performer = getControlledEntity(user, power);
                GETransformationEntity tf = new GETransformationEntity(world);
                
                ObjectWrapper<ITextComponent> customName = new ObjectWrapper<>(null);
                boolean tfTargetFound = false;
                
                ObjectWrapper<Entity> nonUserItemHolder = new ObjectWrapper<>(null);
                
                // marked item...
                if (itemTrackerId.isPresent()) {
                    TrackerItemStack itemTracker = SidedItemTrackerMap.getSidedTrackers(world).getTracker(itemTrackerId.get());
                    if (itemTracker != null && itemTracker.checkItemIsThere((ServerWorld) world)) {
                        // ...from entity
                        Entity itemEntity = itemTracker.getAtEntity(world);
                        LivingEntity livingItemHolder = itemEntity instanceof LivingEntity ? (LivingEntity) itemEntity : null;
                        if (itemEntity != null) {
                            KnownItemState itemState = itemTracker.getItemState();
                            if (itemState != null) {
                                tfTargetFound = true;
                                itemTracker.clear();
                                Vector3d pos = itemEntity.position();
                                
                                switch (itemState) {
                                case ENTITY_HAS_ITEM:
                                    mobFromInventory(tf, itemTracker.getItem(), world, 
                                            livingItemHolder != null ? livingItemHolder : user, 
                                            itemEntity.blockPosition(), customName);
                                    if (itemEntity != user) {
                                        nonUserItemHolder.set(itemEntity);
                                    }
                                    
                                    tf.moveTo(pos.x, pos.y, pos.z, itemEntity.yRot, 0);
                                    
                                    break;
                                case ENTITY_IS_ITEM:
                                    mobFromEntity(tf, itemEntity);
                                    break;
                                case STUCK_ARROW:
                                    tf.getTfSourceData().withEntitySource(new ArrowEntity(world, user));
                                    tf.moveTo(pos.x, pos.y, pos.z, itemEntity.yRot, itemEntity.xRot);
                                    if (livingItemHolder != null) {
                                        decrementStuckArrow(livingItemHolder);
                                        tf.withHost(livingItemHolder);
                                        tf.getTfSourceData().withFollowTarget(livingItemHolder.getUUID(), GETransformationEntity.FollowTargetMode.AGGRO);
                                    }
                                    break;
                                case STUCK_KNIFE:
                                    tf.getTfSourceData().withEntitySource(new KnifeEntity(world, user));
                                    tf.moveTo(pos.x, pos.y, pos.z, itemEntity.yRot, itemEntity.xRot);
                                    if (livingItemHolder != null) {
                                        decrementStuckKnife(livingItemHolder);
                                        tf.withHost(livingItemHolder);
                                        tf.getTfSourceData().withFollowTarget(livingItemHolder.getUUID(), GETransformationEntity.FollowTargetMode.AGGRO);
                                    }
                                    break;
                                default:
                                    JojoMod.getLogger().error("Didn't handle the case of {} item being inside an entity", itemState);
                                    break;
                                }
                            }
                            else {
                                JojoMod.getLogger().error("Failed to extract tracked item from {} entity", itemEntity.getType().getRegistryName());
                            }
                        }
                        else {
                            // ...or from block
                            BlockPos itemPos = itemTracker.getAtBlockPos(world);
                            if (itemPos != null) {
                                BlockState blockState = world.getBlockState(itemPos);
                                KnownItemState itemState = itemTracker.getItemState();
                                if (itemState != null) {
                                    tfTargetFound = true;
                                    itemTracker.clear();
                                    
                                    switch (itemState) {
                                    case BLOCK_HAS_ITEM:
                                        mobFromInventory(tf, itemTracker.getItem(), world, 
                                                user, itemPos.above(), customName);
                                        
                                        tf.moveTo(itemPos.getX(), itemPos.getY() + 1, itemPos.getZ(), 0, 0);
                                        break;
                                    case BLOCK_IS_ITEM:
                                        tfTargetFound = false;
                                        break;
                                    default:
                                        JojoMod.getLogger().error("Didn't handle the case of {} item being inside a block", itemState);
                                        break;
                                    }
                                }
                                else {
                                    JojoMod.getLogger().error("Failed to extract tracked item from {} block at {}", blockState.getBlock().getRegistryName(), itemPos);
                                }
                            }
                        }
                    }
                }
                
                // targeted non-living entity
                if (!tfTargetFound && target.getType() == TargetType.ENTITY) {
                    Entity targetEntity = target.getEntity();
                    tfTargetFound = true;
                    mobFromEntity(tf, targetEntity);
                }
                
                // item held in off-hand
                if (!tfTargetFound) {
                    ItemStack heldItem = user.getItemInHand(Hand.OFF_HAND);
                    if (!heldItem.isEmpty() && canGiveLifeTo(heldItem)) {
                        tfTargetFound = true;
                        mobFromInventory(tf, heldItem, world, 
                                user, performer.blockPosition(), customName);
                        
                        Vector3d pos = performer.position();
                        Vector3d lookVec = performer.getLookAngle();
                        double distScale = lifeFormCreated.getBbWidth() + 1;
                        pos = pos.add(lookVec.x * distScale, 0, lookVec.z * distScale);
                        tf.moveTo(pos.x, pos.y, pos.z, performer.yRot, 0);
                    }
                }
                
                // targeted non-living block
                if (!tfTargetFound && target.getType() == TargetType.BLOCK
                        && JojoModUtil.breakingBlocksEnabled(user.level)) {
                    BlockPos blockPos = target.getBlockPos();
                    BlockState blockState = world.getBlockState(blockPos);
                    
                    if (!HamonOrganismInfusion.isBlockLiving(blockState)) {
                        tfTargetFound = true;
                        mobFromBlock(tf, blockPos, blockState, (ServerWorld) world, lifeFormCreated);
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
                    if (customName.get() != null) {
                        lifeFormCreated.setCustomName(customName.get());
                    }
                    world.addFreshEntity(tf);
                    
                    if (lifeFormCreated instanceof LivingEntity) {
                        IStandPower.getStandPowerOptional((LivingEntity) lifeFormCreated).ifPresent(mobStand -> {
                            if (mobStand.getType() == ModStandsInit.MR_PRESIDENT.get()) {
                                LazyOptional<MrPresidentWorldData> mrPresidentTracker = MrPresidentWorldData.get(((ServerWorld) user.level).getServer());
                                mrPresidentTracker.ifPresent(tracker -> {
                                    tracker.rememberTurtlePosition(lifeFormCreated);
                                    List<Entity> entitiesToTeleport = MrPresidentStandType.findTargets(
                                            lifeFormCreated, toTeleport -> 
                                            toTeleport != tf && toTeleport != user && toTeleport != power.getStandManifestation());
                                    if (nonUserItemHolder.get() != null) {
                                        entitiesToTeleport = new ArrayList<>(entitiesToTeleport);
                                        entitiesToTeleport.add(nonUserItemHolder.get());
                                    }
                                    MrPresidentStandType.teleportEntities(lifeFormCreated, mobStand, entitiesToTeleport);
                                });
                            }
                        });
                    }
                    
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
    
    
    private void mobFromEntity(GETransformationEntity tf, Entity entity) {
        MCUtil.cloneEntity(entity).ifPresent(e -> tf.getTfSourceData().withEntitySource(e));
        entity.remove();
        
        Vector3d pos = entity.position();
        tf.moveTo(pos.x, pos.y, pos.z, entity.yRot, entity.xRot);
        
        if (entity.isOnFire()) {
            tf.setSecondsOnFire((entity.getRemainingFireTicks() + 19) / 20);
        }
        tf.setDeltaMovement(entity.getDeltaMovement());
        
        if (entity instanceof ItemEntity) {
            UUID thrower = ((ItemEntity) entity).getThrower();
            if (thrower != null) {
                tf.getTfSourceData().withFollowTarget(thrower, GETransformationEntity.FollowTargetMode.TRACK);
            }
        }
    }
    
    private void mobFromInventory(GETransformationEntity tf, ItemStack item, World world, 
            @Nonnull LivingEntity wouldBeThrower, BlockPos fishBucketPos, ObjectWrapper<ITextComponent> mobName) {
        Entity itemEntity;
        ItemStack transformedItem = null;
        if (item.getItem() instanceof BucketItem) {
            BucketItem bucketType = (BucketItem) item.getItem();
            Fluid fluid = bucketType.getFluid();
            Item bucketWithoutFish = fluid.getBucket();
            if (bucketWithoutFish != Items.AIR) {
                transformedItem = new ItemStack(bucketWithoutFish);
                bucketType.checkExtraContent(world, item, fishBucketPos);
            }
        }
        if (transformedItem == null) {
            transformedItem = item.copy();
        }
        transformedItem.setCount(1);
        if (item.getItem() instanceof ThrowablePotionItem) {
            PotionEntity potionEntity = new PotionEntity(world, wouldBeThrower);
            potionEntity.setItem(transformedItem);
            itemEntity = potionEntity;
        }
        else if (item.getItem() == Items.ENDER_PEARL) {
            EnderPearlEntity pearlEntity = new EnderPearlEntity(world, wouldBeThrower);
            itemEntity = pearlEntity;
        }
        else if (item.getItem() == ModItems.MOLOTOV.get() && wouldBeThrower instanceof PlayerEntity && MolotovItem.useFire((PlayerEntity) wouldBeThrower, world)) {
            MolotovEntity molotovEntity = new MolotovEntity(world, wouldBeThrower);
            itemEntity = molotovEntity;
        }
        else {
            itemEntity = new ItemEntity(world, 0, 0, 0, transformedItem);
        }
        if (item.hasCustomHoverName()) {
            mobName.set(item.getHoverName());
        }
        item.shrink(1);
        
        tf.getTfSourceData().withEntitySource(itemEntity);
    }
    
    private void mobFromBlock(GETransformationEntity tf, BlockPos blockPos, BlockState blockState, ServerWorld world, Entity lifeformCreated) {
        TileEntity tileEntity = world.getBlockEntity(blockPos);
        
        if (tileEntity instanceof IInventory) {
            KEEP_ITEMS.add(tileEntity);
            
            if (lifeformCreated.getType().getRegistryName().getPath().contains("pigeon")) {
                IInventory inventory = (IInventory) tileEntity;
                Optional<UUID> deliveryDest = IntStream.range(0, inventory.getMaxStackSize()).mapToObj(inventory::getItem)
                        .filter(item -> !item.isEmpty() && item.getItem() == Items.NAME_TAG && item.hasCustomHoverName())
                        .map(nameTag -> nameTag.getHoverName().getString())
                        .filter(name -> !StringUtils.isBlank(name))
                        .map(name -> {
                            ServerPlayerEntity online = world.getServer().getPlayerList().getPlayerByName(name);
                            if (online != null) {
                                return online.getUUID();
                            }
                            return PlayerEntity.createPlayerUUID(name);
                        })
                        .filter(id -> id != null).findFirst();
                deliveryDest.ifPresent(destId -> tf.getTfSourceData().withFollowTarget(destId, GETransformationEntity.FollowTargetMode.DELIVERY));
            }
        }
        world.removeBlock(blockPos, false);
        KEEP_ITEMS.remove(tileEntity);
        
        tf.getTfSourceData().withBlockSource(blockState, blockPos, tileEntity);
    }
    

    
    static int getTicksToCreate(LivingEntity user, IStandPower power, Entity targetEntity) {
        return getTicksToCreate(user, power, targetEntity, 
                targetEntity.getCapability(PlayerUtilCapProvider.CAPABILITY).map(PlayerUtilCap::getMetMobs).orElse(null));
    }
    
    public static int getTicksToCreate(LivingEntity user, IStandPower power, Entity targetEntity, LifeformsMetMobs geUserMetMobs) {
        double entityStrength = getAttackStrength(targetEntity);
        float volume = getVolume(targetEntity);
        double standSpeed = 0;
        if (power != null && power.hasPower()) {
            StandStats stats = power.getType().getStats();
            standSpeed = stats.getBaseAttackSpeed() + stats.getDevAttackSpeed(power.getStatsDevelopment());
        }
        
        double value = 240 / Math.max(standSpeed, 1)
                + MathHelper.ceil(volume * (1 + entityStrength * 0.125) * MathHelper.clamp(100 - standSpeed * 2, 0, 100));
        if (geUserMetMobs != null && geUserMetMobs.isMobNativeToPlayerPos(user.level, targetEntity, user)) {
            value *= 0.5;
        }
        return (int) value;
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
    
    
    public static int getStuckArrows(LivingEntity entity) {
        return entity.getArrowCount();
    }
    
    public static void decrementStuckArrow(LivingEntity entity) {
        entity.setArrowCount(entity.getArrowCount() - 1);
    }
    
    public static int getStuckKnives(LivingEntity entity) {
        return entity.getCapability(LivingUtilCapProvider.CAPABILITY)
                .map(data -> data.getStuckObjects().getKnives().getCount()).orElse(0);
    }
    
    public static void decrementStuckKnife(LivingEntity entity) {
        entity.getCapability(LivingUtilCapProvider.CAPABILITY).map(data -> data.getStuckObjects().getKnives()).ifPresent(
                knives -> knives.setCount(knives.getCount() - 1));
    }
    
    
    
    @Override
    public IFormattableTextComponent getTranslatedName(IStandPower power, String key) {
        EntitySubtype<?> chosenEntityType = getChosenEntityType(ClientUtil.getClientPlayer());
        if (chosenEntityType != null) {
            return new TranslationTextComponent(key + ".param", chosenEntityType.getDescription());
        }
        else {
            return super.getTranslatedName(power, key);
        }
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
