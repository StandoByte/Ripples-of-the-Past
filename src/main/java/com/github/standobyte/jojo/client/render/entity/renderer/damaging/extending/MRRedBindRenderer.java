package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import java.util.Map;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.MRRedBindModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.MRRedBindEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class MRRedBindRenderer extends ExtendingEntityRenderer<MRRedBindEntity, MRRedBindModel> {
    private boolean second = false;

    public MRRedBindRenderer(EntityRendererManager renderManager) {
        super(renderManager, new MRRedBindModel(), null);
    }

    @Override
    public void render(MRRedBindEntity entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        if (entity.isInKickAttack()) {
            second = !second;
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
            second = !second;
        }
    }
    
    private static final Vector3f bodyWindup = scale(new Vector3f(-49.1066F, -20.7048F, 22.2077F), MathUtil.DEG_TO_RAD);
    private static final Vector3f leftArmWindup = new Vector3f(0.3054F, 0.0F, -0.2182F);
    private static final Vector3f leftForeArmWindup = new Vector3f(-2.0944F, -0.2618F, 1.0472F);
    private static final Vector3f rightArmWindup = new Vector3f(0.1309F, 0.0F, 0.4363F);
    private static final Vector3f rightForeArmWindup = new Vector3f(-2.3562F, 0.2618F, -1.8326F);
    
    private static final Vector3f bodyKickStart = scale(new Vector3f(-54.7356F, -30F, 35.2644F), MathUtil.DEG_TO_RAD);
    private static final Vector3f leftArmKickStart = scale(new Vector3f(-60F, 0.0F, -45F), MathUtil.DEG_TO_RAD);
    private static final Vector3f leftForeArmKickStart = scale(new Vector3f(0.0F, 0.0F, 50F), MathUtil.DEG_TO_RAD);
    private static final Vector3f rightArmKickStart = scale(new Vector3f(45F, -10F, 10F), MathUtil.DEG_TO_RAD);
    private static final Vector3f rightForeArmKickStart = scale(new Vector3f(0F, 0F, 0F), MathUtil.DEG_TO_RAD);
    
    private static final Vector3f bodyKickEnd = scale(new Vector3f(-59.3179F, -27.034F, 37.4537F), MathUtil.DEG_TO_RAD);
    private static final Vector3f leftArmKickEnd = scale(new Vector3f(-135F, -15F, 30F), MathUtil.DEG_TO_RAD);
    private static final Vector3f leftForeArmKickEnd = scale(new Vector3f(0.0F, 0.0F, 50F), MathUtil.DEG_TO_RAD);
    private static final Vector3f rightArmKickEnd = scale(new Vector3f(-180F, 30F, -45F), MathUtil.DEG_TO_RAD);
    private static final Vector3f rightForeArmKickEnd = scale(new Vector3f(0F, 0F, -45F), MathUtil.DEG_TO_RAD);
    
    private static final Map<ModelPart, Vector3f[]> ROTATIONS = ImmutableMap.<ModelPart, Vector3f[]>builder()
            .put(ModelPart.BODY, new Vector3f[] { bodyWindup, bodyKickStart, bodyKickEnd })
            .put(ModelPart.LEFT_ARM, new Vector3f[] { leftArmWindup, leftArmKickStart, leftArmKickEnd })
            .put(ModelPart.LEFT_FOREARM, new Vector3f[] { leftForeArmWindup, leftForeArmKickStart, leftForeArmKickEnd })
            .put(ModelPart.RIGHT_ARM, new Vector3f[] { rightArmWindup, rightArmKickStart, rightArmKickEnd })
            .put(ModelPart.RIGHT_FOREARM, new Vector3f[] { rightForeArmWindup, rightForeArmKickStart, rightForeArmKickEnd })
            .build();
    
    private static Vector3f scale(Vector3f vec, float factor) {
        vec.mul(factor);
        return vec;
    }
    
    private Vector3f getPartRotation(ModelPart part, StandEntityAction.Phase phase, float completion) {
        Vector3f[] partRotations = ROTATIONS.get(part);
        Vector3f vec = null;
        switch (phase) {
        case WINDUP:
            vec = partRotations[0].copy();
            vec.lerp(partRotations[1], completion);
            break;
        case PERFORM:
            vec = partRotations[1].copy();
            vec.lerp(partRotations[2], completion);
            break;
        case RECOVERY:
            vec = partRotations[2].copy();
            break;
        default:
            break;
        }
        return vec;
    }
    
    private enum ModelPart {
        BODY,
        LEFT_ARM,
        LEFT_FOREARM,
        RIGHT_ARM,
        RIGHT_FOREARM
    }
    
    private Vector3d rotateVec(Vector3d vec, Vector3f rotations, HandSide side) {
        return vec.zRot(side == HandSide.LEFT ? rotations.z() : -rotations.z())
                .yRot(-rotations.y()).xRot(rotations.x());
    }
    
    @Override
    protected Vector3d getOriginPos(MRRedBindEntity entity, float partialTick) {
        Vector3d originPos = super.getOriginPos(entity, partialTick);
        if (entity.isInKickAttack() && entity.getOwner() instanceof StandEntity) {
            StandEntity magiciansRed = (StandEntity) entity.getOwner();
            if (magiciansRed.getCurrentTask().isPresent()) {
                StandEntityAction.Phase phase = magiciansRed.getCurrentTaskPhase().get();
                float anim = magiciansRed.getCurrentTaskPhaseCompletion(partialTick);
                originPos = MCUtil.getEntityPosition(entity.getOwner(), partialTick).add(0, entity.getOwner().getBbHeight() * 0.75F, 0);
                Vector3d shoulder = new Vector3d(0, -0.234375, 0);
                Vector3d foreArm = new Vector3d(0, -0.234375, 0);
                float yRot = 180F - MathHelper.lerp(partialTick, entity.getOwner().yRotO, entity.getOwner().yRot);
                if (second) {
                    Vector3d posArmLeft = new Vector3d(-0.3515625, -0.1171875, 0);
                    posArmLeft = rotateVec(posArmLeft, getPartRotation(ModelPart.BODY, phase, anim), HandSide.LEFT);
                    shoulder = rotateVec(shoulder, getPartRotation(ModelPart.BODY, phase, anim), HandSide.LEFT);
                    shoulder = rotateVec(shoulder, getPartRotation(ModelPart.LEFT_ARM, phase, anim), HandSide.LEFT);
                    foreArm = rotateVec(foreArm, getPartRotation(ModelPart.BODY, phase, anim), HandSide.LEFT);
                    foreArm = rotateVec(foreArm, getPartRotation(ModelPart.LEFT_ARM, phase, anim), HandSide.LEFT);
                    foreArm = rotateVec(foreArm, getPartRotation(ModelPart.LEFT_FOREARM, phase, anim), HandSide.LEFT);
                    originPos = originPos.add(
                            posArmLeft
                            .add(shoulder)
                            .add(foreArm)
                            .yRot(yRot * MathUtil.DEG_TO_RAD))
                            ;
                }
                else {
                    Vector3d posArmRight = new Vector3d(0.3515625, -0.1171875, 0);
                    posArmRight = rotateVec(posArmRight, getPartRotation(ModelPart.BODY, phase, anim), HandSide.RIGHT);
                    shoulder = rotateVec(shoulder, getPartRotation(ModelPart.BODY, phase, anim), HandSide.RIGHT);
                    shoulder = rotateVec(shoulder, getPartRotation(ModelPart.RIGHT_ARM, phase, anim), HandSide.RIGHT);
                    foreArm = rotateVec(foreArm, getPartRotation(ModelPart.BODY, phase, anim), HandSide.RIGHT);
                    foreArm = rotateVec(foreArm, getPartRotation(ModelPart.RIGHT_ARM, phase, anim), HandSide.RIGHT);
                    foreArm = rotateVec(foreArm, getPartRotation(ModelPart.RIGHT_FOREARM, phase, anim), HandSide.RIGHT);
                    originPos = originPos.add(
                            posArmRight
                            .add(shoulder)
                            .add(foreArm)
                            .yRot(yRot * MathUtil.DEG_TO_RAD))
                            ;
                }
            }
        }
        return originPos;
    }
    
    @Override
    protected void doRender(MRRedBindEntity entity, MRRedBindModel model, 
            float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        renderModel(entity, model, partialTick, matrixStack, buffer.getBuffer(Atlases.translucentCullBlockSheet()), packedLight);
    }

}
