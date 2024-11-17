package com.github.standobyte.jojo.client.ui.actionshud;

import com.github.standobyte.jojo.client.ui.BlitFloat;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;

public class RadialBar {
    private static final float PI = (float) Math.PI;

    // FIXME breaks when using non-zero angle0 with fill > 0.5 (not planning to do that anyway, but when i've got nothing else to do might as well fix that)
    public static void render(MatrixStack matrixStack, float x, float y, 
            float angle0, float fill, 
            float emptyTexU, float emptyTexV, float filledTexU, float filledTexV, 
            float uWidth, float vHeight, float texWidth, float texHeight, int blitOffset) {
        BlitFloat.blitFloat(matrixStack, x, y, blitOffset, 
                emptyTexU, emptyTexV, uWidth, vHeight, texWidth, texHeight);
        if (fill == 0) return;
        if (fill <= -1 || fill >= 1) {
            BlitFloat.blitFloat(matrixStack, x, y, blitOffset, 
                    filledTexU, filledTexV, uWidth, vHeight, texWidth, texHeight);
            return;
        }
        
        angle0 = MathUtil.wrapRadians(angle0);
        float angle1 = angle0 + PI * 2 * fill;
        if (fill < 0) {
            float swap = angle1;
            angle1 = angle0;
            angle0 = swap;
            
            float angle0Wr = MathUtil.wrapRadians(angle0);
            angle1 += (angle0Wr - angle0);
            angle0 += (angle0Wr - angle0);
        }

        Matrix4f matrix = matrixStack.last().pose();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuilder();
        float xWidth = uWidth;
        float yHeight = vHeight;
        float halfWidth = xWidth / 2;
        float halfHeight = yHeight / 2;
        float minX = x;
        float maxX = x + xWidth;
        float minY = y;
        float maxY = y + yHeight;
        float minU = filledTexU / texWidth;
        float maxU = (filledTexU + uWidth) / texWidth;
        float minV = filledTexV / texHeight;
        float maxV = (filledTexV + vHeight) / texHeight;
        
        float x0 = (minX + maxX) / 2;
        float y0 = (minY + maxY) / 2;
        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float angleV1;
        float angleV3;
        float cosV1;
        float sinV1;
        float cosV3;
        float sinV3;
        float scaleV1;
        float scaleV3;
        
        for (Quadrant quadrant : Quadrant.values()) {
            if (quadrant.ordinal() == 2 && angle1 > PI) {
                angle0 -= PI * 2;
                angle1 -= PI * 2;
            }
            
            if (angle1 > quadrant.minAngle && angle0 <= quadrant.maxAngle) {
                angleV1 = Math.max(angle0, quadrant.minAngle) - quadrant.minAngle;
                angleV3 = Math.min(angle1, quadrant.maxAngle) - quadrant.minAngle;
                cosV1 = MathHelper.cos(angleV1);
                sinV1 = MathHelper.sin(angleV1);
                cosV3 = MathHelper.cos(angleV3);
                sinV3 = MathHelper.sin(angleV3);
                
                switch (quadrant) {
                case LOWER_RIGHT:
                    x1 = cosV1;
                    y1 = sinV1;
                    x3 = cosV3;
                    y3 = sinV3;
                    break;
                case UPPER_RIGHT:
                    x1 =  sinV1;
                    y1 = -cosV1;
                    x3 =  sinV3;
                    y3 = -cosV3;
                    break;
                case UPPER_LEFT:
                    x1 = -cosV1;
                    y1 = -sinV1;
                    x3 = -cosV3;
                    y3 = -sinV3;
                    break;
                case LOWER_LEFT:
                    x1 = -sinV1;
                    y1 =  cosV1;
                    x3 = -sinV3;
                    y3 =  cosV3;
                    break;
                default:
                    throw new AssertionError();
                }
                
                scaleV1 = cosV1 > sinV1 ? halfWidth / cosV1 : halfHeight / sinV1;
                scaleV3 = cosV3 > sinV3 ? halfWidth / cosV3 : halfHeight / sinV3;
                x1 *= scaleV1;
                y1 *= scaleV1;
                x3 *= scaleV3;
                y3 *= scaleV3;
                x1 += x0;
                y1 += y0;
                x3 += x0;
                y3 += y0;
                
                switch (quadrant) {
                case LOWER_RIGHT:
                    x2 = Math.min(x1, maxX);
                    y2 = Math.min(y3, maxY);
                    break;
                case UPPER_RIGHT:
                    x2 = Math.min(x3, maxX);
                    y2 = Math.max(y1, minY);
                    break;
                case UPPER_LEFT:
                    x2 = Math.max(x1, minX);
                    y2 = Math.max(y3, minY);
                    break;
                case LOWER_LEFT:
                    x2 = Math.max(x3, minX);
                    y2 = Math.min(y1, maxY);
                    break;
                default:
                    throw new AssertionError();
                }
                
                bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferBuilder.vertex(matrix, x3, y3, blitOffset).uv(
                        lerpUV(x3, minX, maxX, minU, maxU), 
                        lerpUV(y3, minY, maxY, minV, maxV))
                .endVertex();
                bufferBuilder.vertex(matrix, x2, y2, blitOffset).uv(
                        lerpUV(x2, minX, maxX, minU, maxU), 
                        lerpUV(y2, minY, maxY, minV, maxV))
                .endVertex();
                bufferBuilder.vertex(matrix, x1, y1, blitOffset).uv(
                        lerpUV(x1, minX, maxX, minU, maxU), 
                        lerpUV(y1, minY, maxY, minV, maxV))
                .endVertex();
                bufferBuilder.vertex(matrix, x0, y0, blitOffset).uv(
                        lerpUV(x0, minX, maxX, minU, maxU), 
                        lerpUV(y0, minY, maxY, minV, maxV))
                .endVertex();
                bufferBuilder.end();
                RenderSystem.enableAlphaTest();
                WorldVertexBufferUploader.end(bufferBuilder);
            }
        }
    }
    
    private static enum Quadrant {
        LOWER_RIGHT( PI / 2,  PI),
        UPPER_RIGHT( 0,       PI / 2),
        UPPER_LEFT( -PI / 2,  0),
        LOWER_LEFT( -PI,     -PI / 2);
        
        private final float minAngle;
        private final float maxAngle;
        
        private Quadrant(float minAngle, float maxAngle) {
            this.minAngle = minAngle;
            this.maxAngle = maxAngle;
        }
    }
    
    private static float lerpUV(float coord, float coord0, float coord1, float minUV, float maxUV) {
        return (float) MathHelper.lerp(MathHelper.inverseLerp(coord, coord0, coord1), minUV, maxUV);
    }
}
