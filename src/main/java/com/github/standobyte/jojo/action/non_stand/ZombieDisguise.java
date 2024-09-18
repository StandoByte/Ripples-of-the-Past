package com.github.standobyte.jojo.action.non_stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.zombie.ZombieData;
import com.github.standobyte.jojo.util.general.LazySupplier;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ZombieDisguise extends ZombieAction {

    public ZombieDisguise(ZombieAction.Builder builder) {
        super(builder);
    }

//    @Override
//    public ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
//        return ActionConditionResult.POSITIVE;
//    }
    
    private final LazySupplier<ResourceLocation> disguiseTex = new LazySupplier<>(() -> makeIconVariant(this, "_on"));
    @Override
    public ResourceLocation getIconTexturePath(@Nullable INonStandPower power) {
        if (power != null && isDisguised(power)) {
            return disguiseTex.get();
        }
        else {
            return super.getIconTexturePath(power);
        }
    }
    
    @Override
    public String getTranslationKey(INonStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        if (isDisguised(power)) {
            key += ".disable";
        }
        return key;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {  
        if (!world.isClientSide()) {
            power.getTypeSpecificData(ModPowers.ZOMBIE.get()).get().toggleDisguise();
        }
    }
    
    @Override
    public boolean greenSelection(INonStandPower power, ActionConditionResult conditionCheck) {
        return isDisguised(power);
    }
    
    private static boolean isDisguised(INonStandPower power) {
        return power.getTypeSpecificData(ModPowers.ZOMBIE.get()).map(ZombieData::isDisguiseEnabled).orElse(false);
    }
}
