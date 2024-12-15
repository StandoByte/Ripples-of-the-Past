package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;

public class PillarmanGiantCarthwheelPrison extends PillarmanAction {

    public PillarmanGiantCarthwheelPrison(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.HEAT;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean reqFulfilled, boolean reqStateChanged) {
        if (reqFulfilled) {
            PillarmanDivineSandstorm.auraEffect(user, ModParticles.HAMON_AURA_RED.get(), 12);
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            int n = 8;
            for (int i = 0; i < n; i++) {
                Vector2f rotOffsets2 = MathUtil.xRotYRotOffsets((double) i / (double) n * Math.PI * 2, 2);
                PillarmanErraticBlazeKing.addVeinProjectile(world, power, user, rotOffsets2.x, rotOffsets2.y, 0, -0.5D, 0);
                PillarmanErraticBlazeKing.addVeinProjectile(world, power, user, rotOffsets2.x, rotOffsets2.y + 180, 0, -0.5D, 0);
                PillarmanErraticBlazeKing.addVeinProjectile(world, power, user, rotOffsets2.x, rotOffsets2.y + 90, 0, -0.5D, 0);
                PillarmanErraticBlazeKing.addVeinProjectile(world, power, user, rotOffsets2.x, rotOffsets2.y - 90, 0, -0.5D, 0);
            }
            user.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 600, 0, false, false));
        }
    }
    
    @Override
    public boolean clHeldStartAnim(PlayerEntity user) {
        return ModPlayerAnimations.giantCartwheelPrison.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(PlayerEntity user) {
        ModPlayerAnimations.giantCartwheelPrison.setAnimEnabled(user, false);
    }
    
}
