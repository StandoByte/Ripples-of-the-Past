package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class PillarmanRegeneration extends PillarmanAction {

    public PillarmanRegeneration(PillarmanAction.Builder builder) {
        super(builder);
        stage = 2;
        canBeUsedInStone = true;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        
        if (!world.isClientSide()) {
            HamonHealing.updateRegenEffect(user, 80, 3, user.getEffect(Effects.REGENERATION), Effects.REGENERATION);
            world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), ModSounds.VAMPIRE_BLOOD_DRAIN.get(), user.getSoundSource(), 1.5F, 1.2F);
        }
    }
}
