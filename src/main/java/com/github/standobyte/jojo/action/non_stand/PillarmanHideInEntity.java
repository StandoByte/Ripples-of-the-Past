package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

public class PillarmanHideInEntity extends PillarmanAction {

    public PillarmanHideInEntity(PillarmanAction.Builder builder) {
        super(builder);
        stage = 2;
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {  
        if (!world.isClientSide() && user instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) user;
            Entity ride = (Entity) target.getEntity();
            player.startRiding(ride);
            player.setCamera(ride);
            player.setGameMode(GameType.SPECTATOR);
            power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().setInvaded(true);
        }
    }
}
