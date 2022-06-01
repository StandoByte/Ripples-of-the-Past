package com.github.standobyte.jojo.action.actions;

import java.util.Random;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.MRFlameEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class MagiciansRedFlameBurst extends StandEntityAction {

    public MagiciansRedFlameBurst(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        Random random = standEntity.getRandom();
        if (!world.isClientSide()) {
        	JojoModUtil.doFractionTimes(() -> {
                MRFlameEntity flame = new MRFlameEntity(standEntity, world);
                float velocity = (float) standEntity.getAttributeValue(ForgeMod.REACH_DISTANCE.get()) / 4F;
                if (userPower.getResolveLevel() >= 3) {
                    velocity *= 2F;
                }
                flame.shootFromRotation(standEntity, standEntity.xRot + (random.nextFloat() - 0.5F) * 10F, 
                        standEntity.yRot + (random.nextFloat() - 0.5F) * 10F, 
                        0, velocity, 0.0F);
                standEntity.addProjectile(flame);
        	}, standEntity.getAttackSpeed() / userPower.getType().getDefaultStats().getBaseAttackSpeed());
        }
        else {
            standEntity.playSound(ModSounds.MAGICIANS_RED_FIRE_BLAST.get(), 0.5F, 0.3F + random.nextFloat() * 0.4F, ClientUtil.getClientPlayer());
        }
    }
}
