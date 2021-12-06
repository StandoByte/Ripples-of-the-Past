package com.github.standobyte.jojo.entity.itemprojectile;

import java.util.Arrays;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.StandArrowItem;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class StandArrowEntity extends AbstractArrowEntity {
    private ItemStack arrowItem = new ItemStack(ModItems.STAND_ARROW.get());
    
    public StandArrowEntity(World world, LivingEntity thrower, ItemStack arrowItem) {
        super(ModEntityTypes.STAND_ARROW.get(), thrower, world);
        this.arrowItem = arrowItem.copy();
    }
    
    public StandArrowEntity(EntityType<? extends AbstractArrowEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        StandArrowItem.onPiercedByArrow(target, arrowItem, level);
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
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Arrow", 10)) {
            arrowItem = ItemStack.of(compound.getCompound("Trident"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT arrow) {
        super.addAdditionalSaveData(arrow);
        arrow.put("Arrow", arrowItem.save(new CompoundNBT()));
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
