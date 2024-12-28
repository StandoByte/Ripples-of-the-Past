package com.github.standobyte.jojo.action.non_stand;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.WindupAttackAnim;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanBladeDashAttack extends PillarmanAction implements IPlayerAction<PillarmanBladeDashAttack.Instance, INonStandPower> {

    public PillarmanBladeDashAttack(PillarmanAction.Builder builder) {
        super(builder);
        mode = Mode.LIGHT;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        return ActionConditionResult.noMessage(user.isOnGround());
    }

    @Override
    public boolean holdOnly(INonStandPower power) {
        return false;
    }
    
    @Override
    public int getHoldDurationMax(INonStandPower power) {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled && !world.isClientSide()) {
        	power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().setBladesVisible(true);
            /*ClientTickingSoundsHelper.playStoppableEntitySound(user, ModSounds.HAMON_SYO_CHARGE.get(), 
                    1.0F, 1.0F, false, entity -> power.getHeldAction() == this);*/ //there should be it's own sfx
        }
    }
    
    @Override
    public boolean clHeldStartAnim(PlayerEntity user) {
        return getPlayerAnim().setWindupAnim(user);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!user.level.isClientSide()) {
            setPlayerAction(user, power);
        }
        Vector3d leap = Vector3d.directionFromRotation(MathHelper.clamp(user.xRot, -45F, -18F), user.yRot)
                .scale(1 + user.getAttributeValue(Attributes.MOVEMENT_SPEED) * 20);
        user.setDeltaMovement(leap.x, leap.y * 0.05F, leap.z);
    }
    
    @Override
    public Instance createContinuousActionInstance(LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        if (user.level.isClientSide() && user instanceof PlayerEntity) {
            getPlayerAnim().setAttackAnim((PlayerEntity) user);
        }
        return new Instance(user, userCap, power, this, getEnergyCost(power, ActionTarget.EMPTY));
    }
    
    @Override
    public void stoppedHolding(World world, LivingEntity user, INonStandPower power, int ticksHeld, boolean willFire) {
    	if (!willFire) {
            if (!world.isClientSide()) {
            	power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().setBladesVisible(false);
            }
            else if (user instanceof PlayerEntity) {
                getPlayerAnim().stopAnim((PlayerEntity) user);
            }
    	}
    }
    
    protected WindupAttackAnim getPlayerAnim() {
        return ModPlayerAnimations.bladeDash;
    }
     
    public static class Instance extends ContinuousActionInstance<PillarmanBladeDashAttack, INonStandPower> {
        private Set<UUID> damagedEntities = new HashSet<>();
        PillarmanData pillarman = playerPower.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get();

        public Instance(LivingEntity user, PlayerUtilCap userCap, 
                INonStandPower playerPower, PillarmanBladeDashAttack action, float spentEnergy) {
            super(user, userCap, playerPower, action);
        }
        
        @Override
        public boolean cancelIncomingDamage(DamageSource dmgSource, float dmgAmount) {
            return true;
        }
        
        @Override
        public void playerTick() {
        	 List<LivingEntity> targets = user.level.getEntitiesOfClass(LivingEntity.class, slashHitbox(user), 
                     entity -> !entity.is(user) && user.canAttack(entity));
             for (LivingEntity target : targets) {
                 if (damagedEntities.add(target.getUUID())) {
                     boolean kickDamage = dealPhysicalDamage(user.level, user, target);
                     if (kickDamage) {
                         Vector3d vecToTarget = target.position().subtract(user.position());
                         boolean left = MathHelper.wrapDegrees(
                                 user.yBodyRot - MathUtil.yRotDegFromVec(vecToTarget))
                                 < 0;
                         float knockbackYRot = (60F + user.getRandom().nextFloat() * 30F) * (left ? 1 : -1);
                         knockbackYRot += (float) -MathHelper.atan2(vecToTarget.x, vecToTarget.z) * MathUtil.RAD_TO_DEG;
                         DamageUtil.knockback((LivingEntity) target, 0.75F, knockbackYRot);
                         PillarmanUtil.sparkEffect(target, 60);
                     }
                 }
             }
            switch (getTick()) {
            case 1:
                if (user.level.isClientSide()) {
                    user.level.playSound(ClientUtil.getClientPlayer(), user.getX(), user.getEyeY(), user.getZ(), 
                            ModSounds.HAMON_SYO_SWING.get(), user.getSoundSource(), 1.0f, 1.0f);
                    user.swing(Hand.MAIN_HAND, true);
                    pillarman.setBladesVisible(true);
                }
                break;
            case 15:
            	pillarman.setBladesVisible(false);
                stopAction();
                break;
            }
        }

        @Override
        public float getWalkSpeed() {
            return getAction().getHeldWalkSpeed();
        }
        
        @Override
        public void onStop() {
            super.onStop();
            if (user.level.isClientSide() && user instanceof PlayerEntity) {
                getAction().getPlayerAnim().stopAnim((PlayerEntity) user);
            }
        }
    }
       
    private static boolean dealPhysicalDamage(World world, LivingEntity user, Entity target) {
        return target.hurt(new EntityDamageSource(user instanceof PlayerEntity ? "player" : "mob", user), 
        		DamageUtil.addArmorPiercing(VampirismClawLacerate.getDamage(world, user) + 1F, 15F, (LivingEntity) target));
    }
    
    public static AxisAlignedBB slashHitbox(LivingEntity user) {
        float xzAngle = -user.yRot * MathUtil.DEG_TO_RAD;
        Vector3d lookVec = new Vector3d(Math.sin(xzAngle), 0, Math.cos(xzAngle));
        Vector3d hitboxXZCenter = user.position().add(lookVec.scale(user.getBbWidth() * 0.75F));
        return new AxisAlignedBB(hitboxXZCenter, hitboxXZCenter)
                .inflate(user.getBbWidth() * 1.5F, 0.125, user.getBbWidth() * 1.5F)
                .expandTowards(0, user.getBbHeight() / 2, 0);
    }
}

