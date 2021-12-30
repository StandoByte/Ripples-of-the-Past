package com.github.standobyte.jojo.entity.damaging.projectile;

import java.util.List;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.util.damage.ModDamageSources;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonCutterEntity extends ModdedProjectileEntity {
    private static final Vector3d MOUTH_POS_OFFSET = new Vector3d(0.0D, -0.1D, 0.0D);
    private ItemStack potionItem;
    private int color = -1;

    public HamonCutterEntity(LivingEntity shooter, World world, ItemStack potionItem) {
        super(ModEntityTypes.HAMON_CUTTER.get(), shooter, world);
        this.potionItem = potionItem == null ? ItemStack.EMPTY : potionItem;
        this.color = PotionUtils.getColor(potionItem);
    }

    public HamonCutterEntity(EntityType<? extends HamonCutterEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide() && tickCount % 7 == getId() % 7) {
            HamonPowerType.createHamonSparkParticles(level, ClientUtil.getClientPlayer(), getX(), getY(0.5), getZ(), 0.1F);
        }
    }
    
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return MOUTH_POS_OFFSET;
    }

    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        boolean projectileAttack = super.hurtTarget(target, owner);
        boolean hamonAttack = ModDamageSources.dealHamonDamage(target, 0.075F, this, owner);
        return projectileAttack || hamonAttack;
    }

    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            List<EffectInstance> effects = PotionUtils.getMobEffects(potionItem);
            Entity entity = entityRayTraceResult.getEntity();
            if (entity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) entity;
                if (target.isAffectedByPotions()) {
                    for (EffectInstance effectInstance : effects) {
                        Effect effect = effectInstance.getEffect();
                        if (effect.isInstantenous()) {
                            effect.applyInstantenousEffect(this, getOwner(), target, effectInstance.getAmplifier(), getEffectDurationFactor());
                        } else {
                            target.addEffect(new EffectInstance(effect, MathHelper.floor(effectInstance.getDuration() * getEffectDurationFactor()), 
                                    effectInstance.getAmplifier(), effectInstance.isAmbient(), effectInstance.isVisible()));
                        }
                    }
                }
            }

            LivingEntity owner = getOwner();
            if (owner != null) {
                INonStandPower.getNonStandPowerOptional(owner).ifPresent(power -> {
                    power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, ModActions.ZEPPELI_HAMON_CUTTER.get().getEnergyCost(null) / 6F);
                    });
                });
            }
        }
    }

    private float getEffectDurationFactor() {
        return 0.75F;
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    public float getBaseDamage() {
        return 2.0F;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }
    
    @Override
    protected int ticksLifespan() {
        return 100;
    }
    
    public int getColor() {
        return color;
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        if (!potionItem.isEmpty()) {
            nbt.put("Potion", potionItem.save(new CompoundNBT()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        potionItem = ItemStack.of(nbt.getCompound("Potion"));
        color = PotionUtils.getColor(potionItem);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(color);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        color = additionalData.readInt();
    }
}
