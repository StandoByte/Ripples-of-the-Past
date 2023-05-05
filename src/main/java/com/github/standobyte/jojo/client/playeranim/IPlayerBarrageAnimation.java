package com.github.standobyte.jojo.client.playeranim;

import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.BarrageFistAfterimagesLayer;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.IBarrageAnimation;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;

public interface IPlayerBarrageAnimation extends IBarrageAnimation<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {
    void beforeSwingsRender(MatrixStack matrixStack, BarrageFistAfterimagesLayer playerModelLayer);
}
