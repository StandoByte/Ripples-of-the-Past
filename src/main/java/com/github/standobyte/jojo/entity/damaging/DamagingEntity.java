package com.github.standobyte.jojo.entity.damaging;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.IModdedDamageSource;
import com.github.standobyte.jojo.util.mc.damage.IStandDamageSource;
import com.github.standobyte.jojo.util.mc.damage.IndirectStandEntityDamageSource;
import com.github.standobyte.jojo.util.mc.damage.ModdedDamageSourceWrapper;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.TNTBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class DamagingEntity extends ProjectileEntity implements IEntityAdditionalSpawnData {
    protected static final Vector3d DEFAULT_POS_OFFSET = new Vector3d(0.0D, -0.3D, 0.0D);
    private float damageFactor = 1F;
    // only used for OwnerBoundProjectileEntity
    protected float speedFactor = 1F;
    private LivingEntity livingEntityOwner = null;
    private LazyOptional<IStandPower> userStandPower = LazyOptional.empty();
    private LazyOptional<INonStandPower> userNonStandPower = LazyOptional.empty();
    private Optional<ResourceLocation> standSkin = Optional.empty();

    public DamagingEntity(EntityType<? extends DamagingEntity> entityType, @Nullable LivingEntity owner, World world) {
        this(entityType, world);
        if (owner != null) {
            setOwner(owner);
            this.livingEntityOwner = owner;
            Vector3d pos = getPos(owner, 1.0F, owner.yRot, owner.xRot);
            setPos(pos.x, pos.y, pos.z);
            setRot(owner.yRot, owner.xRot);
        }
    }

    public DamagingEntity(EntityType<? extends DamagingEntity> entityType, World world) {
        super(entityType, world);
    }
    
    public void setShootingPosOf(LivingEntity entity) {
        Vector3d pos = getPos(entity, 1.0F, entity.yRot, entity.xRot);
        setPos(pos.x, pos.y, pos.z);
        setRot(entity.yRot, entity.xRot);
    }
    
    public void withStandSkin(Optional<ResourceLocation> standSkin) {
        this.standSkin = standSkin;
    }
    
    protected final Vector3d getPos(LivingEntity owner, float partialTick, float yRot, float xRot) {
        return owner.getEyePosition(partialTick)
                .add(MathUtil.relativeVecToAbsolute(getOwnerRelativeOffset().add(getXRotOffset().xRot(-owner.xRot * MathUtil.DEG_TO_RAD)), yRot));
    }
    
    protected Vector3d getOwnerRelativeOffset() {
        return DEFAULT_POS_OFFSET;
    }
    
    protected Vector3d getXRotOffset() {
        return Vector3d.ZERO;
    }
    
    @Override
    public LivingEntity getOwner() {
        if (livingEntityOwner == null) {
            Entity owner = super.getOwner();
            if (owner == null) {
                return null;
            }
            if (owner instanceof LivingEntity) {
                livingEntityOwner = (LivingEntity) owner;
            }
        }
        return livingEntityOwner;
    }
    
    @Override
    public void setOwner(Entity owner) {
        super.setOwner(owner);
        userStandPower = LazyOptional.empty();
        userNonStandPower = LazyOptional.empty();
    }
    
    protected LazyOptional<IStandPower> getUserStandPower() {
        if (!userStandPower.isPresent()) {
            userStandPower = IStandPower.getStandPowerOptional(StandUtil.getStandUser(getOwner()));
        }
        return userStandPower;
    }
    
    protected LazyOptional<INonStandPower> getUserNonStandPower() {
        if (!userNonStandPower.isPresent()) {
            userNonStandPower = INonStandPower.getNonStandPowerOptional(StandUtil.getStandUser(getOwner()));
        }
        return userNonStandPower;
    }
    
    @Override
    public void tick() {
        super.tick();
        checkInsideBlocks();
        checkHit();
    }
    
    protected void checkHit() {
        RayTraceResult[] rayTrace = rayTrace();
        for (RayTraceResult result : rayTrace) {
            if (result.getType() != RayTraceResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, result)) {
                onHit(result);
            }
        }
    }

    protected RayTraceResult[] rayTrace() {
        return new RayTraceResult[] { ProjectileHelper.getHitResult(this, this::canHitEntity) };
    }
    
    @Override
    protected void onHitEntity(EntityRayTraceResult entityRayTraceResult) {
        if (!level.isClientSide() && isAlive()) {
            Entity target = entityRayTraceResult.getEntity();
            LivingEntity owner = getOwner();
            boolean entityHurt = hurtTarget(target, owner);
            int prevTargetFireTimer = target.getRemainingFireTicks();
            if (isOnFire()) {
                target.setSecondsOnFire(5);
            }
            if (entityHurt) {
                if (owner instanceof StandEntity && target instanceof LivingEntity) {
                    LivingEntity standUser = ((StandEntity) owner).getUser();
                    if (standUser != null) {
                        LivingEntity livingTarget = (LivingEntity) target;
                        if (standUser instanceof PlayerEntity) {
                            livingTarget.setLastHurtByPlayer((PlayerEntity) standUser);
                            livingTarget.lastHurtByPlayerTime = 100;
                        }
                        livingTarget.setLastHurtByMob(standUser);
                    }
                }
            }
            else {
                target.setRemainingFireTicks(prevTargetFireTimer);
            }
            afterEntityHit(entityRayTraceResult, entityHurt);
        }
        super.onHitEntity(entityRayTraceResult);
    }
    
    protected boolean checkPvpRules() {
        return true;
    }
    
    protected boolean hurtTarget(Entity target, @Nullable LivingEntity owner) {
        return hurtTarget(target, getDamageSource(owner), getDamageAmount());
    }
    
    protected boolean hurtTarget(Entity target, DamageSource dmgSource, float dmgAmount) {
        return DamageUtil.hurtThroughInvulTicks(target, DamageUtil.enderDragonDamageHack(dmgSource, target), dmgAmount);
    }
    
    protected DamageSource getDamageSource(LivingEntity owner) { // TODO damage sources/death messages
        DamageSource damageSource = standDamage() ? new IndirectStandEntityDamageSource("arrow", this, owner).setProjectile() :
            new IndirectEntityDamageSource("arrow", this, owner).setProjectile();
        
        float knockbackReduction = knockbackMultiplier();
        if (knockbackReduction < 1) {
            if (!(damageSource instanceof IModdedDamageSource)) {
                damageSource = new ModdedDamageSourceWrapper(damageSource);
            }
            ((IModdedDamageSource) damageSource).setKnockbackReduction(Math.max(knockbackReduction, 0));
        }
        
        return damageSource;
    }
    
    protected float knockbackMultiplier() {
        return 1F;
    }
    
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {}

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (super.canHitEntity(entity)) {
            LivingEntity owner = getOwner();
            if (owner == null) {
                return true;
            }
            if (entity instanceof LivingEntity) {
                if (entity.is(owner) || owner instanceof StandEntity && entity.is(((StandEntity) owner).getUser())) {
                    return canHitOwner();
                }
                else {
                    return owner.canAttack((LivingEntity) entity);
                }
            }
            return !(checkPvpRules() && 
                    owner instanceof StandEntity && !((StandEntity) owner).canHarm(entity) || 
                    owner instanceof PlayerEntity && entity instanceof PlayerEntity && !((PlayerEntity) owner).canHarmPlayer((PlayerEntity) entity));
        }
        return false;
    }
    
    public boolean canHitOwner() {
        return false;
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult blockRayTraceResult) {
        super.onHitBlock(blockRayTraceResult);
        if (!level.isClientSide() && isAlive()) {
            BlockPos blockPos = blockRayTraceResult.getBlockPos();
            LivingEntity owner = getOwner();
            boolean brokenBlock = owner != null && !JojoModUtil.canEntityDestroy((ServerWorld) level, blockPos, level.getBlockState(blockPos), owner) ? 
                    false
                    : destroyBlock(blockRayTraceResult);
            afterBlockHit(blockRayTraceResult, brokenBlock);
        }
    }
    
    protected boolean destroyBlock(BlockRayTraceResult blockRayTraceResult) {
        BlockPos blockPos = blockRayTraceResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        Direction face = blockRayTraceResult.getDirection();
        boolean brokenBlock = canBreakBlock(blockPos, blockState);
        if (isFiery() && blockState.isFlammable(level, blockPos, face)) {
            blockState.catchFire(level, blockPos, face, getOwner());
            if (blockState.getBlock() instanceof TNTBlock) level.removeBlock(blockPos, false);
            return false;
        }
        if (brokenBlock) {
            LivingEntity ownerOrStandUser = getOwner();
            if (ownerOrStandUser instanceof StandEntity) {
                ownerOrStandUser = ((StandEntity) ownerOrStandUser).getUser();
            }
            boolean dropItem = ownerOrStandUser instanceof PlayerEntity ? !((PlayerEntity) ownerOrStandUser).abilities.instabuild : true;
            brokenBlock = level.destroyBlock(blockPos, dropItem, getOwner());
        }
        return brokenBlock;
    }
    
    protected boolean canBreakBlock(BlockPos blockPos, BlockState blockState) {
        float hardness = blockState.getDestroySpeed(level, blockPos);
        return hardness >= 0 && hardness <= getMaxHardnessBreakable();
    }
    
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean blockDestroyed) {}
    
    public boolean isFiery() {
        return false;
    }
    
    public abstract int ticksLifespan();

    protected abstract float getBaseDamage();
    
    protected float getDamageAmount() {
        float configMultiplier = standDamage() || getOwner() instanceof StandEntity ? JojoModConfig.getCommonConfigInstance(false).standDamageMultiplier.get().floatValue() : 1;
        float damage = getBaseDamage() * configMultiplier;
        if (debuffsFromStand()) {
            damage *= damageFactor;
        }
        return damage;
    }
    
    protected float getDamageFinalCalc(float damage) {
        return damage;
    }
    
    public void setDamageFactor(float damageFactor) {
        this.damageFactor = damageFactor;
    }
    
    public float getDamageFactor() {
        return damageFactor;
    }
    
    public void setSpeedFactor(float speedFactor) {
        this.speedFactor = speedFactor;
    }
    
    public float getSpeedFactor() {
        return speedFactor;
    }
    
    protected boolean debuffsFromStand() {
        return true;
    }
    
    protected abstract float getMaxHardnessBreakable();
    
    public abstract boolean standDamage();
    
    protected boolean standVisibility() {
        return standDamage();
    }

    @Override
    public boolean isInvisible() {
        return standVisibility() || super.isInvisible();
    }

    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        return standVisibility() && !StandUtil.clStandEntityVisibleTo(player) || !player.isSpectator() && super.isInvisible();
    }
    
    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        if (!this.isSilent()) {
            if (standVisibility()) {
                MCUtil.playSound(level, null, getX(), getY(), getZ(), sound, getSoundSource(), volume, pitch, StandUtil::playerCanHearStands);
            }
            else {
                level.playSound(null, getX(), getY(), getZ(), sound, getSoundSource(), volume, pitch);
            }
        }
    }
    
    @Override
    public boolean displayFireAnimation() {
        return super.displayFireAnimation() && (!isInvisible() || !isInvisibleTo(ClientUtil.getClientPlayer()));
    }
    
    @Override
    public Team getTeam() {
        LivingEntity owner = getOwner();
        return owner == null ? super.getTeam() : owner.getTeam();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (standDamage() && !(source instanceof IStandDamageSource)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public void moveTo(double x, double y, double z, float yRot, float xRot) {
        Vector3d pos = position();
        this.xo = pos.x;
        this.yo = pos.y;
        this.zo = pos.z;
        this.xOld = pos.x;
        this.yOld = pos.y;
        this.zOld = pos.z;
        setPosRaw(x, y, z);
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
        this.yRot = yRot;
        this.xRot = xRot;
        this.reapplyPosition();
    }
    
    public Optional<ResourceLocation> getStandSkin() {
        return standSkin;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("DamageFactor", damageFactor);
        nbt.putFloat("SpeedFactor", speedFactor);
        nbt.putInt("Age", tickCount);
        standSkin.ifPresent(path -> nbt.putString("StandSkin", path.toString()));
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        damageFactor = nbt.getFloat("DamageFactor");
        speedFactor = nbt.getFloat("SpeedFactor");
        tickCount = nbt.getInt("Age");
        standSkin = MCUtil.getNbtElement(nbt, "StandSkin", StringNBT.class)
                .map(StringNBT::getAsString)
                .map(ResourceLocation::new);
    }

    @Override
    protected void defineSynchedData() {}
    
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(tickCount);
        buffer.writeFloat(speedFactor);
        NetworkUtil.writeOptional(buffer, standSkin, path -> buffer.writeResourceLocation(path));
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        tickCount = additionalData.readInt();
        speedFactor = additionalData.readFloat();
        standSkin = NetworkUtil.readOptional(additionalData, () -> additionalData.readResourceLocation());
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
