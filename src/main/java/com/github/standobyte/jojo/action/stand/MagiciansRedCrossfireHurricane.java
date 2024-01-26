package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.damaging.projectile.MRCrossfireHurricaneEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;

public class MagiciansRedCrossfireHurricane extends StandEntityAction {

    public MagiciansRedCrossfireHurricane(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void phaseTransition(World world, StandEntity standEntity, IStandPower standPower, 
            Phase from, Phase to, StandEntityTask task, int ticks) {
        super.phaseTransition(world, standEntity, standPower, from, to, task, ticks);
        if (!world.isClientSide() && to == Phase.BUTTON_HOLD) {
            task.getAdditionalData().push(Integer.class, 0);
        }
    }
    
    @Override
    public void standTickButtonHold(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            // FIXME consume fire blocks around the stand
            if (false) {
                task.getAdditionalData().push(Integer.class, task.getAdditionalData().pop(Integer.class) + 1);
            }
            if (task.getTicksLeft() == 1) {
                task.getAdditionalData().push(Integer.class, task.getStartingTicks());
            }
        }
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            int chargeTicks = task.getAdditionalData().isEmpty(Integer.class) ? 0 : 
                task.getAdditionalData().pop(Integer.class);
            int fireBlocksConsumed = task.getAdditionalData().isEmpty(Integer.class) ? 0 : 
                task.getAdditionalData().pop(Integer.class);
            float fireConsumed = fireBlocksConsumed > 0 && chargeTicks > 0 ? (float) fireBlocksConsumed / (float) chargeTicks : 0;
            
            boolean special = isShiftVariation();
            int n = special ? MathHelper.floor(8 * (fireConsumed + 1)) : 1;
            ActionTarget target = task.getTarget();
            if (special && target.getType() == TargetType.EMPTY) {
                target = standEntity.aimWithThisOrUser(64, target);
            }
            for (int i = 0; i < n; i++) {
                MRCrossfireHurricaneEntity ankh = new MRCrossfireHurricaneEntity(special, standEntity, world, userPower);
                Vector2f rotOffsets = i == 0 ? Vector2f.ZERO
                        : MathUtil.xRotYRotOffsets(((double) i / (double) n + 0.5) * Math.PI, 1.5);
                if (special && target.getType() != TargetType.EMPTY) {
                    ankh.setSpecial(target.getTargetPos(true));
                }
                ankh.shootFromRotation(standEntity, standEntity.xRot + rotOffsets.x, standEntity.yRot + rotOffsets.y, 
                        0, special ? 2.0F : 1.25F, 0.0F);
                ankh.setScale((float) standEntity.getStandEfficiency());
                if (!special) {
                    ankh.setScale(ankh.getScale() * (fireConsumed + 1));
                }
                world.addFreshEntity(ankh);
            }
            standEntity.playSound(ModSounds.MAGICIANS_RED_CROSSFIRE_HURRICANE.get(), 1.0F, 1.0F);
        }
    }
    
    @Override
    public void onMaxTraining(IStandPower power) {
        power.unlockAction((StandAction) getShiftVariationIfPresent());
    }
}
