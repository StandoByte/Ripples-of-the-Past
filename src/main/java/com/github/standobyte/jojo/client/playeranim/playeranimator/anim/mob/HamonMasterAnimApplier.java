package com.github.standobyte.jojo.client.playeranim.playeranimator.anim.mob;

import com.github.standobyte.jojo.client.render.entity.model.mob.HamonMasterModel;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.SetableSupplier;
import dev.kosmx.playerAnim.impl.IMutableModel;

public class HamonMasterAnimApplier extends EntityAnimApplier<HamonMasterEntity, HamonMasterModel> {
    private final IAnimation sittingAnim;

    public HamonMasterAnimApplier(HamonMasterModel model, IMutableModel modelWithMixin, IAnimation sittingAnim) {
        super(model, modelWithMixin);
        this.sittingAnim = sittingAnim;
    }

    @Override
    public void onInit() {
        SetableSupplier<AnimationProcessor> animProcessor = modelWithMixin.getEmoteSupplier();
        AnimationProcessor anim = new AnimationProcessor(sittingAnim);
        animProcessor.set(anim);
        modelWithMixin.setEmoteSupplier(animProcessor);
    }
}
