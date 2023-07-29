package com.github.standobyte.jojo.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClLeavesGliderColorPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.client.entity.player.ClientPlayerEntity;
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
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
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
    private static final int MAX_PASSENGERS = 4;

    private static final DataParameter<Boolean> IS_FLYING = EntityDataManager.defineId(LeavesGliderEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> ENERGY = EntityDataManager.defineId(LeavesGliderEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> HEALTH = EntityDataManager.defineId(LeavesGliderEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Byte> HAMON_USERS_CHARGING = EntityDataManager.defineId(LeavesGliderEntity.class, DataSerializers.BYTE);
    
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
                        // FIXME also turn passengers other than the controlling player
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
            rechargeFromHamonUsers();
            
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
            float energy = getEnergy();
            if (energy > 0) {
                boolean[] chargingHamonUsers = getHamonChargers();
                boolean isBeingCharged = false;
                for (int i = 0; i < MAX_PASSENGERS; i++) {
                    if (chargingHamonUsers[i] && i < getPassengers().size()) {
                        Entity charger = getPassengers().get(i);
                        if (charger != null && charger.isAlive() && charger instanceof LivingEntity) {
                            CustomParticlesHelper.createHamonGliderChargeParticles((LivingEntity) charger);
                            isBeingCharged = true;
                        }
                        
                    }
                }
                
                float energyRatio = energy / MAX_ENERGY;
                Vector3d soundPos = clSoundPos();
                if (isBeingCharged || random.nextFloat() < energyRatio * 0.2F) {
                    HamonSparksLoopSound.playSparkSound(this, soundPos, energyRatio);
                    CustomParticlesHelper.createHamonSparkParticles(this, this.getRandomX(0.5F), this.getY(1.0F), this.getRandomZ(0.5F), 
                            MathUtil.fractionRandomInc(energyRatio * 2));
                }
            }
            
            for (Entity passenger : getPassengers()) { 
                if (passenger instanceof ClientPlayerEntity) {
                    ClientReflection.setHandsBusy((ClientPlayerEntity) passenger, true);
                }
            }
        }
    }
    
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (level.isClientSide()) {
            Vector3d soundPos = clSoundPos();
            SoundType soundType = leavesBlock.getSoundType();
            level.playLocalSound(soundPos.x, soundPos.y, soundPos.z, 
                    soundType.getBreakSound(), getSoundSource(), 
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
            
            addLeavesParticles(200);
        }
    }
    
    private void rechargeFromHamonUsers() {
        boolean[] hamonUsersCharging = new boolean[MAX_PASSENGERS];
        
        Iterator<INonStandPower> iter = passengerPowers.iterator();
        boolean infiniteEnergy = false;
        while (iter.hasNext()) {
            INonStandPower power = iter.next();
            if (power.getType() != ModPowers.HAMON.get()) {
                iter.remove();
            }
            else if (power.isUserCreative()) {
                infiniteEnergy = true;
                addPassengerIndex(hamonUsersCharging, power.getUser());
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
                    if (energyConsumed > 0) {
                        addPassengerIndex(hamonUsersCharging, power.getUser());
                    }
                    if (energyConsumed < energyFromEach) {
                        hamonUsersWithEnergy--;
                    }
                    energyToReplenish -= energyConsumed;
                }
            }
            setEnergy(MAX_ENERGY - energyToReplenish);
        }
        
        setHamonChargers(hamonUsersCharging);
    }
    
    private float consumeEnergy(INonStandPower power, float energy) {
        energy = Math.min(energy, power.getEnergy());
        power.getTypeSpecificData(ModPowers.HAMON.get()).get().hamonPointsFromAction(HamonStat.CONTROL, energy);
        power.consumeEnergy(energy);
        return energy;
    }
    
    private void addPassengerIndex(boolean[] arr, Entity passenger) {
        int index = getPassengers().indexOf(passenger);
        if (index >= 0) {
            arr[index] = true;
        }
    }
    
    private void setHamonChargers(boolean[] passengerIndices) {
        byte data = 0;
        for (int i = MAX_PASSENGERS - 1; i >= 0; i--) {
            data <<= 1;
            if (passengerIndices[i]) {
                data |= 1;
            }
        }
        entityData.set(HAMON_USERS_CHARGING, data);
    }
    
    private boolean[] getHamonChargers() {
        boolean[] passengerIndices = new boolean[MAX_PASSENGERS];
        byte data = entityData.get(HAMON_USERS_CHARGING);
        for (int i = 0; i < MAX_PASSENGERS; i++) {
            passengerIndices[i] = (data & 1) > 0;
            data >>= 1;
        }
        return passengerIndices;
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
        return getPassengers().size() < MAX_PASSENGERS;
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
            if (i < MAX_PASSENGERS) {
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
        return !level.isClientSide() || ClientUtil.getClientPlayer().getRootVehicle() != this.getRootVehicle();
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
    
    private float prevHealth = 0;
    @Override
    public void onSyncedDataUpdated(DataParameter<?> parameter) {
        super.onSyncedDataUpdated(parameter);
        if (level.isClientSide()) {
            if (IS_FLYING.equals(parameter) && isFlying()) {
                ClientTickingSoundsHelper.playGliderFlightSound(this);
            }
            else if (HEALTH.equals(parameter)) {
                float health = entityData.get(HEALTH);
                if (health < prevHealth) {
                    float diff = prevHealth - health;
                    addLeavesParticles(Math.max((int) (diff * 100), 1));
                    
                    SoundType soundType = leavesBlock.getSoundType();
                    Vector3d soundPos = clSoundPos();
                    level.playLocalSound(soundPos.x, soundPos.y, soundPos.z, 
                            soundType.getHitSound(), getSoundSource(), 
                            (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.8F, false);
                }
                prevHealth = health;
            }
        }
    }
    
    private Vector3d clSoundPos() {
        PlayerEntity clientPlayer = ClientUtil.getClientPlayer();
        return clientPlayer.getVehicle() == this ? 
                new Vector3d(clientPlayer.getX(), this.getY(1.0F), clientPlayer.getZ()) 
                : new Vector3d(this.getX(), this.getY(1.0F), this.getZ());
    }
    
    private void addLeavesParticles(int count) {
        IParticleData leavesParticle = new BlockParticleData(ParticleTypes.BLOCK, leavesBlock);
        for (int i = 0; i < count; i++) {
            level.addParticle(leavesParticle, 
                    getRandomX(0.5F), 
                    getY(1.0F), 
                    getRandomZ(0.5F), 0, 0, 0);
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(IS_FLYING, false);
        entityData.define(ENERGY, MAX_ENERGY);
        entityData.define(HEALTH, MAX_HEALTH);
        entityData.define(HAMON_USERS_CHARGING, (byte) 0);
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
        buffer.writeVarInt(Block.getId(leavesBlock));
        buffer.writeInt(foliageColor);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        setLeavesBlock(GameData.getBlockStateIDMap().byId(additionalData.readVarInt()));
        
        foliageColor = additionalData.readInt();
        if (foliageColor < 0) {
            foliageColor = ClientUtil.getFoliageColor(leavesBlock, level, this.blockPosition());
            PacketManager.sendToServer(new ClLeavesGliderColorPacket(getId(), foliageColor));
        }
    }

}
