package com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage;

import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.ArmBarrageSwing;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.IBarrageAnimation;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.HandSide;

public class PlayerArmBarrageSwing extends ArmBarrageSwing<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {
    private final BarrageFistAfterimagesLayer effectLayer;

    public PlayerArmBarrageSwing(IBarrageAnimation<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> barrageAnim, 
            float ticks, float ticksMax, HandSide side, double maxOffset, 
            BarrageFistAfterimagesLayer effectLayer) {
        super(barrageAnim, ticks, ticksMax, side, maxOffset);
        this.effectLayer = effectLayer;
    }

    @Override
    protected void setArmOnlyModelVisibility(AbstractClientPlayerEntity entity, PlayerModel<AbstractClientPlayerEntity> model, HandSide side) {
        effectLayer.setArmsVisibility(model, side);
    }

}
