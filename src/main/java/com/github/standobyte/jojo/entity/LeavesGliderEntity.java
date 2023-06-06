package com.github.standobyte.jojo.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClLeavesGliderColorPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.GameData;

public class LeavesGliderEntity extends Entity implements IEntityAdditionalSpawnData, IHasHealth {
    private static final double GRAVITY = -0.01D;
    public static final float MAX_ENERGY = 200;
    private static final float MAX_HEALTH = 4F;

    private static final DataParameter<Boolean> IS_FLYING = EntityDataManager.defineId(LeavesGliderEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> ENERGY = EntityDataManager.defineId(LeavesGliderEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> HEALTH = EntityDataManager.defineId(LeavesGliderEntity.class, DataSerializers.FLOAT);
    
    private BlockState leavesBlock = Blocks.OAK_LEAVES.defaultBlockState();
    private ResourceLocation leavesBlockTex = null;
    private int foliageColor = -1;
    private List<INonStandPower> passengerPowers = new ArrayList<>();
    private float passengersHeight;

    private float yRotDelta;
    private boolean inputLeft;
    private boolean inputRight;
  
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;

    public LeavesGliderEntity(World world) {
        this(ModEntityTypes.LEAVES_GLIDER.get(), world);
    }

    public LeavesGliderEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        tickLerp();

        if (!level.isClientSide()) {
            updateFlying();
        }
        if (isFlying() && isControlledByLocalInstance()) {
            Vector3d prevMovement = getDeltaMovement().subtract(0, getDeltaMovement().y, 0);
            if (level.isClientSide()) {
                if (isVehicle()) {
                    updateRotationDelta();
                    yRot += yRotDelta;
                    for (Entity passenger : getPassengers()) {
                        passenger.yRot += yRotDelta;
                        if (passenger instanceof LivingEntity) {
                            ((LivingEntity) passenger).yBodyRot += yRotDelta;
                        }
                    }
                    prevMovement = Vector3d.directionFromRotation(0, yRot).scale(prevMovement.length());
                }
            }
            double gravity = isNoGravity() ? 0.0D : GRAVITY * (1 + getPassengers().size());
            Vector3d movement = prevMovement.normalize().scale(Math.min(prevMovement.length() + 0.01D, 0.5D));
            setDeltaMovement(movement.x, Math.max(getDeltaMovement().y, 0) + gravity, movement.z);
            move(MoverType.SELF, getDeltaMovement());
        }
        
        if (!level.isClientSide()) {
            Iterator<INonStandPower> iter = passengerPowers.iterator();
            boolean infiniteEnergy = false;
            while (iter.hasNext()) {
                INonStandPower power = iter.next();
                if (power.getType() != ModPowers.HAMON.get()) {
                    iter.remove();
                }
                else if (!infiniteEnergy && power.isUserCreative()) {
                    infiniteEnergy = true;
                }
            }
            if (infiniteEnergy) {
                setEnergy(MAX_ENERGY);
            }
            else {
                setEnergy(Math.max(getEnergy() - 2, 0));
                float energyToReplenish = MAX_ENERGY - getEnergy();
                int hamonUsersWithEnergy = passengerPowers.size();
                while (energyToReplenish > 0 && hamonUsersWithEnergy > 0) {
                    float energyFromEach = energyToReplenish / hamonUsersWithEnergy;
                    for (INonStandPower power : passengerPowers) {
                        float energyConsumed = consumeEnergy(power, energyFromEach);
                        if (energyConsumed < energyFromEach) {
                            hamonUsersWithEnergy--;
                        }
                        energyToReplenish -= energyConsumed;
                    }
                }
                setEnergy(MAX_ENERGY - energyToReplenish);
            }
            
            if (getEnergy() <= 0) {
                setHealth(getHealth() - 0.04F);
            }
            else {
                setHealth(Math.min(getHealth() + 0.1F, MAX_HEALTH));
            }
            if (getHealth() <= 0) {
                remove();
            }
        }
        else {
            if (tickCount % 20 == 0 && getEnergy() > 0) {
                Vector3d sparkVec = position().add(
                        (random.nextDouble() - 0.5) * getBbWidth(), 
                        getBbHeight(),
                        (random.nextDouble() - 0.5) * getBbWidth());
                HamonPowerType.createHamonSparkParticles(level, ClientUtil.getClientPlayer(), sparkVec, getEnergy() * 0.0015F);
            }
        }
    }
    
    private float consumeEnergy(INonStandPower power, float energy) {
        energy = Math.min(energy, power.getEnergy());
        power.getTypeSpecificData(ModPowers.HAMON.get()).get().hamonPointsFromAction(HamonStat.CONTROL, energy);
        power.consumeEnergy(energy);
        return energy;
    }

    private void tickLerp() {
        if (isControlledByLocalInstance()) {
            lerpSteps = 0;
            setPacketCoordinates(getX(), getY(), getZ());
        }
        if (lerpSteps > 0) {
            double xLerp = getX() + (lerpX - getX()) / (double) lerpSteps;
            double yLerp = getY() + (lerpY - getY()) / (double) lerpSteps;
            double zLerp = getZ() + (lerpZ - getZ()) / (double) lerpSteps;
            double yRotLerp = MathHelper.wrapDegrees(lerpYRot - (double) yRot);
            yRot = (float) ((double) yRot + yRotLerp / (double) lerpSteps);
            xRot = (float) ((double) xRot + (lerpXRot - (double) xRot) / (double) lerpSteps);
            --lerpSteps;
            setPos(xLerp, yLerp, zLerp);
            setRot(yRot, xRot);
        }
    }

    @Override
    public void lerpTo(double lerpX, double lerpY, double lerpZ, float lerpYRot, float lerpZRot, int lerpSteps, boolean teleport) {
        this.lerpX = lerpX;
        this.lerpY = lerpY;
        this.lerpZ = lerpZ;
        this.lerpYRot = (double) lerpYRot;
        this.lerpXRot = (double) lerpZRot;
        this.lerpSteps = 10;
    }
    
    private void updateFlying() {
        boolean prevIsFlying = isFlying();
        boolean isFlying = !isOnGround() && !isInWaterOrBubble();
        if (prevIsFlying && !isFlying) {
            setDeltaMovement(Vector3d.ZERO);
            if (!level.isClientSide()) {
                ejectPassengers();
            }
        }
        setIsFlying(isFlying);
    }

    private void updateRotationDelta() {
        float d = 3.5F - (float) getPassengers().size() * 0.5F;
        if (!(inputLeft || inputRight)) {
            if (yRotDelta > 0) {
                yRotDelta = Math.max(yRotDelta - d * 0.05F, 0);
            }
            else if (yRotDelta < 0) {
                yRotDelta = Math.min(yRotDelta + d * 0.05F, 0);
            }
        }
        else {
            yRotDelta = 0;
            if (inputLeft) {
                yRotDelta -= d;
            }
            if (inputRight) {
                yRotDelta += d;
            }
        }
    }
    
    public void setInput(boolean left, boolean right) {
        this.inputLeft = left;
        this.inputRight = right;
    }
    
    @Override
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        if (player.isSecondaryUseActive() || this.is(player.getVehicle())) {
            return ActionResultType.PASS;
        } 
        if (!level.isClientSide()) {
            return player.startRiding(this) ? ActionResultType.CONSUME : ActionResultType.PASS;
        } 
        return ActionResultType.SUCCESS;
    }
    
    

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return getPassengers().size() < 4;
    }

    @Override
    public Entity getControllingPassenger() {
        return isVehicle() ? getPassengers().get(0) : null;
    }

    @Override
    protected void addPassenger(Entity entity) {
        if (!isVehicle()) {
            xRot = entity.xRot;
            yRot = entity.yRot;
            entity.setYBodyRot(entity.yRot);
            
            float minGap = 1;
            double groundGap = -MCUtil.collide(this, new Vector3d(0, -minGap, 0)).y;
            if (groundGap < minGap) {
                float liftUp = minGap - (float) groundGap;
                move(MoverType.SELF, new Vector3d(0, entity.getBbHeight() + liftUp, 0));
            }

            Vector3d riderMovement = entity.getDeltaMovement().multiply(1, 0, 1);
            Vector3d gliderRotVec = Vector3d.directionFromRotation(0, yRot);
            // FIXME add the entity's movement (when making a glider after leap)
            setDeltaMovement(gliderRotVec.scale(Math.max(riderMovement.dot(gliderRotVec), 0.05D)));
        }
        super.addPassenger(entity);
        if (isControlledByLocalInstance() && lerpSteps > 0) {
            lerpSteps = 0;
            absMoveTo(lerpX, lerpY, lerpZ, (float) lerpYRot, (float) lerpXRot);
        }
        updateBbHeight();
        if (!level.isClientSide() && entity instanceof LivingEntity) {
            INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                if (power.getType() == ModPowers.HAMON.get()) {
                    passengerPowers.add(power);
                }
            });
        }
    }

    @Override
    protected void removePassenger(Entity entity) {
        super.removePassenger(entity);
        updateBbHeight();
        if (!level.isClientSide() && entity instanceof LivingEntity) {
            INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> passengerPowers.remove(power));
        }
    }
    
    private void updateBbHeight() {
        passengersHeight = isVehicle() ? getPassengers().stream().max(Comparator.comparingDouble(Entity::getBbHeight)).get().getBbHeight() : 0;
        refreshDimensions();
    }

    @Override
    public EntitySize getDimensions(Pose pose) {
        EntitySize defaultSize = super.getDimensions(pose);
        return new EntitySize(defaultSize.width, defaultSize.height + passengersHeight, defaultSize.fixed);
    }

    

    private static final Vector3d[] OFFSETS = {
        new Vector3d(0, 0, 0.625), 
        new Vector3d(0.625, 0, 0), 
        new Vector3d(-0.625, 0, 0), 
        new Vector3d(0, 0, -0.625)
    };
    @Override
    public void positionRider(Entity entity) {
        if (hasPassenger(entity)) {
            int i = getPassengers().indexOf(entity);
            if (i < 4) {
                Vector3d rotatedVec = OFFSETS[i].yRot(-yRot * MathUtil.DEG_TO_RAD);
                entity.setPos(
                        getX() + rotatedVec.x, 
                        getY(1.0) - super.getDimensions(Pose.STANDING).height - entity.getBbHeight(), 
                        getZ() + rotatedVec.z);
            }
        }
    }
    
    

    @Override
    public boolean hurt(DamageSource dmgSource, float amount) {
        if (isInvulnerableTo(dmgSource)) {
            return false;
        }
        Entity source = dmgSource.getDirectEntity();
        if (source != null && this.is(source.getVehicle())) {
            return false;
        }
        if (!level.isClientSide && isAlive()) {
            if (source instanceof LivingEntity) {
                float energy = Math.min(getEnergy(), 100);
                DamageUtil.dealHamonDamage((LivingEntity) source, energy * 0.04F, this, null);
                setEnergy(getEnergy() - energy);
            }
            setHealth(getHealth() - amount);
            markHurt();
            return true;
        } else {
            return true;
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource dmgSource) {
       return super.isInvulnerableTo(dmgSource) || "hamon".startsWith(dmgSource.getMsgId());
    }
    
    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier) {
        return false;
    }

    @Override
    public void push(Entity entity) {}

    @Override
    public boolean isPickable() {
        return true;
    }

    private void setIsFlying(boolean isGliding) {
        entityData.set(IS_FLYING, isGliding);
    }

    public boolean isFlying() {
        return entityData.get(IS_FLYING);
    }
    
    public float getYRotDelta() {
        return yRotDelta;
    }

    public void setEnergy(float energy) {
        entityData.set(ENERGY, energy);
    }
    
    public float getEnergy() {
        return entityData.get(ENERGY);
    }
    
    @Override
    public float getHealth() {
        return entityData.get(HEALTH);
    }

    public void setHealth(float health) {
        entityData.set(HEALTH, MathHelper.clamp(health, 0, getMaxHealth()));
    }

    @Override
    public float getMaxHealth() {
        return MAX_HEALTH;
    }
    
    public void setLeavesBlock(BlockState block) {
        if (block != null && block.getBlock() != Blocks.AIR) {
            this.leavesBlock = block;
            this.leavesBlockTex = null;
        }
    }
    
    public BlockState getLeavesBlock() {
        return leavesBlock;
    }
    
    @Nullable
    public ResourceLocation getLeavesTexture() {
        return leavesBlockTex;
    }
    
    public void setLeavesTex(ResourceLocation texture) {
        this.leavesBlockTex = texture;
    }
    
    public void setFoliageColor(int color) {
        this.foliageColor = color;
    }
    
    public int getFoliageColor() {
        return foliageColor;
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> parameter) {
        super.onSyncedDataUpdated(parameter);
        if (level.isClientSide() && IS_FLYING.equals(parameter) && isFlying()) {
            ClientTickingSoundsHelper.playGliderFlightSound(this);
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(IS_FLYING, false);
        entityData.define(ENERGY, MAX_ENERGY);
        entityData.define(HEALTH, MAX_HEALTH);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        setIsFlying(nbt.getBoolean("Flight"));
        if (nbt.contains("Energy")) setEnergy(nbt.getFloat("Energy"));
        if (nbt.contains("Health")) setHealth(nbt.getFloat("Health"));
        if (nbt.contains("Color"))  foliageColor = nbt.getInt("Color");
        if (nbt.contains("Block", MCUtil.getNbtId(CompoundNBT.class))) {
            setLeavesBlock(NBTUtil.readBlockState(nbt.getCompound("Block")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putBoolean("Flight", isFlying());
        nbt.putFloat("Energy", getEnergy());
        nbt.putFloat("Health", getHealth());
        if (foliageColor >= 0) {
            nbt.putInt("Color", foliageColor);
        }
        nbt.put("Block", NBTUtil.writeBlockState(leavesBlock));
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(foliageColor);
        
        buffer.writeVarInt(Block.getId(leavesBlock));
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        foliageColor = additionalData.readInt();
        if (foliageColor < 0) {
            foliageColor = ClientUtil.getFoliageColor(Blocks.OAK_LEAVES.defaultBlockState(), level, this.blockPosition());
            PacketManager.sendToServer(new ClLeavesGliderColorPacket(getId(), foliageColor));
        }
        
        setLeavesBlock(GameData.getBlockStateIDMap().byId(additionalData.readVarInt()));
    }

}
