package com.github.standobyte.jojo.client.particle.custom;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.TexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;

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
            super.render(pBuffer, pRenderInfo, pPartialTicks); // IllegalStateException: Not filled all elements of the vertex
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
