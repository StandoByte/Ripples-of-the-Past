package com.github.standobyte.jojo.power.impl.nonstand.type.pillarman;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.non_stand.PillarmanAction;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent;

public class PillarmanPowerType extends NonStandPowerType<PillarmanData> {
    public static final int COLOR = 0xFFAA00;

    public PillarmanPowerType(PillarmanAction[] startingAttacks, PillarmanAction[] startingAbilities, PillarmanAction defaultMmb) {
        super(startingAttacks, startingAbilities, defaultMmb, PillarmanData::new);
    }

    public PillarmanPowerType(PillarmanAction[] startingAttacks, PillarmanAction[] startingAbilities) {
        super(startingAttacks, startingAbilities, startingAttacks[0], PillarmanData::new);
    }
    
    @Override
    public boolean keepOnDeath(INonStandPower power) {
        return true;
    }
    
    @Override
    public void onClear(INonStandPower power) {
    	power.getTypeSpecificData(this).get().setEvolutionStage(0);
    	power.getTypeSpecificData(this).get().setMode(Mode.NONE);
    	power.getTypeSpecificData(this).get().setPillarmanBuffs(power.getUser(), 0);
    	LivingEntity user = power.getUser();
        for (Effect effect : EFFECTS) {
            EffectInstance effectInstance = user.getEffect(effect);
            if (effectInstance != null) {
                user.removeEffect(effectInstance.getEffect());
            }
        }
    }
    
    @Override
    public float getMaxEnergy(INonStandPower power) {
        World world = power.getUser().level;
        return super.getMaxEnergy(power) * GeneralUtil.getOrLast(
                JojoModConfig.getCommonConfigInstance(world.isClientSide()).maxBloodMultiplier.get(), world.getDifficulty().getId())
                .floatValue() * power.getTypeSpecificData(this).get().getEvolutionStage();
    }
    
    @Override
    public float tickEnergy(INonStandPower power) {
        World world = power.getUser().level;
        float inc = -GeneralUtil.getOrLast(
        		JojoModConfig.getCommonConfigInstance(world.isClientSide()).bloodTickDown.get(), world.getDifficulty().getId())
                .floatValue();
        if (power.isUserCreative()) {
            inc = Math.max(inc, 0);
        }
        if(power.getTypeSpecificData(this).get().getEvolutionStage() == 1) {
        	return power.getEnergy() + 0.5F * world.getDifficulty().getId()/2;
        }
        return power.getEnergy() + inc * power.getTypeSpecificData(this).get().getEvolutionStage();
    }
    
    @Override
    public float getMaxStaminaFactor(INonStandPower power, IStandPower standPower) {
    	
        return 1 * power.getTypeSpecificData(this).get().getEvolutionStage();
    }
    
    @Override
    public float getStaminaRegenFactor(INonStandPower power, IStandPower standPower) {
        return 1 * power.getTypeSpecificData(this).get().getEvolutionStage();
    }
    
    @Override
    public void tickUser(LivingEntity entity, INonStandPower power) {
    	PillarmanData pillarman = power.getTypeSpecificData(this).get();
    	pillarman.tick();
    	if (!entity.level.isClientSide()) {
	    	if(power.getTypeSpecificData(this).get().isInvaded() == true) {
	    		ServerPlayerEntity player = (ServerPlayerEntity) entity;
	    		if(player.isShiftKeyDown() || (player.getCamera() == player)) {
	    			player.setGameMode(GameType.SURVIVAL);
	        		power.getTypeSpecificData(this).get().setInvaded(false);
	    		}
	    	}
	    	if(pillarman.getEvolutionStage() > 1) {
	    		if (entity instanceof PlayerEntity) {
	    			((PlayerEntity) entity).getFoodData().setFoodLevel(17);
	    			}
		        entity.setAirSupply(entity.getMaxAirSupply());
		        float energy =  power.getEnergy();
		        if(pillarman.refreshEnergy((int) power.getEnergy())) {
		        	for (Effect effect : EFFECTS) {
		        		if (energy > power.getMaxEnergy()/10) {
		                entity.addEffect(new EffectInstance(effect, Integer.MAX_VALUE, 0, false, false, true));
		                } else {
		                	entity.removeEffect(effect);
		                	}
		        	}
		        }
	    	}
    	}
    }
    
    private static final Set<Effect> EFFECTS = new HashSet<>();
    public static void initPillarmanEffects() {
        Collections.addAll(EFFECTS, 
                ModStatusEffects.UNDEAD_REGENERATION.get(),
                Effects.DAMAGE_RESISTANCE,
                Effects.NIGHT_VISION);
    }
    
    @Override
    public boolean isReplaceableWith(NonStandPowerType<?> newType) {
        return false;
    }
    
    @Override
    public float getTargetResolveMultiplier(INonStandPower power, IStandPower attackingStand) {
        LivingEntity entity = power.getUser();
        if (entity != null) {
            return (float) Math.pow(2, Math.max(entity.level.getDifficulty().getId() - 1, 0));
        }
        return 1;
    }
    
    @Override
    public boolean isLeapUnlocked(INonStandPower power) {
        return true;
    }
    
    @Override
    public float getLeapStrength(INonStandPower power) {
        float leapStrength = 3F; //Math.max(bloodLevel(power), 0);
        return leapStrength * 0.35F;
    }
    
    @Override
    public int getLeapCooldownPeriod() {
        return 20;
    }
    
    @Override
    public float getLeapEnergyCost() {
    	/*if(INonStandPower.getNonStandPowerOptional().map(power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get())
                .map(pillarman -> pillarman.getEvolutionStage() < 2).orElse(false)).orElse(false)) {
    		return 50;
    	}*/
        return 0;
    }
    
    public static void cancelPillarmanEffectRemoval(PotionRemoveEvent event) {
        EffectInstance effectInstance = event.getPotionEffect();
        if (effectInstance != null) {
            LivingEntity entity = event.getEntityLiving();
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).ifPresent(pillarman -> {
                    Effect effect = event.getPotion();
                    if (EFFECTS.contains(effect) 
                    		&& power.getEnergy() > power.getMaxEnergy()/10 
                    		&& power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().getEvolutionStage() > 1) {
                        event.setCanceled(true);
                    }
                });
            });
        }
    }
    
    public boolean isHighLifeForce(LivingEntity entity) {
    	if(INonStandPower.getNonStandPowerOptional(entity).map(
                        power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get())
                        .map(pillarman -> pillarman.getEvolutionStage() > 1).orElse(false)).orElse(false)) {
    		return true;
    	}
        return false;
    }
}
