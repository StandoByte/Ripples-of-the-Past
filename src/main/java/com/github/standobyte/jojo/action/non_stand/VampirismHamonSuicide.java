package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class VampirismHamonSuicide extends VampirismAction {

    public VampirismHamonSuicide(NonStandAction.Builder builder) {
        super(builder); 
    }
    
    @Override
    public void playVoiceLine(LivingEntity user, INonStandPower power, ActionTarget target, boolean wasActive, boolean shift) {
        power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(vampirism -> {
            vampirism.getPrevHamonCharacter().ifPresent(character -> {
                SoundEvent shout = ModHamonActions.HAMON_BREATH.get().getCharacterShout(character);
                if (shout != null) {
                    JojoModUtil.sayVoiceLine(user, shout, null, 1.0F, 1.0F, 0, true);
                }
            });
        });
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (world.isClientSide()) {
            ClientTickingSoundsHelper.playHamonEnergyConcentrationSound(user, 1.0F, this);
        }
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide()) {
            if (ticksHeld % 10 == 5) {
                DamageUtil.dealHamonDamage(user, 4, user, null);
            }
            if (ticksHeld == 30) {
                user.addEffect(new EffectInstance(ModStatusEffects.HAMON_SPREAD.get(), 100, 1));
            }
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            DamageUtil.dealHamonDamage(user, 200, user, null);
            power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(vampirism -> {
                float hamonStrength = vampirism.getPrevHamonStrengthLevel();
                if (hamonStrength > 0) {
                    HamonUtil.hamonExplosion(world, user, null, 
                            user.getBoundingBox().getCenter(), 6, hamonStrength * 0.1F);
                }
            });
        }
    }
}
