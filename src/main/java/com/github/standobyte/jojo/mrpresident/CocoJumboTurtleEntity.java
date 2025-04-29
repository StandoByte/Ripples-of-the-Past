package com.github.standobyte.jojo.mrpresident;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.item.InventoryItemHighlight;
import com.github.standobyte.jojo.entity.IPassengerMixinReposition;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.item.MrPresidentKeyItem;
import com.github.standobyte.jojo.item.StandArrowItem;
import com.github.standobyte.jojo.potion.StandVirusEffect;
import com.github.standobyte.jojo.potion.StandVirusEffect.MobStandGiver;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandPower;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

public class CocoJumboTurtleEntity extends TurtleEntity implements IMobStandUser, IPassengerMixinReposition {
    private static final DataParameter<Boolean> HAS_KEY = EntityDataManager.defineId(CocoJumboTurtleEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> ASSIGNED_KEY = EntityDataManager.defineId(CocoJumboTurtleEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> IS_CARRIED = EntityDataManager.defineId(CocoJumboTurtleEntity.class, DataSerializers.BOOLEAN);
    private final IStandPower standPower = new StandPower(this);
    
    static {
        StandVirusEffect.addMobStandGiver(new MobStandGiver(ModEntityTypes.COCO_JUMBO_TURTLE, ModStandsInit.MR_PRESIDENT));
    }
    
    public CocoJumboTurtleEntity(EntityType<? extends TurtleEntity> type, World world) {
        super(type, world);
    }
    
    
    @Override
    public IStandPower getStandPower() {
        return standPower;
    }
    
    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.put("StandPower", standPower.writeNBT());
        nbt.putBoolean("Key", hasKey());
        nbt.putBoolean("AssignedKey", hasAssignedKey());
        nbt.putBoolean("Carried", isCarried());
    }
    
    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("StandPower", MCUtil.getNbtId(CompoundNBT.class))) {
            standPower.readNBT(nbt.getCompound("StandPower"));
        }
        setHasKey(nbt.getBoolean("Key"));
        entityData.set(ASSIGNED_KEY, nbt.getBoolean("AssignedKey"));
        entityData.set(IS_CARRIED, nbt.getBoolean("Carried"));
    }
    
    
    @Override
    public void tick() {
        standPower.tick();
        bandAidPreTick();
        super.tick();
        
        if (!level.isClientSide()) {
            for (ServerPlayerEntity player : ((ServerWorld) level).players()) {
                if (player.distanceToSqr(this) < 36) {
                    ModCriteriaTriggers.MEET_ENTITY.get().trigger(player, this);
                }
            }
        }
        else if (!standPower.hasPower() && !this.hasEffect(ModStatusEffects.STAND_VIRUS.get())) {
            if (ClientUtil.getClientPlayer().distanceToSqr(this) < 36) {
                InventoryItemHighlight.highlightItem(Items.BOW, 20);
                InventoryItemHighlight.highlightItem(Items.CROSSBOW, 20);
                InventoryItemHighlight.highlightItem(ModItems.STAND_ARROW.get(), 20);
                InventoryItemHighlight.highlightItem(ModItems.STAND_ARROW_BEETLE.get(), 20);
            }
        }
        
        if (!level.isClientSide() && getVehicle() == null) {
            entityData.set(IS_CARRIED, false);
        }
        LivingEntity carrier = getCarrier(); // FIXME sometimes doesn't trigger on client (IS_CARRIED has already been synced to client)
        if (carrier != null) {
            if (!MCUtil.itemHandFree(carrier.getItemInHand(Hand.OFF_HAND)) || carrier.isSpectator()) {
                stopRiding();
            }
            else {
                float headRotOffset = carrier.yBodyRot - this.yHeadRot;
                this.yRot = carrier.yBodyRot;
                this.yHeadRot += headRotOffset;
                this.yBodyRot += headRotOffset;
            }
        }
    }
    
    
    @Override
    protected void defineSynchedData() {
       super.defineSynchedData();
       entityData.define(HAS_KEY, false);
       entityData.define(ASSIGNED_KEY, false);
       entityData.define(IS_CARRIED, false);
    }
    
    public boolean hasKey() {
        return entityData.get(HAS_KEY);
    }
    
    public void setHasKey(boolean key) {
        entityData.set(HAS_KEY, key);
    }
    
    public boolean hasAssignedKey() {
        return entityData.get(ASSIGNED_KEY);
    }
    
    @Override
    public boolean removeWhenFarAway(double distToClosestPlayer) {
        return false;
    }
    
    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }
    
    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand pHand) {
        ItemStack heldItem = player.getItemInHand(pHand);
        if (!this.hasKey() && heldItem.getItem() instanceof MrPresidentKeyItem) {
            ActionConditionResult canPutKey = canPutKey(heldItem);
            if (canPutKey.isPositive()) {
//                pPlayer.playSound(SoundEvents., 1.0F, 1.0F);
                heldItem.shrink(1);
                if (!level.isClientSide()) {
                    setHasKey(true);
                    entityData.set(ASSIGNED_KEY, true);
                    ModCriteriaTriggers.COCO_JUMBO_KEY.get().trigger((ServerPlayerEntity) player);
                }
                return ActionResultType.SUCCESS;
            }
            
            if (level.isClientSide()) {
                player.displayClientMessage(canPutKey.getWarning(), true);
            }
            return ActionResultType.FAIL;
        }
        else if (pHand == Hand.MAIN_HAND && player.isShiftKeyDown() && this.hasKey() && heldItem.isEmpty()) {
//            pPlayer.playSound(SoundEvents., 1.0F, 1.0F);
            if (!level.isClientSide()) {
                setHasKey(false);
                ItemStack keyItem = makeKeyItem();
                player.setItemInHand(pHand, keyItem);
            }
            return ActionResultType.SUCCESS;
        }
        else if (pHand == Hand.MAIN_HAND && !player.isShiftKeyDown() && !this.isPassenger() && !(heldItem.getItem() instanceof ShootableItem)) {
            if (MCUtil.isHandFree(player, Hand.OFF_HAND)) {
                if (this.startRiding(player, true)) {
                    if (!level.isClientSide()) {
                        entityData.set(IS_CARRIED, true);
                        player.displayClientMessage(new TranslationTextComponent("coco_jumbo.hint.release", 
                                new KeybindTextComponent("key.swapOffhand"), getDisplayName()), true);
                    }
                }
                return ActionResultType.sidedSuccess(this.level.isClientSide);
            }
            else {
                if (level.isClientSide()) {
                    player.displayClientMessage(new TranslationTextComponent("coco_jumbo.carry.offhand", 
                            getDisplayName()), true);
                }
                return ActionResultType.PASS;
            }
        }
        else {
            return super.mobInteract(player, pHand);
        }
    }
    
    
    private ItemStack makeKeyItem() {
        ItemStack keyItem = new ItemStack(ModItems.MR_PRESIDENT_KEY.get());
        CompoundNBT nbt = keyItem.getOrCreateTag();
        nbt.putUUID("TurtleEntity", getUUID());
        ITextComponent name = new TranslationTextComponent(keyItem.getDescriptionId() + ".named", this.getDisplayName());
        keyItem.setHoverName(name);
        return keyItem;
    }
    
    private ActionConditionResult canPutKey(ItemStack item) {
        if (item.getItem() == ModItems.MR_PRESIDENT_MASTER_KEY.get()) {
            return ActionConditionResult.POSITIVE;
        }
        if (!getStandPower().hasPower()) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("coco_jumbo.key.no_stand", 
                    getDisplayName()));
        }
        if (hasAssignedKey()) {
            CompoundNBT nbt = item.getTag();
            if (nbt == null) {
                return ActionConditionResult.createNegative(new TranslationTextComponent("coco_jumbo.key.empty"));
            }
            if (!(nbt.hasUUID("TurtleEntity") && this.getUUID().equals(nbt.getUUID("TurtleEntity")))) {
                return ActionConditionResult.createNegative(new TranslationTextComponent("coco_jumbo.key.wrong"));
            }
            return ActionConditionResult.POSITIVE;
        }
        else {
            if (item.hasTag()) {
                return ActionConditionResult.createNegative(new TranslationTextComponent("coco_jumbo.key.not_empty"));
            }
            return ActionConditionResult.POSITIVE;
        }
    }

    
    public boolean isCarried() {
        return entityData.get(IS_CARRIED);
    }
    
    @Nullable
    public LivingEntity getCarrier() {
        if (isCarried()) {
            Entity vehicle = getVehicle();
            if (vehicle instanceof LivingEntity) {
                return ((LivingEntity) vehicle);
            }
        }
        
        return null;
    }
    
    @Override
    public void stopRiding() {
        if (cancelStopRiding()) {
            return;
        }
        super.stopRiding();
        if (!level.isClientSide() && getVehicle() == null) {
            entityData.set(IS_CARRIED, false);
        }
    }
    
    @Override
    public void onSyncedDataUpdated(DataParameter<?> key) {
        super.onSyncedDataUpdated(key);
        if (IS_CARRIED.equals(key) && !entityData.get(IS_CARRIED)) {
            stopRiding();
        }
    }
    
    @Override
    public boolean startRiding(Entity vehicle, boolean force) {
        boolean res = super.startRiding(vehicle, force);
        return res;
    }
    
    @Override
    public boolean isPickable() {
        return super.isPickable() && !isCarried();
    }
    
    @Override
    public Vector3d repositionPassenger(Entity vehicle) {
        if (isCarried() && vehicle instanceof LivingEntity) {
            LivingEntity carrier = (LivingEntity) vehicle;
            Vector3d carryVec = carryOffset(carrier.yBodyRot, carrier);
            return vehicle.position().add(carryVec);
        }
        return null;
    }
    
    public static Vector3d carryOffset(float yRot, LivingEntity carrier) {
        HandSide offHand = MCUtil.getOppositeSide(carrier.getMainArm());
        float width = carrier.getBbWidth();
        Vector3d carryVec = new Vector3d(
                width * (offHand == HandSide.LEFT ? 0.55 : -0.55), 
                carrier.getBbHeight() * 0.35, 
                width * 0.75);
        
        carryVec = carryVec.yRot(-yRot * MathUtil.DEG_TO_RAD);
        
        return carryVec;
    }
    
    public static boolean isCarriedTurtle(Entity passenger, Entity carrier) {
        return passenger.getType() == ModEntityTypes.COCO_JUMBO_TURTLE.get()
                && ((CocoJumboTurtleEntity) passenger).getCarrier() == carrier;
    }
    
    @Override
    public boolean isInvulnerableTo(DamageSource pDamageSource) {
        return pDamageSource == DamageSource.IN_WALL || super.isInvulnerableTo(pDamageSource);
    }
    
    
    public static final ResourceLocation GOT_ARROW_ADVANCEMENT = new ResourceLocation(JojoMod.MOD_ID, "jojo/stand_arrow");
    public static final ResourceLocation MET_TURTLE_ADVANCEMENT = new ResourceLocation(JojoMod.MOD_ID, "jojo/coco_jumbo");
    private static long lastSpawnTime;
    public static void onRegularTutelSpawn(LivingSpawnEvent.CheckSpawn event) {
        SpawnReason spawnReason = event.getSpawnReason();
        switch (spawnReason) {
        case NATURAL:
        case CHUNK_GENERATION:
        case SPAWNER:
            if (event.getWorld() instanceof IServerWorld && lastSpawnTime != event.getWorld().dayTime()) {
                IServerWorld spawnRegion = (IServerWorld) event.getWorld();
                double x = event.getX();
                double y = event.getY();
                double z = event.getZ();
                PlayerEntity nearestPlayer = spawnRegion.getLevel().getNearestPlayer(x, y, z, -1, EntityPredicates.NO_SPECTATORS);
                if (nearestPlayer instanceof ServerPlayerEntity) {
                    ServerPlayerEntity player = (ServerPlayerEntity) nearestPlayer;
                    boolean hasArrow = !MCUtil.findInInventory(player.inventory, item -> item.getItem() instanceof StandArrowItem).isEmpty();
                    boolean hasArrowAdvancement = MCUtil.hasAdvancement(player, GOT_ARROW_ADVANCEMENT);
                    boolean hasTurtleAdvancement = MCUtil.hasAdvancement(player, MET_TURTLE_ADVANCEMENT);
                    
                    float spawnChancePerTurtle;
                    switch (spawnReason) {
                    case CHUNK_GENERATION:
                        if (!hasTurtleAdvancement)          spawnChancePerTurtle = 0.075f;
                        else if (hasArrow)                  spawnChancePerTurtle = 0.0375f;
                        else if (hasArrowAdvancement)       spawnChancePerTurtle = 0.025f;
                        else                                spawnChancePerTurtle = 0.0125f;
                        break;
                    default:
                        if (!hasTurtleAdvancement)          spawnChancePerTurtle = 0.015f;
                        else if (hasArrow)                  spawnChancePerTurtle = 0.0075f;
                        else if (hasArrowAdvancement)       spawnChancePerTurtle = 0.005f;
                        else                                spawnChancePerTurtle = 0.0025f;
                        break;
                    }
                    
                    if (player.getRandom().nextFloat() < spawnChancePerTurtle) {
                        LivingEntity turtle = event.getEntityLiving();
                        MobEntity extraTurtle = ModEntityTypes.COCO_JUMBO_TURTLE.get().create(spawnRegion.getLevel());

                        extraTurtle.moveTo(x, y, z, turtle.getRandom().nextFloat() * 360.0F, 0.0F);
                        if (ForgeHooks.canEntitySpawn(extraTurtle, spawnRegion, x, y, z, null, spawnReason) != -1
                                && extraTurtle.checkSpawnRules(spawnRegion, spawnReason) && extraTurtle.checkSpawnObstruction(spawnRegion)) {
                            ILivingEntityData entityData = null;
                            entityData = extraTurtle.finalizeSpawn(spawnRegion, 
                                    spawnRegion.getCurrentDifficultyAt(extraTurtle.blockPosition()), 
                                    spawnReason, entityData, null);
                            spawnRegion.addFreshEntityWithPassengers(extraTurtle);
                            lastSpawnTime = event.getWorld().dayTime();
                        }
                    }
                }
            }
            break;
        default:
            break;
        }
        
    }
    
    
    // the shit below prevents the turtle from dismounting when the player gets into water
    // (the actual logic for that in in LivingEntity#baseTick, and i ain't whipping out a mixin for that)
    private byte stopRidingIsDueToWater;
    
    private void bandAidPreTick() {
        stopRidingIsDueToWater = 0;
    }
    
    @Override
    public boolean isEyeInFluid(ITag<Fluid> tag) {
        if (stopRidingIsDueToWater == 0 && tag == FluidTags.WATER) {
            stopRidingIsDueToWater = 1;
        }
        return super.isEyeInFluid(tag);
    }
    
    @Override
    public boolean canBreatheUnderwater() {
        if (stopRidingIsDueToWater == 1) {
            stopRidingIsDueToWater = 2;
        }
        return super.canBreatheUnderwater();
    }

    @Override
    protected void onChangedBlock(BlockPos pos) {
        stopRidingIsDueToWater = -1;
        super.onChangedBlock(pos);
    }
    
    private boolean cancelStopRiding() {
        return stopRidingIsDueToWater == 2;
    }
    // the stupid band-aid code is over

}
