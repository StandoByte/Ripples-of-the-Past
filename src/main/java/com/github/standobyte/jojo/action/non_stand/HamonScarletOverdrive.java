package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.WindupAttackAnim;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class HamonScarletOverdrive extends HamonSunlightYellowOverdrive {

    public HamonScarletOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected WindupAttackAnim getPlayerAnim() {
        return ModPlayerAnimations.scarletOverdrive;
    }
    
    @Override
    public Instance createContinuousActionInstance(
            LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        if (user.level.isClientSide() && user instanceof PlayerEntity) {
            getPlayerAnim().setAttackAnim((PlayerEntity) user);
        }
        return new HamonScarletOverdrive.Instance(user, userCap, power, this, getSpentEnergy(power));
    }
    
    
    
    public static class Instance extends HamonSunlightYellowOverdrive.Instance {

        public Instance(LivingEntity user, PlayerUtilCap userCap, INonStandPower playerPower,
                HamonSunlightYellowOverdrive action, float spentEnergy) {
            super(user, userCap, playerPower, action, spentEnergy);
        }
        
        @Override
        public void playerTick() {
            switch (getTick()) {
            case 1:
                if (user.level.isClientSide()) {
                    user.level.playSound(ClientUtil.getClientPlayer(), user.getX(), user.getEyeY(), user.getZ(), 
                            ModSounds.HAMON_SYO_SWING.get(), user.getSoundSource(), 1.0f, 1.0f);
                    user.swing(Hand.OFF_HAND, true);
                }
                break;
            case 4:
                if (!user.level.isClientSide()) {
                    ActionTarget target = playerPower.getMouseTarget();
                    if (target.getEntity() instanceof LivingEntity) {
                        performPunch((LivingEntity) target.getEntity());
                    }
                }
                break;
            case 9:
                stopAction();
                break;
            }
        }
        
        @Override
        protected void doHamonAttack(LivingEntity target) {
            float efficiency = userHamon.getActionEfficiency(0, true, getAction().getUnlockingSkill());
            float damage = 2.5F + 5F * energySpentRatio;
            damage *= efficiency;
            int fireSeconds = MathHelper.floor(2 + 8F * (float) userHamon.getHamonStrengthLevel() / (float) HamonData.MAX_STAT_LEVEL * efficiency);
            float hamonDamage = damage;
            if (DamageUtil.dealDamageAndSetOnFire(target, 
                    entity -> DamageUtil.dealHamonDamage(entity, hamonDamage, user, null, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_RED.get())), 
                    fireSeconds, false)) {
                target.level.playSound(null, target.getX(), target.getEyeY(), target.getZ(), ModSounds.HAMON_SYO_PUNCH.get(), target.getSoundSource(), energySpentRatio, 1.0F);
                userHamon.hamonPointsFromAction(HamonStat.STRENGTH, getActualMaxEnergy(playerPower) * energySpentRatio * efficiency);
                DamageUtil.knockback3d(target, 2f, -5, user.yRot);
                boolean hamonSpread = userHamon.isSkillLearned(ModHamonSkills.HAMON_SPREAD.get());
                float punchDamage = damage;
                KnockbackCollisionImpact.getHandler(target).ifPresent(cap -> {
                    cap.onPunchSetKnockbackImpact(target.getDeltaMovement(), user);
                    if (hamonSpread) {
                        cap.hamonDamage(punchDamage, Math.max(20 * fireSeconds / 2, 20), ModParticles.HAMON_SPARK_RED.get());
                    }
                });
            }
        }
    }
    
}
