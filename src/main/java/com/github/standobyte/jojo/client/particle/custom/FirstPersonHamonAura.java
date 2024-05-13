package com.github.standobyte.jojo.client.particle.custom;

import java.util.Iterator;
import java.util.Queue;
import java.util.Random;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class FirstPersonHamonAura {
    private static final IParticleRenderType RENDER_TYPE = HamonAuraParticleRenderType.HAMON_AURA;
    private final Queue<FirstPersonPseudoParticle> particlesToAdd = Queues.newArrayDeque();
    private final Queue<FirstPersonPseudoParticle> particles = EvictingQueue.create(16384);
    
    private FirstPersonHamonAura() {}
    
    private static FirstPersonHamonAura instance;
    public static void init() {
        instance = new FirstPersonHamonAura();
    }
    
    public static FirstPersonHamonAura getInstance() {
        return instance;
    }
    
    
    
    public void add(FirstPersonPseudoParticle pEffect) {
        this.particlesToAdd.add(pEffect);
    }

    public void tick() {
        if (!particles.isEmpty()) {
            Iterator<FirstPersonPseudoParticle> iterator = particles.iterator();

            while(iterator.hasNext()) {
                FirstPersonPseudoParticle particle = iterator.next();
                try {
                    particle.tick();
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking Particle");
                    CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being ticked");
                    crashreportcategory.setDetail("Particle", particle::toString);
                    crashreportcategory.setDetail("Particle Type", RENDER_TYPE::toString);
                    throw new ReportedException(crashreport);
                }
                if (!particle.isAlive()) {
                    iterator.remove();
                }
            }
        }

        FirstPersonPseudoParticle particle;
        if (!particlesToAdd.isEmpty()) {
            while((particle = particlesToAdd.poll()) != null) {
                particles.add(particle);
            }
        }

    }


    @SuppressWarnings("deprecation")
    public void renderParticles(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, LightTexture pLightTexture, 
            ActiveRenderInfo pActiveRenderInfo, float pPartialTicks, HandSide handSide) {
        if (particles.isEmpty()) return;
        
        pLightTexture.turnOnLightLayer();
        Runnable enable = () -> {
            RenderSystem.enableAlphaTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.enableFog();
            RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE2);
            RenderSystem.enableTexture();
            RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        };
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(pMatrixStack.last().pose());

        enable.run(); //Forge: MC-168672 Make sure all render types have the correct GL state.
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        RENDER_TYPE.begin(bufferbuilder, Minecraft.getInstance().textureManager);
        
        for (FirstPersonPseudoParticle particle : particles) {
            try {
                particle.render(bufferbuilder, pActiveRenderInfo, pPartialTicks);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                crashreportcategory.setDetail("Particle", particle::toString);
                crashreportcategory.setDetail("Particle Type", RENDER_TYPE::toString);
                throw new ReportedException(crashreport);
            }
        }

        RENDER_TYPE.end(tessellator);

        RenderSystem.popMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
        RenderSystem.disableBlend();
        RenderSystem.defaultAlphaFunc();
        pLightTexture.turnOffLightLayer();
        RenderSystem.disableFog();
    }
    
    
    
    public static class FirstPersonPseudoParticle {
        private static final Random RANDOM = new Random();
        private static final AxisAlignedBB INITIAL_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        private double xo;
        private double yo;
        private double zo;
        private double x;
        private double y;
        private double z;
        private double xd;
        private double yd;
        private double zd;
        private AxisAlignedBB bb = INITIAL_AABB;
        private boolean removed;
        private float bbWidth = 0.6F;
        private float bbHeight = 1.8F;
        private int age;
        private int lifetime;
        private float rCol = 1.0F;
        private float gCol = 1.0F;
        private float bCol = 1.0F;
        private float alpha = 1.0F;
        private float quadSize = 0.1F * (RANDOM.nextFloat() * 0.5F + 0.5F) * 2.0F;
        private TextureAtlasSprite sprite;
        private final IAnimatedSprite sprites;
        private final double fallSpeed;
        private final int startingSpriteRandom;
        
        
        public static FirstPersonPseudoParticle createParticle(IAnimatedSprite sprites, double x, double y, double z) {
            FirstPersonPseudoParticle particle = new FirstPersonPseudoParticle(x, y, z, 0, 0, 0, sprites);
            return particle;
        }



        private FirstPersonPseudoParticle(double x, double y, double z, 
                double xda, double yda, double zda, IAnimatedSprite sprites) {
            this.setSize(0.2F, 0.2F);
            this.setPos(x, y, z);
            this.xo = x;
            this.yo = y;
            this.zo = z;
            this.lifetime = (int)(4.0F / (RANDOM.nextFloat() * 0.9F + 0.1F));

            this.xd = (Math.random() * 2.0 - 1.0) * 0.4;
            this.yd = (Math.random() * 2.0 - 1.0) * 0.4;
            this.zd = (Math.random() * 2.0 - 1.0) * 0.4;
            double f = (Math.random() + Math.random() + 1.0) * 0.15;
            double f1 = MathHelper.sqrt(xd * xd + yd * yd + zd * zd);
            this.xd = xd / f1 * f * 0.4;
            this.yd = yd / f1 * f * 0.4 + 0.1;
            this.zd = zd / f1 * f * 0.4;

            this.fallSpeed = 0.004;
            this.sprites = sprites;
            this.xd *= 0.1;
            this.yd *= 0.1;
            this.zd *= 0.1;
            this.xd += xda;
            this.yd += yda;
            this.zd += zda;
            float f3 = 1.2F + 0.6F * RANDOM.nextFloat();
            this.quadSize *= 0.75F * f3;
            this.lifetime = (int)(8 / (RANDOM.nextDouble() * 0.8 + 0.2));
            this.lifetime = (int)((float) lifetime * f3);
            this.lifetime = Math.max(lifetime, 1);

            this.rCol = 1;
            this.gCol = 1;
            this.bCol = 1;
            lifetime = 25 + RANDOM.nextInt(10);
            startingSpriteRandom = RANDOM.nextInt(lifetime);
            setSpriteFromAge(sprites);
            alpha = 0.25F;
        }

        private int getLightColor(float partialTick) {
            return 0xF000F0;
        }

        public void tick() {
            xo = x;
            yo = y;
            zo = z;
            if (age++ >= lifetime) {
                remove();
            } else {
                setSpriteFromAge(sprites);
                yd += fallSpeed;
//                move(xd, yd, zd);
                if (y == yo) {
                    xd *= 1.1D;
                    zd *= 1.1D;
                }

                xd *= 0.96;
                yd *= 0.96;
                zd *= 0.96;

            }
        }

        private static final float ALPHA_MIN = 0.05F;
        private static final float ALPHA_DIFF = 0.3F;
        public void render(IVertexBuilder buffer, ActiveRenderInfo camera, float partialTick) {
            float ageF = ((float) age + partialTick) / (float) lifetime;
            float alphaFunc = ageF <= 0.5F ? ageF * 2 : (1 - ageF) * 2;
            this.alpha = ALPHA_MIN + alphaFunc * ALPHA_DIFF;

            float f = (float)(MathHelper.lerp((double)partialTick, this.xo, this.x));
            float f1 = (float)(MathHelper.lerp((double)partialTick, this.yo, this.y));
            float f2 = (float)(MathHelper.lerp((double)partialTick, this.zo, this.z));
            Quaternion quaternion = camera.rotation();
            quaternion = Quaternion.ONE;

            Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
            vector3f1.transform(quaternion);
            Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
            float f4 = this.getQuadSize(partialTick);

            for(int i = 0; i < 4; ++i) {
                Vector3f vector3f = avector3f[i];
                vector3f.transform(quaternion);
                vector3f.mul(f4);
                vector3f.add(f, f1, f2);
            }

            float u0 = sprite.getU0();
            float u1 = sprite.getU1();
            float v0 = sprite.getV0();
            float v1 = sprite.getV1();
            int j = getLightColor(partialTick);
            buffer.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).uv(u1, v1).color(rCol, gCol, bCol, alpha).uv2(j).endVertex();
            buffer.vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).uv(u1, v0).color(rCol, gCol, bCol, alpha).uv2(j).endVertex();
            buffer.vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).uv(u0, v0).color(rCol, gCol, bCol, alpha).uv2(j).endVertex();
            buffer.vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).uv(u0, v1).color(rCol, gCol, bCol, alpha).uv2(j).endVertex();
        }

        private void setSpriteFromAge(IAnimatedSprite pSprite) {
            setSprite(pSprite.get((age + startingSpriteRandom) % lifetime, lifetime));
        }

        private void setSprite(TextureAtlasSprite pSprite) {
            this.sprite = pSprite;
        }

        private float getQuadSize(float pScaleFactor) {
            return quadSize * MathHelper.clamp(((float)age + pScaleFactor) / (float)lifetime * 32.0F, 0.0F, 1.0F);
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + ", Pos (" + this.x + "," + this.y + "," + this.z + "), RGBA (" + this.rCol + "," + this.gCol + "," + this.bCol + "," + this.alpha + "), Age " + this.age;
        }

        private void remove() {
            this.removed = true;
        }

        public boolean isAlive() {
            return !this.removed;
        }

        private void setSize(float pWidth, float pHeight) {
            if (pWidth != this.bbWidth || pHeight != this.bbHeight) {
                this.bbWidth = pWidth;
                this.bbHeight = pHeight;
                double x0 = (bb.minX + bb.maxX - pWidth) / 2.0;
                double z0 = (bb.minZ + bb.maxZ - pWidth) / 2.0;
                bb = new AxisAlignedBB(x0, bb.minY, z0, x0 + (double)this.bbWidth, bb.minY + (double)this.bbHeight, z0 + (double)this.bbWidth);
            }
        }

        private void setPos(double pX, double pY, double pZ) {
            this.x = pX;
            this.y = pY;
            this.z = pZ;
            bb = new AxisAlignedBB(
                    pX - bbWidth / 2, pY,            pZ - bbWidth / 2, 
                    pX + bbWidth / 2, pY + bbHeight, pZ + bbWidth / 2);
        }

        private void move(double pX, double pY, double pZ) {
            if (pX != 0.0 || pY != 0.0 || pZ != 0.0) {
                bb = bb.move(pX, pY, pZ);
                setLocationFromBoundingbox();
            }
        }

        private void setLocationFromBoundingbox() {
            this.x = (bb.minX + bb.maxX) / 2.0;
            this.y = bb.minY;
            this.z = (bb.minZ + bb.maxZ) / 2.0;
        }
        
    }
}
