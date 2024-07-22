package com.github.standobyte.jojo.modcompat.mod.client.playeranimator.anim.hamon;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.playeranim.IPlayerBarrageAnimation;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.BarrageFistAfterimagesLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.PlayerArmBarrageSwing;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.BarrageSwingsHolder;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.TwoHandedBarrageAnimation;
import com.mojang.blaze3d.matrix.MatrixStack;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IMutableModel;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;

public class PlayerBarrageAfterimagesAnim extends TwoHandedBarrageAnimation<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> implements IPlayerBarrageAnimation {
    private final BarrageFistAfterimagesLayer modelLayer;
    private final PlayerBarrageAnim anim;

    public PlayerBarrageAfterimagesAnim(PlayerModel<AbstractClientPlayerEntity> model, 
            PlayerBarrageAnim barrageSwing, BarrageFistAfterimagesLayer modelLayer) {
        super(model, barrageSwing, null);
        this.modelLayer = modelLayer;
        this.anim = barrageSwing;
    }

    @Override
    public void animate(Phase phase, float phaseCompletion, AbstractClientPlayerEntity entity, float ticks, 
            float yRotOffsetRad, float xRotRad, HandSide side) {
        if (phase != Phase.PERFORM) {
            super.animate(phase, phaseCompletion, entity, ticks, yRotOffsetRad, xRotRad, side);
        }
    }

    @Override
    public BarrageSwingsHolder<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> getBarrageSwingsHolder(AbstractClientPlayerEntity entity) {
        return BarrageFistAfterimagesLayer.getSwings(entity);
    }

    @Override
    protected float swingsToAdd(AbstractClientPlayerEntity entity, float loop, float lastLoop) {
        return 3 * Math.min(loop - lastLoop, 1) * getLoopLen();
    }
    
    protected float getLoopLen() {
        return super.getLoopLen();
    }

    @Override
    protected double maxSwingOffset(AbstractClientPlayerEntity entity) {
        return 0.625;
    }

    @Override
    protected void addSwing(AbstractClientPlayerEntity entity, BarrageSwingsHolder<AbstractClientPlayerEntity, 
            PlayerModel<AbstractClientPlayerEntity>> swings, HandSide side, float f, double maxOffset) {
        swings.addSwing(new PlayerArmBarrageSwing(this, f, getLoopLen(), side, maxOffset, modelLayer));
    }

    @Override
    public void beforeSwingsRender(MatrixStack matrixStack, BarrageFistAfterimagesLayer playerModelLayer) {
        PlayerModel<AbstractClientPlayerEntity> model = playerModelLayer.getParentModel();
        if (model instanceof IMutableModel) {
            AnimationProcessor anim = ((IMutableModel) model).getEmoteSupplier().get();
            if (anim != null) {
                float yRot = anim.get3DTransform("body", TransformType.ROTATION, Vec3f.ZERO).getY();
                matrixStack.mulPose(Vector3f.YP.rotation(yRot));
            }
        }
    }
    
    @Override
    public void beforeSwingAfterimageRender(MatrixStack matrixStack, 
            PlayerModel<AbstractClientPlayerEntity> model, float loopCompletion, HandSide side) {
        anim.rotateBody(matrixStack, loopCompletion, side);
    }
}
