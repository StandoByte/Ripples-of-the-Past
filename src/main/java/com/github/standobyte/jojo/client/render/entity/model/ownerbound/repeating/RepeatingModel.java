package com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating;

import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3f;

public abstract class RepeatingModel<T extends Entity> extends EntityModel<T> {
    private static final Random RANDOM = new Random();
    
    private float length;
    private float yRotation;
    private float xRotation;

    protected RepeatingModel() {
        super(RenderType::entityTranslucent);
    }

    @Nullable
    protected abstract ModelRenderer getMainPart();
    
    protected abstract float getMainPartLength();
    
    protected abstract ModelRenderer getRepeatingPart();
    
    protected abstract float getRepeatingPartLength();
    
    protected boolean squareModelRandomRotation() {
        return false;
    }
    
    @Override
    public void setupAnim(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        this.yRotation = yRotationOffset;
        this.xRotation = xRotation;
        RANDOM.setSeed(entity.getId());
    }
    
    public void setLength(float length) {
        this.length = length;
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(yRotation));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(xRotation));
        float modelLength = length;
        ModelRenderer mainPart = getMainPart();
        float mainPartLength = getMainPartLength() / 16F;
        if (mainPart != null && modelLength >= mainPartLength) {
            mainPart.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            modelLength -= mainPartLength;
        }
        ModelRenderer repeatingPart = getRepeatingPart();
        float repeatingLength = getRepeatingPartLength() / 16F;
        while (modelLength >= repeatingLength) {
            if (squareModelRandomRotation()) {
                repeatingPart.zRot = RANDOM.nextInt(4) * 90F * MathUtil.DEG_TO_RAD;
            }
            repeatingPart.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            modelLength -= repeatingLength;
            matrixStack.translate(0, 0, repeatingLength);
        }
        repeatingPart.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-xRotation));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-yRotation));
        matrixStack.popPose();
    }
}
