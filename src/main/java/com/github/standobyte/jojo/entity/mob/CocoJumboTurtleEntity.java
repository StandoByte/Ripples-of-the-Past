package com.github.standobyte.jojo.entity.mob;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.render.item.InventoryItemHighlight;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.potion.StandVirusEffect;
import com.github.standobyte.jojo.potion.StandVirusEffect.MobStandGiver;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.advancements.Advancement;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

public class CocoJumboTurtleEntity extends TurtleEntity implements IMobStandUser {
    private static final DataParameter<Boolean> HAS_KEY = EntityDataManager.defineId(CocoJumboTurtleEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> ASSIGNED_KEY = EntityDataManager.defineId(CocoJumboTurtleEntity.class, DataSerializers.BOOLEAN);
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
    }
    
    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("StandPower", MCUtil.getNbtId(CompoundNBT.class))) {
            standPower.readNBT(nbt.getCompound("StandPower"));
        }
        setHasKey(nbt.getBoolean("Key"));
        entityData.set(ASSIGNED_KEY, nbt.getBoolean("AssignedKey"));
    }
    
    
    @Override
    public void tick() {
        standPower.tick();
        super.tick();
        if (!level.isClientSide()) {
            for (ServerPlayerEntity player : ((ServerWorld) level).players()) {
                if (player.distanceToSqr(this) < 36) {
                    ModCriteriaTriggers.MEET_ENTITY.get().trigger(player, this);
                }
            }
        }
    }
    
    
    @Override
    protected void defineSynchedData() {
       super.defineSynchedData();
       entityData.define(HAS_KEY, false);
       entityData.define(ASSIGNED_KEY, false);
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
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || hasKey() || standPower.hasPower();
    }
    
    @Override
    public ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
        ItemStack heldItem = pPlayer.getItemInHand(pHand);
        if (!this.hasKey() && heldItem.getItem() == ModItems.MR_PRESIDENT_KEY.get()) {
            ActionConditionResult canPutKey = canPutKey(heldItem);
            if (canPutKey.isPositive()) {
//                pPlayer.playSound(SoundEvents., 1.0F, 1.0F);
                heldItem.shrink(1);
                if (!standPower.hasPower()) {
                    if (!level.isClientSide()) {
                        pPlayer.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(
                                cap -> cap.sendNotification(OneTimeNotification.SHOOT_COCO_JUMBO, new TranslationTextComponent("give_mr_president_hint")));
                    }
                    else {
                        InventoryItemHighlight.highlightItem(Items.BOW, 120);
                        InventoryItemHighlight.highlightItem(Items.CROSSBOW, 120);
                        InventoryItemHighlight.highlightItem(ModItems.STAND_ARROW.get(), 120);
                        InventoryItemHighlight.highlightItem(ModItems.STAND_ARROW_BEETLE.get(), 120);
                    }
                }
                if (!level.isClientSide()) {
                    setHasKey(true);
                    entityData.set(ASSIGNED_KEY, true);
                }
                return ActionResultType.SUCCESS;
            }
            else {
                if (level.isClientSide()) {
                    pPlayer.displayClientMessage(canPutKey.getWarning(), true);
                }
                return ActionResultType.FAIL;
            }
        }
        else if (this.hasKey() && heldItem.isEmpty()) {
//            pPlayer.playSound(SoundEvents., 1.0F, 1.0F);
            if (!level.isClientSide()) {
                setHasKey(false);
                ItemStack keyItem = makeKeyItem();
                pPlayer.setItemInHand(pHand, keyItem);
            }
            return ActionResultType.SUCCESS;
        }
        else {
            return super.mobInteract(pPlayer, pHand);
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
                    boolean hasArrowAdvancement = hasAdvancement(player, GOT_ARROW_ADVANCEMENT);
                    boolean hasTurtleAdvancement = hasAdvancement(player, MET_TURTLE_ADVANCEMENT);
                    
                    float spawnChancePerTurtle;
                    switch (spawnReason) {
                    case CHUNK_GENERATION:
                        if (!hasArrowAdvancement)           spawnChancePerTurtle = 0.025f;
                        else if (!hasTurtleAdvancement)     spawnChancePerTurtle = 0.1f;
                        else                                spawnChancePerTurtle = 0.05f;
                        break;
                    default:
                        if (!hasArrowAdvancement)           spawnChancePerTurtle = 0.005f;
                        else if (!hasTurtleAdvancement)     spawnChancePerTurtle = 0.02f;
                        else                                spawnChancePerTurtle = 0.01f;
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
    
    
    private static boolean hasAdvancement(ServerPlayerEntity player, ResourceLocation advancementPath) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(advancementPath);
        if (advancement != null) {
            return player.getAdvancements().getOrStartProgress(advancement).isDone();
        }
        return false;
    }

}
