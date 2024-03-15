package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;


import net.minecraft.entity.LivingEntity;

import net.minecraft.world.World;

public class HamonRebuffOverdrive extends HamonAction {
    
    public HamonRebuffOverdrive(HamonAction.Builder builder) {
        super(builder.needsFreeMainHand());
    }
    

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {    
        power.getTypeSpecificData(ModPowers.HAMON.get()).get().toggleRebuffOverdrive();
    }

   /*private final LazySupplier<ResourceLocation> rebuffTex = 
            new LazySupplier<>(() -> makeIconVariant(this, "_on"));
    
    @Override
    public ResourceLocation getIconTexturePath(@Nullable INonStandPower power) {
        if (power != null && power.getTypeSpecificData(ModPowers.HAMON.get()).get().getRebuffOverdrive()) {
            return rebuffTex.get();
        }
        else {
            return super.getIconTexturePath(power);
        }
    }*/
    
}
