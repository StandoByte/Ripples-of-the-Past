package com.github.standobyte.jojo.client.render;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Vector3f;

public class FlameModelRenderer extends ModelRenderer {
    private final ObjectList<FlameModelRenderer.Flame> flames = new ObjectArrayList<>();
    private Supplier<TextureAtlasSprite> spriteFire0 = () -> ModelBakery.FIRE_0.sprite();
    private Supplier<TextureAtlasSprite> spriteFire1 = () -> ModelBakery.FIRE_1.sprite();

    public FlameModelRenderer(Model model) {
        super(model);
    }
    
    public FlameModelRenderer setFireSprites(Supplier<TextureAtlasSprite> spriteFire0, Supplier<TextureAtlasSprite> spriteFire1) {
        this.spriteFire0 = spriteFire0;
        this.spriteFire1 = spriteFire1;
        return this;
    }

    public ModelRenderer addFlame(float bottomY, float width, float height) {
        return addFlame(0, bottomY, 0, width, height, Direction.UP);
    }

    public ModelRenderer addFlame(float centerX, float bottomY, float centerZ, float width, float height) {
        return addFlame(centerX, bottomY, centerZ, width, height, Direction.UP);
    }

    public ModelRenderer addFlame(float x, float y, float z, float width, float height, Direction flameDirection) {
        flames.add(new FlameModelRenderer.Flame(x, y, z, width, height, flameDirection));
        return this;
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.render(matrixStack, vertexBuilder, packedLight, packedOverlay, red, green, blue, alpha);
        if (visible && !flames.isEmpty()) {
            matrixStack.pushPose();
            translateAndRotate(matrixStack);
            packedLight = ClientUtil.MAX_MODEL_LIGHT;
            for (FlameModelRenderer.Flame flame : flames) {
                renderFlame(matrixStack, vertexBuilder, packedLight, packedOverlay, red, green, blue, alpha, 
                        flame.width, flame.height, 
                        flame.x, flame.y, flame.z, flame.flameDirection);
            }
            matrixStack.popPose();
        }
    }

    public static boolean renderingUI = false;
    private void renderFlame(MatrixStack matrixStack, IVertexBuilder vertexBuilder, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha, 
            float width, float height, 
            double xOffset, double yOffset, double zOffset, Direction flameDirection) {
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, zOffset);
        Matrix3f lightNormal = matrixStack.last().normal();
        lightNormal.setIdentity();
        if (!renderingUI) {
            ActiveRenderInfo camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            float cameraXRot = camera.getXRot();
            lightNormal.mul(-1);
            lightNormal.mul(Vector3f.XP.rotationDegrees(cameraXRot));
        }
        switch (flameDirection) {
        case UP:
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
            break;
        case DOWN:
            lightNormal.mul(Vector3f.XP.rotationDegrees(180));
            break;
        case NORTH:
            matrixStack.mulPose(Vector3f.XN.rotationDegrees(90));
            lightNormal.mul(Vector3f.XN.rotationDegrees(90));
            break;
        case EAST:
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90));
            lightNormal.mul(Vector3f.ZP.rotationDegrees(90));
            break;
        case SOUTH:
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
            lightNormal.mul(Vector3f.XP.rotationDegrees(90));
            break;
        case WEST:
            matrixStack.mulPose(Vector3f.ZN.rotationDegrees(90));
            lightNormal.mul(Vector3f.ZN.rotationDegrees(90));
            break;
        }
        
        for (int yRot = 0; yRot < 4; yRot++) {
            matrixStack.pushPose();
            float f0 = width * 1.4F;
            matrixStack.scale(f0, f0, f0);
            float f1 = 0.5F;
            float f2 = 0.0F;
            float f3 = height / f0;
            float f4 = 0.0F;
            matrixStack.mulPose(Vector3f.YP.rotation(yRot * (float) Math.PI / 2));
            matrixStack.translate(0.0D, 0.0D, (double)(-0.4F + (float)((int)f3) * 0.02F));
            float f5 = 0.0F;
            int i = 0;
            for (MatrixStack.Entry matrixstack$entry = matrixStack.last(); f3 > 0.0F; ++i) {
                TextureAtlasSprite sprite = i % 2 == 0 ? spriteFire0.get() : spriteFire1.get();
                float texU0 = sprite.getU0();
                float texV0 = sprite.getV0();
                float texU1 = sprite.getU1();
                float texV1 = sprite.getV1();
                if (i / 2 % 2 == 0) {
                    float tmp = texU1;
                    texU1 = texU0;
                    texU0 = tmp;
                }
                ClientUtil.vertex(matrixstack$entry, vertexBuilder, 
                        packedLight, packedOverlay, red, green, blue, alpha, 
                        f1 - f2, f2 - f4, f5, texU1, texV1);
                ClientUtil.vertex(matrixstack$entry, vertexBuilder, 
                        packedLight, packedOverlay, red, green, blue, alpha, 
                        -f1 - f2, f2 - f4, f5, texU0, texV1);
                ClientUtil.vertex(matrixstack$entry, vertexBuilder, 
                        packedLight, packedOverlay, red, green, blue, alpha, 
                        -f1 - f2, 1.4F - f4, f5, texU0, texV0);
                ClientUtil.vertex(matrixstack$entry, vertexBuilder, 
                        packedLight, packedOverlay, red, green, blue, alpha, 
                        f1 - f2, 1.4F - f4, f5, texU1, texV0);
                f3 -= 0.45F;
                f4 -= 0.45F;
                f1 *= 0.9F;
                f5 += 0.03F;
            }
            matrixStack.popPose();
        }
        matrixStack.popPose();
    }



    private static class Flame {
        private final double x;
        private final double y;
        private final double z;
        private final float width;
        private final float height;
        private final Direction flameDirection;

        private Flame(float x, float y, float z, float width, float height, Direction flameDirection) {
            this.x = x / 16F;
            this.y = y / 16F;
            this.z = z / 16F;
            this.width = width / 16F;
            this.height = height / 16F;
            this.flameDirection = flameDirection;
        }
    }
}
