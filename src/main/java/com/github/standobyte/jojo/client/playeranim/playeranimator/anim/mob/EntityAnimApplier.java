package com.github.standobyte.jojo.client.playeranim.playeranimator.anim.mob;

import com.github.standobyte.jojo.client.ClientTicking;
import com.github.standobyte.jojo.client.playeranim.IEntityAnimApplier;
import com.mojang.blaze3d.matrix.MatrixStack;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IMutableModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3f;

// TODO a more generalized way to apply animations to custom mobs
public abstract class EntityAnimApplier<T extends LivingEntity, M extends BipedModel<T>> implements IEntityAnimApplier<T, M> {
    protected final M model;
    protected final IMutableModel modelWithMixin;
    
    public EntityAnimApplier(M model, IMutableModel modelWithMixin) {
        this.model = model;
        this.modelWithMixin = modelWithMixin;
        ClientTicking.addTicking(this);
    }

    @Override
    public void applyBodyTransforms(MatrixStack matrixStack, float partialTick) {
        AnimationProcessor pose = modelWithMixin.getEmoteSupplier().get();
        
        if (pose != null) {
            pose.setTickDelta(partialTick);
            if (pose.isActive()) {
                
                //These are additive properties
                Vec3f vec3d = pose.get3DTransform("body", TransformType.POSITION, Vec3f.ZERO);
                matrixStack.translate(vec3d.getX(), vec3d.getY() + 0.7, vec3d.getZ());
                Vec3f vec3f = pose.get3DTransform("body", TransformType.ROTATION, Vec3f.ZERO);
                matrixStack.mulPose(Vector3f.ZP.rotation(vec3f.getZ()));    //roll
                matrixStack.mulPose(Vector3f.YP.rotation(vec3f.getY()));    //pitch
                matrixStack.mulPose(Vector3f.XP.rotation(vec3f.getX()));    //yaw
                matrixStack.translate(0, - 0.7d, 0);
            }
        }
    }
    
    @Override
    public void setEmote() {
        AnimationProcessor pose = modelWithMixin.getEmoteSupplier().get();

        if (pose != null) {
            updatePart(pose, "head", model.head);
            model.hat.copyFrom(model.head);
            updatePart(pose, "leftArm", model.leftArm);
            updatePart(pose, "rightArm", model.rightArm);
            updatePart(pose, "leftLeg", model.leftLeg);
            updatePart(pose, "rightLeg", model.rightLeg);
            updatePart(pose, "torso", model.body);
            
            Pair<Float, Float> torsoBend = pose.getBend("torso");
            Pair<Float, Float> bodyBend = pose.getBend("body");
            modelWithMixin.getTorso().bend(new Pair<>(torsoBend.getLeft() + bodyBend.getLeft(), torsoBend.getRight() + bodyBend.getRight()));
            modelWithMixin.getLeftArm().bend(pose.getBend("leftArm"));
            modelWithMixin.getLeftLeg().bend(pose.getBend("leftLeg"));
            modelWithMixin.getRightArm().bend(pose.getBend("rightArm"));
            modelWithMixin.getRightLeg().bend(pose.getBend("rightLeg"));
        }
    }

    private void updatePart(AnimationProcessor pose, String partName, ModelRenderer part) {
        Vec3f pos = pose.get3DTransform(partName, TransformType.POSITION, new Vec3f(part.x, part.y, part.z));
        part.x = pos.getX();
        part.y = pos.getY();
        part.z = pos.getZ();
        Vec3f rot = pose.get3DTransform(partName, TransformType.ROTATION, new Vec3f(part.xRot, part.yRot, part.zRot));
        part.xRot = rot.getX();
        part.yRot = rot.getY();
        part.zRot = rot.getZ();
    }
    
    @Override
    public void tick() {
        AnimationProcessor pose = modelWithMixin.getEmoteSupplier().get();
        
        if (pose != null) {
            pose.tick();
        }
    }
}
