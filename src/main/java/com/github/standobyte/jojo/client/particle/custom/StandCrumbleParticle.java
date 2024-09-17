package com.github.standobyte.jojo.client.particle.custom;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.TexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class StandCrumbleParticle extends TexturedParticle {
    private ResourceLocation texture;
    private float u0;
    private float v0;
    private float u1;
    private float v1;
    
    public StandCrumbleParticle(ClientWorld world, double pX, double pY, double pZ) {
        super(world, pX, pY, pZ);
    }
    
    public StandCrumbleParticle(ClientWorld world, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(world, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
    }
    
    public void setTextureAndUv(ResourceLocation texture, float u0, float v0, float u1, float v1) {
        this.texture = texture;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
    }

    @Override
    public void render(IVertexBuilder pBuffer, ActiveRenderInfo pRenderInfo, float pPartialTicks) {
        if (texture != null) {
            pBuffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.entityCutoutNoCull(texture));
            
            
            Vector3d pos = pRenderInfo.getPosition();
            float x = (float)(MathHelper.lerp((double)pPartialTicks, this.xo, this.x) - pos.x());
            float y = (float)(MathHelper.lerp((double)pPartialTicks, this.yo, this.y) - pos.y());
            float z = (float)(MathHelper.lerp((double)pPartialTicks, this.zo, this.z) - pos.z());
//            Quaternion quaternion;
//            if (this.roll == 0.0F) {
//                quaternion = pRenderInfo.rotation();
//            } else {
//                quaternion = new Quaternion(pRenderInfo.rotation());
//                float roll = MathHelper.lerp(pPartialTicks, this.oRoll, this.roll);
//                quaternion.mul(Vector3f.ZP.rotation(roll));
//            }

//            Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
//            vector3f1.transform(quaternion);
            Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
            float size = this.getQuadSize(pPartialTicks);

            for(int i = 0; i < 4; ++i) {
                Vector3f vector3f = avector3f[i];
//                vector3f.transform(quaternion);
                vector3f.mul(size);
                vector3f.add(x, y, z);
            }
            
            Vector3f normal = new Vector3f(1, 1, 1);
            float u0 = this.getU0();
            float u1 = this.getU1();
            float v0 = this.getV0();
            float v1 = this.getV1();
            int light = this.getLightColor(pPartialTicks);
            
            pBuffer
            .vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z())
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv(u1, v1)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(normal.x(), normal.y(), normal.z())
            .endVertex();
            
            pBuffer
            .vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z())
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv(u1, v0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(normal.x(), normal.y(), normal.z())
            .endVertex();
            
            pBuffer
            .vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z())
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv(u0, v0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(normal.x(), normal.y(), normal.z())
            .endVertex();
            
            pBuffer
            .vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z())
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv(u0, v1)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(normal.x(), normal.y(), normal.z())
            .endVertex();
        }
    }

    @Override
    protected float getU0() {
        return u0;
    }

    @Override
    protected float getU1() {
        return u1;
    }

    @Override
    protected float getV0() {
        return v0;
    }

    @Override
    protected float getV1() {
        return v1;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.CUSTOM;
    }
}
