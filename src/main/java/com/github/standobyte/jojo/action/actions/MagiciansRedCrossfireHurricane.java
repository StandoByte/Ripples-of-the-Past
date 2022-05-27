package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.damaging.projectile.MRCrossfireHurricaneEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.MathUtil;

import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;

public class MagiciansRedCrossfireHurricane extends StandEntityAction {

    public MagiciansRedCrossfireHurricane(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            boolean special = isShiftVariation();
            int n = special ? 8 : 1;
            if (special && target.getType() == TargetType.EMPTY) {
                target = ActionTarget.fromRayTraceResult(standEntity.aimWithStandOrUser(32, target));
            }
            for (int i = 0; i < n; i++) {
                MRCrossfireHurricaneEntity ankh = new MRCrossfireHurricaneEntity(special, standEntity, world, userPower);
                Vector2f rotOffsets = i == 0 ? Vector2f.ZERO
                        : MathUtil.xRotYRotOffsets(((double) i / (double) n + 0.5) * Math.PI, 1.5);
                if (special && target.getType() != TargetType.EMPTY) {
                    ankh.setSpecial(target.getTargetPos(true));
                }
                ankh.shootFromRotation(standEntity, standEntity.xRot + rotOffsets.x, standEntity.yRot + rotOffsets.y, 
                        0, special ? 1.0F : 0.75F, 0.0F);
                ankh.setScale((float) standEntity.getStandEfficiency());
                world.addFreshEntity(ankh);
            }
            standEntity.playSound(ModSounds.MAGICIANS_RED_CROSSFIRE_HURRICANE.get(), 1.0F, 1.0F);
        }
    }
    
    @Override
    public void onMaxTraining(IStandPower power) {
        power.unlockAction(getShiftVariationIfPresent());
    }
}
