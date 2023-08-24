package com.github.standobyte.jojo.entity.itemprojectile;

import java.util.Arrays;
import java.util.Optional;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.StandArrowItem;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class StandArrowEntity extends AbstractArrowEntity {
    private static final DataParameter<Byte> LOYALTY = EntityDataManager.defineId(StandArrowEntity.class, DataSerializers.BYTE);
    
    private ItemStack arrowItem = new ItemStack(ModItems.STAND_ARROW.get());
    private boolean dealtDamage;
    
    public StandArrowEntity(World world, double x, double y, double z, ItemStack arrowItem) {
        super(ModEntityTypes.STAND_ARROW.get(), x, y, z, world);
        setArrowStack(arrowItem);
    }
    
    public StandArrowEntity(World world, LivingEntity thrower, ItemStack arrowItem) {
        super(ModEntityTypes.STAND_ARROW.get(), thrower, world);
        setArrowStack(arrowItem);
    }
    
    public StandArrowEntity(EntityType<? extends AbstractArrowEntity> type, World world) {
        super(type, world);
    }
    
    private void setArrowStack(ItemStack arrowItem) {
        this.arrowItem = arrowItem.copy();
        this.entityData.set(LOYALTY, (byte) EnchantmentHelper.getLoyalty(arrowItem));
    }

    @Override
    protected void defineSynchedData() {
       super.defineSynchedData();
       entityData.define(LOYALTY, (byte)0);
    }
    
    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        if (!level.isClientSide() && target.isAlive()) {
            StandArrowItem.onPiercedByArrow(target, arrowItem, level, Optional.ofNullable(getOwner()));
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return arrowItem.copy();
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult entityRayTraceResult) {
        Entity target = entityRayTraceResult.getEntity();
        
        int damage = MathHelper.ceil(MathHelper.clamp(getDeltaMovement().length() * getBaseDamage(), 0.0D, 2.147483647E9D));
        if (isCritArrow()) {
            damage = (int) Math.min((long) random.nextInt(damage / 2 + 2) + (long) damage, 2147483647L);
        }

        Entity shooter = getOwner();
        DamageSource damageSource;
        if (shooter == null) {
            damageSource = DamageSource.arrow(this, this);
        }
        else {
            damageSource = DamageSource.arrow(this, shooter);
            if (shooter instanceof LivingEntity) {
                ((LivingEntity) shooter).setLastHurtMob(target);
            }
        }
        dealtDamage = true;
        
        boolean dodge = target.getType() == EntityType.ENDERMAN;
        int prevTargetFireTimer = target.getRemainingFireTicks();
        if (isOnFire() && !dodge) {
            target.setSecondsOnFire(5);
        }

        if (target.hurt(damageSource, (float) damage)) {
            if (dodge) {
                return;
            }

            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;

                if (!level.isClientSide && shooter instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingTarget, shooter);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) shooter, livingTarget);
                }

                doPostHurtEffects(livingTarget);
                if (shooter != null && livingTarget != shooter && livingTarget instanceof PlayerEntity
                        && shooter instanceof ServerPlayerEntity && !this.isSilent()) {
                    ((ServerPlayerEntity) shooter).connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!level.isClientSide && shooter instanceof ServerPlayerEntity) {
                    if (!target.isAlive() && shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger((ServerPlayerEntity) shooter, Arrays.asList(target));
                    }
                }
            }

            playSound(getHitGroundSoundEvent(), 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
            setDeltaMovement(getDeltaMovement().scale(-0.05D));
        } 
        else {
            target.setRemainingFireTicks(prevTargetFireTimer);
            setDeltaMovement(getDeltaMovement().scale(-0.05D));
            yRot += 180.0F;
            yRotO += 180.0F;
        }
    }

    @Override
    protected EntityRayTraceResult findHitEntity(Vector3d pos, Vector3d nextPos) {
        return dealtDamage ? null : super.findHitEntity(pos, nextPos);
    }
    
    @Override
    public double getBaseDamage() {
        return super.getBaseDamage() + EnchantmentHelper.getDamageBonus(arrowItem, CreatureAttribute.UNDEFINED);
    }
    
    @Override
    public void tick() {
        if (inGroundTime > 4) {
            dealtDamage = true;
        }
        Entity owner = getOwner();
        if ((dealtDamage || isNoPhysics()) && owner != null) {
            int loyalty = entityData.get(LOYALTY);
            if (loyalty > 0) {
                if (!owner.isAlive() || owner.isSpectator()) {
                    if (!level.isClientSide && pickup == AbstractArrowEntity.PickupStatus.ALLOWED) {
                        spawnAtLocation(getPickupItem(), 0.1F);
                    }
                    remove();
                }
                else {
                    setNoPhysics(true);
                    Vector3d posDiffToOwner = new Vector3d(owner.getX() - getX(), owner.getEyeY() - getY(), owner.getZ() - getZ());
                    setPosRaw(getX(), getY() + posDiffToOwner.y * 0.015D * (double) loyalty, getZ());
                    if (level.isClientSide) {
                        yOld = getY();
                    }
                    setDeltaMovement(getDeltaMovement().scale(0.95D).add(posDiffToOwner.normalize().scale(0.05D * (double) loyalty)));
                }
            }
        }
        super.tick();
    }

    @Override
    public void playerTouch(PlayerEntity player) {
        Entity owner = this.getOwner();
        if (entityData.get(LOYALTY) == 0 || owner == null || owner.getUUID() == player.getUUID()) {
            super.playerTouch(player);
        }
    }

    @Override
    public void tickDespawn() {
        if (pickup != AbstractArrowEntity.PickupStatus.ALLOWED || entityData.get(LOYALTY) <= 0) {
            super.tickDespawn();
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Arrow", 10)) {
            arrowItem = ItemStack.of(compound.getCompound("Arrow"));
        }
        entityData.set(LOYALTY, (byte) EnchantmentHelper.getLoyalty(arrowItem));
        dealtDamage = compound.getBoolean("DealtDamage");
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.put("Arrow", arrowItem.save(new CompoundNBT()));
        compound.putBoolean("DealtDamage", dealtDamage);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
//    public static class EntityPierce {
//        private static final List<PierceBehavior> ARROW_PIERCE_BEHAVIOR = new ArrayList<>();
//        private static final Random RANDOM = new Random();
//        
//        public static void addBehavior(@Nonnull Supplier<Predicate<LivingEntity>> entityCheck, @Nonnull Supplier<Consumer<LivingEntity>> entityConverter) {
//            ARROW_PIERCE_BEHAVIOR.add(new PierceBehavior(entityCheck, entityConverter));
//        }
//        
//        public static boolean onArrowPierce(LivingEntity entity) {
//            List<Consumer<LivingEntity>> converters = 
//                    ARROW_PIERCE_BEHAVIOR.stream()
//                    .filter(behavior -> behavior.entityCheck.get().test(entity))
//                    .map(behavior -> behavior.entityConverter.get())
//                    .collect(Collectors.toCollection(ArrayList::new));
//            if (!converters.isEmpty()) {
//                converters.get(RANDOM.nextInt(converters.size())).accept(entity);
//                return true;
//            }
//            else {
//                return false;
//            }
//        }
//        
//        private static class PierceBehavior {
//            private final Supplier<Predicate<LivingEntity>> entityCheck;
//            private final Supplier<Consumer<LivingEntity>> entityConverter;
//            
//            private PierceBehavior(@Nonnull Supplier<Predicate<LivingEntity>> entityCheck, @Nonnull Supplier<Consumer<LivingEntity>> entityConverter) {
//                this.entityCheck = entityCheck;
//                this.entityConverter = entityConverter;
//            }
//        }
//    }
}
