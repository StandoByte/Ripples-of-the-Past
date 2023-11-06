package com.github.standobyte.jojo.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class AirStreamParticle extends SpriteTexturedParticle {
    private final float yRot;
    private final float xRot;

    protected AirStreamParticle(ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        quadSize *= 2;
        this.yRot = (float) MathHelper.atan2(xSpeed, zSpeed);
        this.xRot = (float) MathHelper.atan2(ySpeed, MathHelper.sqrt(xSpeed * xSpeed + zSpeed * zSpeed));
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void render(IVertexBuilder vertexBuilder, ActiveRenderInfo renderInfo, float partialTick) {
        renderFromRotation(vertexBuilder, renderInfo, partialTick, yRot, xRot, true);
        renderFromRotation(vertexBuilder, renderInfo, partialTick, yRot, xRot, false);
    }
    
    private void renderFromRotation(IVertexBuilder vertexBuilder, ActiveRenderInfo renderInfo, float partialTick, float yRot, float xRot, boolean mirror) {
        Vector3d pos = renderInfo.getPosition();
        float f = (float) (MathHelper.lerp(partialTick, xo, x) - pos.x());
        float f1 = (float) (MathHelper.lerp(partialTick, yo, y) - pos.y());
        float f2 = (float) (MathHelper.lerp(partialTick, zo, z) - pos.z());
        Quaternion quaternion = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
        quaternion.mul(Vector3f.YP.rotation(yRot));
        quaternion.mul(Vector3f.XP.rotation(-xRot));
        if (mirror) {
            quaternion.mul(Vector3f.YP.rotation((float) Math.PI / 2F));
        }
        else {
            quaternion.mul(Vector3f.YP.rotation(-(float) Math.PI / 2F));
        }
        

        Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
        vector3f1.transform(quaternion);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f4 = getQuadSize(partialTick);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.transform(quaternion);
            vector3f.mul(f4);
            vector3f.add(f, f1, f2);
        }

        float u0 = mirror ? getU1() : getU0();
        float u1 = mirror ? getU0() : getU1();
        float v0 = getV0();
        float v1 = getV1();
        int light = getLightColor(partialTick);
        vertexBuilder.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).uv(u1, v1).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        vertexBuilder.vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).uv(u1, v0).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        vertexBuilder.vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).uv(u0, v0).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        vertexBuilder.vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).uv(u0, v1).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
        } else {
            move(xd, yd, zd);
        }
    }


    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            AirStreamParticle particle = new AirStreamParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(spriteSet);
            particle.scale(1.5F);
            particle.setLifetime(16);
            particle.hasPhysics = false;
            return particle;
        }
    }

}
