package com.github.standobyte.jojo.action.stand;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.effect.GECreatedLifeformEffect;
import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class GoldExperienceRevertLifeform extends StandAction {
    public static final double MARKER_DISTANCE = 64;

    public GoldExperienceRevertLifeform(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public Action<IStandPower> getVisibleAction(IStandPower power, ActionTarget target) {
        Action<IStandPower> action = super.getVisibleAction(power, target);
        if (action == this && 
                !StandEffectsTracker.getEffectsOfType(power, ModStandEffects.GE_CREATED_LIFEFORM.get(), MARKER_DISTANCE)
                .findAny().isPresent()) {
            action = null;
        }
        return action;
    }
    
    @Override
    public void clWriteExtraData(PacketBuffer buf) {
        clWriteTargetedStandEffect(buf, ModStandEffects.GE_CREATED_LIFEFORM.get(), MARKER_DISTANCE);
    }
    
    @Override
    public void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (!world.isClientSide() && extraInput != null) {
            readTargetedStandEffect(extraInput, power, ModStandEffects.GE_CREATED_LIFEFORM.get())
            .ifPresent(effect -> {
                effect.remove();
                Entity entity = effect.getTarget();
                if (entity != null && entity.getType() == ModEntityTypes.GE_LIFEFORM_TRANSFORMATION.get()) {
                    power.setCooldownTimer(ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get(), 
                            power.getCooldownTimer(ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get()) - ((GETransformationEntity) entity).actionCooldown);
                }
            });
        }
    }
    
    
    @Override
    public IFormattableTextComponent getTranslatedName(IStandPower power, String key) {
        Optional<StandEffectInstance> targetedEffect = clGetTargetedStandEffect(ModStandEffects.GE_CREATED_LIFEFORM.get(), MARKER_DISTANCE);
        return targetedEffect.map(e -> {
            GECreatedLifeformEffect effect = (GECreatedLifeformEffect) e;
            return (IFormattableTextComponent) new TranslationTextComponent(key + ".param", effect.getName());
        }).orElse(super.getTranslatedName(power, key));
    }
}
