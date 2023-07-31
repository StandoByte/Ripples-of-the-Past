package com.github.standobyte.jojo.entity;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.general.PlaneRectangle;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class HamonProjectileShieldEntity extends Entity implements IEntityAdditionalSpawnData {
    private LivingEntity user;
    private INonStandPower power;
    private HamonData hamon;
    
    private float width;
    private float height;
    private PlaneRectangle shieldPlane;
    
    public HamonProjectileShieldEntity(World world, @Nonnull LivingEntity hamonUser, float width, float height) {
        this(ModEntityTypes.HAMON_PROJECTILE_SHIELD.get(), world);
        this.user = hamonUser;
        this.power = INonStandPower.getNonStandPowerOptional(hamonUser).orElse(null);
        if (power != null) {
            hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).orElse(null);
        }
        this.width = width;
        this.height = height;
        refreshDimensions();
    }

    public HamonProjectileShieldEntity(EntityType<?> type, World world) {
        super(type, world);
    }
    
    
    @Override
    public void tick() {
        super.tick();
        if (user == null || !user.isAlive() || !level.isClientSide() && 
                (power == null || power.getHeldAction() != ModHamonActions.HAMON_PROJECTILE_SHIELD.get() || hamon == null)) {
            if (!level.isClientSide()) remove();
            return;
        }
        updateShieldPos();
        
        Vector3d shieldPlaneNormal = getLookAngle();
        Vector3d shieldCenter = shieldPlane.center;
        level.getEntitiesOfClass(ProjectileEntity.class, getBoundingBox().inflate(8), 
                entity -> entity.isAlive()).forEach(projectile -> {
                    RayTraceResult rayTrace = ProjectileHelper.getHitResult(projectile, 
                            target -> target != this && !target.isSpectator() && target.isAlive() && !target.is(projectile.getOwner()));
                    if (rayTrace.getType() != RayTraceResult.Type.BLOCK) {
                        Vector3d deltaMov = projectile.getDeltaMovement();
                        Vector3d posCur = projectile.position();
                        Vector3d posNext = posCur.add(deltaMov);
                        double normalProjCur = posCur.subtract(shieldCenter).dot(shieldPlaneNormal);
                        double normalProjNext = posNext.subtract(shieldCenter).dot(shieldPlaneNormal);
                        if (normalProjCur > 0 && // current position is in front of the shield
                                normalProjNext <= 0 /* next position would be behind the shield */ ) {
                            double penetrationRatio = normalProjCur / (normalProjCur - normalProjNext);
                            Vector3d intersectionPoint = posCur.add(deltaMov.scale(penetrationRatio));
                            
                            Vector3d centerToIntersectionVec = intersectionPoint.subtract(shieldCenter);
                            Vector3d horizontalShieldVec = shieldPlane.pRD.subtract(shieldPlane.pLD);
                            Vector3d verticalShieldVec = shieldPlane.pLU.subtract(shieldPlane.pLD);
                            boolean intersectsInShieldBounds = 
                                    Math.abs(centerToIntersectionVec.dot(horizontalShieldVec)) <= width * width / 2 && // comparing the projections length with extra steps
                                    Math.abs(centerToIntersectionVec.dot(verticalShieldVec)) <= height * height / 2;
                            
                            if (intersectsInShieldBounds) {
                                deflectProjectile(projectile, intersectionPoint);
                            }
                        }
                    }
                    
                });
        
        if (level.isClientSide()) {
            int particlesCount = (int) (width * height * 0.1F);
            for (int i = 0; i < particlesCount; i++) {
                Vector3d pos = shieldPlane.getUniformRandomPos();
                level.addParticle(ModParticles.HAMON_SPARK.get(), pos.x, pos.y, pos.z, 0, 0, 0);
            }
            HamonSparksLoopSound.playSparkSound(this, getBoundingBox().getCenter(), 1.0F);
        }
    }
    
    public void updateShieldPos() {
        Vector3d shieldPos = new Vector3d(user.getX(), user.getY(0.5F) - height * 0.5F, user.getZ())
                .add(new Vector3d(0, 0, 2F)
                .xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD));
        setPos(shieldPos.x, shieldPos.y, shieldPos.z);
        
        Vector3d center = getBoundingBox().getCenter();
        Vector3d offset = new Vector3d(width / 2, height / 2, 0)
                .xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD);
        Vector3d offset2 = new Vector3d(width / 2, -height / 2, 0)
                .xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD);
        
        this.shieldPlane = PlaneRectangle.clockwisePoints(
                center.add(offset), 
                center.add(offset2), 
                center.add(offset.reverse()));
    }
    
    public PlaneRectangle getShieldRectangle() {
        return shieldPlane;
    }
    
    @Override
    public void push(Entity entity) {}

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }
    
    @Override
    public boolean hurt(DamageSource dmgSource, float amount) {
        return false;
    }
    
    private void deflectProjectile(ProjectileEntity projectile, Vector3d intersectionPoint) {
        if (projectile == null) return;
        float speed = (float) projectile.getDeltaMovement().length();
        if (power != null && hamon != null) {
            float energyCost = speed * 20;
            if (power.hasEnergy(energyCost)) {
                JojoModUtil.deflectProjectile(projectile, null);
            }
            if (!level.isClientSide()) {
                if (power.consumeEnergy(energyCost)) {
                    DamageUtil.dealHamonDamage(projectile, 0.1F, this, user);
                    hamon.hamonPointsFromAction(HamonStat.CONTROL, energyCost);
                }
                else {
                    power.setEnergy(0);
                    remove();
                }
            }
        }
        if (level.isClientSide()) {
            HamonUtil.emitHamonSparkParticles(level, ClientUtil.getClientPlayer(), intersectionPoint, 5F);
        }
    }
    
    @Override
    public void setBoundingBox(AxisAlignedBB aabb) {
        super.setBoundingBox(aabb);
        Vector3d center = aabb.getCenter();
        
        Vector3d offset = new Vector3d(-width / 2, -height / 2, 0)
                .xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD);
        Vector3d offset2 = new Vector3d(-width / 2, height / 2, 0)
                .xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD);
        
        this.shieldPlane = PlaneRectangle.clockwisePoints(
                center.add(offset), 
                center.add(offset2), 
                center.add(offset.reverse()));
    }
    
    @Override
    public EntitySize getDimensions(Pose pose) {
        EntitySize defaultSize = super.getDimensions(pose);
        return new EntitySize(width, height, defaultSize.fixed);
    }
    
    @Override
    public void setPos(double pX, double pY, double pZ) {
        this.setPosRaw(pX, pY, pZ);
        AxisAlignedBB aabb = this.getDimensions(null).makeBoundingBox(pX, pY, pZ);
        this.setBoundingBox(aabb);
    }
    
    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        width = nbt.getFloat("Width");
        height = nbt.getFloat("Height");
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putFloat("Width", width);
        nbt.putFloat("Height", height);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeFloat(width);
        buffer.writeFloat(height);
        
        buffer.writeInt(user.getId());
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        this.width = additionalData.readFloat();
        this.height = additionalData.readFloat();
        absMoveTo(xo, yo, zo, yRot, xRot);
        
        Entity entity = level.getEntity(additionalData.readInt());
        if (entity instanceof LivingEntity) {
            user = (LivingEntity) entity;
            this.power = INonStandPower.getNonStandPowerOptional(user).orElse(null);
            if (power != null) {
                hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).orElse(null);
            }
        }
    }

}
