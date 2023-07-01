package com.github.standobyte.jojo.client.playeranim.playeranimator.anim.mob;

import com.github.standobyte.jojo.client.render.entity.model.mob.HamonMasterModel;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.SetableSupplier;
import dev.kosmx.playerAnim.impl.IBendHelper;
import dev.kosmx.playerAnim.impl.IMutableModel;
import net.minecraft.util.Direction;

public class HamonMasterAnimApplier extends EntityAnimApplier<HamonMasterEntity, HamonMasterModel> {
    private final AnimationProcessor animProcessor;
    
    private final IBendHelper mutatedJacket;
    private final IBendHelper mutatedRightSleeve;
    private final IBendHelper mutatedLeftSleeve;
    private final IBendHelper mutatedRightPantLeg;
    private final IBendHelper mutatedLeftPantLeg;
    
    public HamonMasterAnimApplier(HamonMasterModel model, IMutableModel modelWithMixin, IAnimation sittingAnim) {
        super(model, modelWithMixin);
        this.animProcessor = new AnimationProcessor(sittingAnim);
        SetableSupplier<AnimationProcessor> emoteSupplier = modelWithMixin.getEmoteSupplier();
        
        mutatedJacket = IBendHelper.create(model.jacket, false, emoteSupplier);
        mutatedRightSleeve = IBendHelper.create(model.rightSleeve, true, emoteSupplier);
        mutatedLeftSleeve = IBendHelper.create(model.leftSleeve, true, emoteSupplier);
        mutatedRightPantLeg = IBendHelper.create(model.rightPants, false, emoteSupplier);
        mutatedLeftPantLeg = IBendHelper.create(model.leftPants, false, emoteSupplier);
        
        mutatedJacket.addBendedCuboid(- 4, 0, - 2, 8, 12, 4, 0.25f, Direction.DOWN);
        mutatedRightPantLeg.addBendedCuboid(- 2, 0, - 2, 4, 12, 4, 0.25f, Direction.UP);
        mutatedLeftPantLeg.addBendedCuboid(- 2, 0, - 2, 4, 12, 4, 0.25f, Direction.UP);
        mutatedLeftSleeve.addBendedCuboid(- 1, - 2, - 2, 4, 12, 4, 0.25f, Direction.UP);
        mutatedRightSleeve.addBendedCuboid(- 3, - 2, - 2, 4, 12, 4, 0.25f, Direction.UP);
        
        modelWithMixin.setLeftArm(IBendHelper.create(model.leftArm, true, emoteSupplier));
        modelWithMixin.getLeftArm().addBendedCuboid(- 1, - 2, - 2, 4, 12, 4, 0, Direction.UP);
        modelWithMixin.setLeftLeg(IBendHelper.create(model.leftLeg, false, emoteSupplier));
        modelWithMixin.getLeftLeg().addBendedCuboid(- 2, 0, - 2, 4, 12, 4, 0, Direction.UP);
    }
    
    @Override
    public void onInit() {
        SetableSupplier<AnimationProcessor> animProcessor = modelWithMixin.getEmoteSupplier();
        animProcessor.set(this.animProcessor);
        modelWithMixin.setEmoteSupplier(animProcessor);
    }
    
    @Override
    public void setEmote() {
        super.setEmote();
        
        mutatedJacket.copyBend(modelWithMixin.getTorso());
        mutatedLeftPantLeg.copyBend(modelWithMixin.getLeftLeg());
        mutatedRightPantLeg.copyBend(modelWithMixin.getRightLeg());
        mutatedLeftSleeve.copyBend(modelWithMixin.getLeftArm());
        mutatedRightSleeve.copyBend(modelWithMixin.getRightArm());
    }
}
