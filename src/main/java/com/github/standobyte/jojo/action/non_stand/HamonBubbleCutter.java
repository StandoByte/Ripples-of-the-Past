package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleCutterEntity;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonBubbleCutter extends HamonAction {

    public HamonBubbleCutter(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        return HamonBubbleLauncher.checkSoap(user);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HamonBubbleLauncher.consumeSoap(user, 20);
            
            boolean shift = isShiftVariation();
            int bubbles = shift ? 4 : 8;
            Vector3d shootingPos = null;
            for (int i = 0; i < bubbles; i++) {
                HamonBubbleCutterEntity bubbleCutterEntity = new HamonBubbleCutterEntity(user, world);
                float velocity = 1.35F + user.getRandom().nextFloat() * 0.3F;
                bubbleCutterEntity.setGliding(shift);
                bubbleCutterEntity.setHamonStatPoints(getEnergyCost(power, target) / 10F);
                bubbleCutterEntity.shootFromRotation(user, velocity, shift ? 2.0F : 8.0F);
                if (i == 0) shootingPos = bubbleCutterEntity.position();
                world.addFreshEntity(bubbleCutterEntity);
            }
            HamonUtil.emitHamonSparkParticles(world, null, shootingPos.x, shootingPos.y, shootingPos.z, 0.75F);
        }
    }
    
    @Override
    public boolean renderHamonAuraOnItem(ItemStack item, HandSide handSide) {
        return item.getItem() == ModItems.SOAP.get();
    }
}
