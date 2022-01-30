package com.github.standobyte.jojo.entity.damaging;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.damage.IStandDamageSource;
import com.github.standobyte.jojo.util.damage.IndirectStandEntityDamageSource;
import com.github.standobyte.jojo.util.damage.ModDamageSources;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class DamagingEntity extends ProjectileEntity implements IEntityAdditionalSpawnData {
    protected static final Vector3d DEFAULT_POS_OFFSET = new Vector3d(0.0D, -0.3D, 0.0D);
    protected float damageFactor = 1.0F; // FIXME set it automatically
    private LivingEntity livingEntityOwner = null;

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
    public void tick() {
        super.tick();
        checkInsideBlocks();
        checkHit();
    }
    
    protected final void checkHit() {
        RayTraceResult rayTraceResult = rayTrace();
        if (rayTraceResult.getType() != RayTraceResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, rayTraceResult)) {
            onHit(rayTraceResult);
        }
    }

    protected RayTraceResult rayTrace() {
        return ProjectileHelper.getHitResult(this, this::canHitEntity);
    }
    
    @Override
    protected void onHitEntity(EntityRayTraceResult entityRayTraceResult) {
        if (!level.isClientSide()) {
            Entity target = entityRayTraceResult.getEntity();
            LivingEntity owner = getOwner();
            boolean entityHurt;
            if (checkPvpRules() && 
                    owner instanceof StandEntity && !((StandEntity) owner).canHarm(target) || 
                    owner instanceof PlayerEntity && target instanceof PlayerEntity && !((PlayerEntity) owner).canHarmPlayer((PlayerEntity) target)) {
                    entityHurt = false;
            }
            else {
                entityHurt = hurtTarget(target, owner);
            }
            if (entityHurt) {
                if (owner instanceof StandEntity && target instanceof LivingEntity) {
                    LivingEntity standUser = ((StandEntity) owner).getUser();
                    if (standUser instanceof PlayerEntity) {
                        LivingEntity livingTarget = (LivingEntity) target;
                        livingTarget.setLastHurtByPlayer((PlayerEntity) standUser);
                        livingTarget.lastHurtByPlayerTime = 100;
                    }
                }
                if (isOnFire()) {
                    target.setSecondsOnFire(5);
                }
            }
            afterEntityHit(entityRayTraceResult, entityHurt);
        }
        super.onHitEntity(entityRayTraceResult);
    }
    
    protected boolean checkPvpRules() {
        return true;
    }
    
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return hurtTarget(target, getDamageSource(owner), getBaseDamage() * getDamageFactor());
    }
    
    protected boolean hurtTarget(Entity target, DamageSource dmgSource, float dmgAmount) {
        return ModDamageSources.hurtThroughInvulTicks(target, dmgSource, dmgAmount);
    }
    
    protected DamageSource getDamageSource(LivingEntity owner) { // TODO damage sources/death messages
        return standDamage() ? new IndirectStandEntityDamageSource("arrow", this, owner).setProjectile() :
            new IndirectEntityDamageSource("arrow", this, owner).setProjectile();
    }
    
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {}

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (super.canHitEntity(entity)) {
            if (entity instanceof LivingEntity) {
                LivingEntity owner = getOwner();
                if (owner == null) {
                    return true;
                }
                if (entity.is(owner)) {
                    return canHitOwner();
                }
                else {
                    return owner.canAttack((LivingEntity) entity);
                }
            }
            return true;
        }
        return false;
    }
    
    protected boolean canHitOwner() {
        return false;
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult blockRayTraceResult) {
        if (!level.isClientSide()) {
            BlockPos blockPos = blockRayTraceResult.getBlockPos();
            LivingEntity owner = getOwner();
            boolean brokenBlock = owner != null && !JojoModUtil.canEntityDestroy(level, blockPos, owner) ? 
                    false
                    : destroyBlock(blockRayTraceResult);
            afterBlockHit(blockRayTraceResult, brokenBlock);
        }
        super.onHitBlock(blockRayTraceResult);
    }
    
    protected boolean destroyBlock(BlockRayTraceResult blockRayTraceResult) {
        BlockPos blockPos = blockRayTraceResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        boolean brokenBlock = canBreakBlock(blockPos, blockState);
        if (brokenBlock) {
            LivingEntity ownerOrStandUser = getOwner();
            if (ownerOrStandUser instanceof StandEntity) {
                ownerOrStandUser = ((StandEntity) ownerOrStandUser).getUser();
            }
            boolean dropItem = ownerOrStandUser instanceof PlayerEntity ? !((PlayerEntity) ownerOrStandUser).abilities.instabuild : true;
            brokenBlock = level.destroyBlock(blockPos, dropItem);
        }
        return brokenBlock;
    }
    
    protected boolean canBreakBlock(BlockPos blockPos, BlockState blockState) {
        float hardness = blockState.getDestroySpeed(level, blockPos);
        return hardness >= 0 && hardness <= getMaxHardnessBreakable();
    }
    
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean blockDestroyed) {}
    
    protected float getDamageFactor() {
        return damageFactor;
    }
    
    // FIXME a method in StandEntity which does this automatically
    public void setDamageFactor(float damageFactor) {
        this.damageFactor = MathHelper.clamp(damageFactor, 0.0F, 1.0F);
    }
    
    protected abstract int ticksLifespan();
    
    public abstract float getBaseDamage();
    
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
        return !player.isSpectator() && (standVisibility() && !ClientUtil.shouldStandsRender(player) || super.isInvisible());
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

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("DamageFactor", damageFactor);
        nbt.putInt("Age", tickCount);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        damageFactor = nbt.getFloat("DamageFactor");
        tickCount = nbt.getInt("Age");
    }

    @Override
    protected void defineSynchedData() {}
    
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(tickCount);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        tickCount = additionalData.readInt();
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
