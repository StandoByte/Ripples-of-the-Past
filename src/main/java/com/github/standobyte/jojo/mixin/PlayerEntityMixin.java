package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.capability.entity.power.NonStandCapProvider;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void jojoPlayerUndeadCreature(CallbackInfoReturnable<CreatureAttribute> ci) {
        if (this.getCapability(NonStandCapProvider.NON_STAND_CAP).map(power -> 
        power.getType() == ModPowers.VAMPIRISM.get()).orElse(false)) {
            ci.setReturnValue(CreatureAttribute.UNDEAD);
        }
    }
}
