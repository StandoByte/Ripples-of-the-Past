package com.github.standobyte.jojo.action.stand;

import java.util.Random;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.MRFlameEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class MagiciansRedFlameBurst extends StandEntityAction {
    public static final StandPose FLAME_BURST_POSE = new StandPose("MR_FLAME_BURST", true);

    public MagiciansRedFlameBurst(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        Random random = standEntity.getRandom();
        if (!world.isClientSide()) {
            GeneralUtil.doFractionTimes(() -> {
                MRFlameEntity flame = new MRFlameEntity(standEntity, world);
                float velocity = (float) standEntity.getAttributeValue(ForgeMod.REACH_DISTANCE.get()) / 5F;
                if (userPower.getResolveLevel() >= 3) {
                    velocity *= 2F;
                }
                flame.shootFromRotation(standEntity, standEntity.xRot + (random.nextFloat() - 0.5F) * 10F, 
                        standEntity.yRot + (random.nextFloat() - 0.5F) * 10F, 
                        0, velocity, 0.0F);
                standEntity.addProjectile(flame);
            }, StandStatFormulas.projectileFireRateScaling(standEntity, userPower));
        }
        else {
            standEntity.playSound(ModSounds.MAGICIANS_RED_FIRE_BLAST.get(), 0.5F, 0.3F + random.nextFloat() * 0.4F, ClientUtil.getClientPlayer());
        }
    }
}
