package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler.BendablePart;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class EnergyRippleLayer<T extends LivingEntity, M extends BipedModel<T>> extends LayerRenderer<T, M> {
    private static final Random RANDOM = new Random();

    public EnergyRippleLayer(IEntityRenderer<T, M> renderer) {
        super(renderer);
    }
    

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, 
            T entity, float walkAnimPos, float walkAnimSpeed, float partialTick, 
            float ticks, float headYRotation, float headXRotation) {
        HamonEnergyRippleHandler sparksHandler = getSparksHandler(entity);
        if (sparksHandler == null) return;
        
        M model = getParentModel();
        sparksHandler.updateSparks(ticks, model);
//        sparksHandler.addHandSparks(HandSide.LEFT, ticks, model, 4, ModParticles.HAMON_SPARK.get(), entity);
        
        Minecraft mc = Minecraft.getInstance();
        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE2);
        RenderSystem.enableTexture();
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        IParticleRenderType particleRenderType = IParticleRenderType.PARTICLE_SHEET_OPAQUE;
        particleRenderType.begin(bufferBuilder, mc.textureManager);

        Vector3f[] avector3f = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F), 
                new Vector3f(-1.0F, 1.0F, 0.0F), 
                new Vector3f(1.0F, 1.0F, 0.0F), 
                new Vector3f(1.0F, -1.0F, 0.0F)};
        float yBodyRot = MathHelper.lerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        
        for (int i = 0; i < 4; ++i) {
           Vector3f vector3f = avector3f[i];
           vector3f.add(-0.125F, 0, -0.125F);
           vector3f.transform(Vector3f.XP.rotationDegrees(-camera.getXRot()));
           vector3f.transform(Vector3f.YP.rotationDegrees(180 + camera.getYRot() - yBodyRot));
        }
        
        sparksHandler.render(matrixStack, bufferBuilder, avector3f, model);
        sparksHandler.afterFrameRender(ticks);
        
        particleRenderType.end(tessellator);
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
        RenderSystem.disableBlend();
        RenderSystem.defaultAlphaFunc();
    }

    private HamonEnergyRippleHandler getSparksHandler(T entity) {
        return entity.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).map(cap -> cap.getHamonSparkWaves()).orElse(null);
    }
    
    
    
    public static class HamonEnergyRippleHandler {
        private final LivingEntity entity;
        private float ticksPrev = Float.MAX_VALUE;
        private final Collection<SparkWave> sparkWaves = new LinkedList<>();
        private final Collection<SparkPseudoParticle> sparks = new LinkedList<>();
        
        public HamonEnergyRippleHandler(LivingEntity entity) {
            this.entity = entity;
        }
        
        @Nullable
        private ParticleType<?> sparkForCurrentWave() {
            if (GeneralUtil.orElseFalse(INonStandPower.getNonStandPowerOptional(entity), 
                    power -> power.getHeldAction() == ModHamonActions.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE.get()) || 
                GeneralUtil.orElseFalse(entity.getCapability(PlayerUtilCapProvider.CAPABILITY), 
                    cap -> cap.getContinuousActionIfItIs(ModHamonActions.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE.get()).isPresent())) {
                return ModParticles.HAMON_SPARK_YELLOW.get();
            }
            return null;
        }

        public void updateSparks(float time, BipedModel<?> model) {
            if (Minecraft.getInstance().isPaused()) return;
            float delta = time - ticksPrev;
            if (delta < 0) return;
            
            Iterator<SparkWave> wIt = sparkWaves.iterator();
            while (wIt.hasNext()) {
                SparkWave wave = wIt.next();
                if (wave.addParticles(delta, model, entity, sparks)) {
                    wIt.remove();
                }
            }
            
            ParticleType<?> particle = sparkForCurrentWave();
            if (particle != null) {
                int t1 = (int) (time / WAVES_GAP);
                int t2 = (int) ((time - delta) / WAVES_GAP);
                int waves = t1 - t2;
                for (int i = 0; i < waves; i++) {
                    sparkWaves.add(new SparkWave(time - WAVES_GAP * (t2 + i + 1), particle));
                }
            }
        }
        
        public void addHandSparks(HandSide hand, float time, BipedModel<?> model, 
                float intensity, ParticleType<?> particleType, LivingEntity hamonUser) {
            if (Minecraft.getInstance().isPaused()) return;
            float delta = time - ticksPrev;
            if (delta < 0) return;
            int particles = MathUtil.fractionRandomInc(intensity * delta);
            int controlLevel = INonStandPower.getNonStandPowerOptional(entity).resolve()
                    .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()))
                    .map(hamon -> hamon.getHamonControlLevel())
                    .orElse(0);
            for (int i = 0; i < particles; i++) {
                Vector3d offset = new Vector3d(
                        (RANDOM.nextDouble() - 0.5) * 0.4,
                        RANDOM.nextDouble() * (0.5 - 0.4 * controlLevel / HamonData.MAX_STAT_LEVEL) - 0.65,
                        (RANDOM.nextDouble() - 0.5) * 0.4);
                sparks.add(SparkPseudoParticle.armSpark(model, hand == HandSide.RIGHT, particleType, offset));
            }
        }
        
        public void afterFrameRender(float time) {
            float delta = time - ticksPrev;
            ticksPrev = time;
            
            Iterator<SparkPseudoParticle> spIt = sparks.iterator();
            while (spIt.hasNext()) {
                SparkPseudoParticle spark = spIt.next();
                if (spark.addTimeDelta(delta)) {
                    spIt.remove();
                }
            }
        }

        public void render(MatrixStack matrixStack, BufferBuilder bufferBuilder, Vector3f[] avector3f, BipedModel<?> model) {
            sparks.forEach(spark -> spark.render(matrixStack, bufferBuilder, avector3f, model));
        }
        
        

        private static final float SPARK_LIFE_SPAN = 2F;
        private static final float WAVES_GAP = 7.5F;
        private static final float WAVE_DURATION = 20F;
        private static final float SPARKS_PER_TICK = 15F;
        private static class SparkWave {
            private final ParticleType<?> particleType;
            private float progress;
            
            private SparkWave(float startingDelta, ParticleType<?> particleType) {
                this.progress = startingDelta / WAVE_DURATION;
                this.particleType = particleType;
            }
            
            private boolean addParticles(float timeDelta, BipedModel<?> model, LivingEntity entity, Collection<SparkPseudoParticle> sparksList) {
                double sparkCount = timeDelta * SPARKS_PER_TICK;
                if (0.5F < progress) {
                    sparkCount *= (1 + (progress - 0.5F) / (1 - 0.5F)) * 3;
                }
                int sparkCountInt = MathUtil.fractionRandomInc(sparkCount);
                for (int i = 0; i < sparkCountInt; i++) {
                    if (progress <= 0.25F) {
                        double y = (progress / 0.25F - 1) * 0.75;
                        sparksList.add(SparkPseudoParticle.legSpark(model, RANDOM.nextBoolean(), 
                                particleType, randomSideOffset(0.25, y, 0.25)));
                    }
                    else if (/*0.25F < */ progress <= 0.5F) {
                        double y = ((progress - 0.25F) / (0.5F - 0.25F) - 1) * 0.75;
                        sparksList.add(SparkPseudoParticle.torsoSpark(model, 
                                particleType, randomSideOffset(0.5, y, 0.25)));
                        sparksList.add(SparkPseudoParticle.armSpark(model, entity.getMainArm() == HandSide.LEFT, 
                                particleType, randomSideOffset(0.25, y + 0.15, 0.25)));
                        if (0.3333F <= progress) {
                            sparksList.add(SparkPseudoParticle.headSpark(model, 
                                    particleType, randomSideOffset(0.5, y, 0.5)));
                        }
                    }
                    else /*if (0.5F < progress)*/ {
                        double r = (progress - 0.5F) / (1 - 0.5F);
                        double y = r * -0.75 + 0.15 + (RANDOM.nextDouble() - 0.5) * 0.375 * r;
                        y = Math.max(y, -0.625);

                        sparksList.add(SparkPseudoParticle.armSpark(model, entity.getMainArm() == HandSide.RIGHT, 
                                particleType, randomSideOffset(0.25, y, 0.25)));
                    }
                }
                return (progress += timeDelta / WAVE_DURATION) >= 1;
            }
        }
        
        private static Vector3d randomSideOffset(double widthX, double y, double widthZ) {
            int side = RANDOM.nextDouble() * (widthX + widthZ) < widthX ? 0 : 1;
            if (RANDOM.nextBoolean()) side += 2;
            widthX += 0.05;
            widthZ += 0.05;
            switch (side) {
            case 0:
                return new Vector3d((RANDOM.nextDouble() - 0.5) * widthX, y, widthZ / 2);
            case 1:
                return new Vector3d(widthX / 2, y, (RANDOM.nextDouble() - 0.5) * widthZ);
            case 2:
                return new Vector3d((RANDOM.nextDouble() - 0.5) * widthX, y, -widthZ / 2);
            case 3:
                return new Vector3d(-widthX / 2, y, (RANDOM.nextDouble() - 0.5) * widthZ);
            default: // that's simply not possible
                return null;
            }
        }
        
        // FIXME wrong for legs
        private static Vector3d bendOffset(Vector3d pos, BipedModel<?> model, BendablePart bendablePart, double bendYPoint) {
            if (bendablePart == BendablePart.LEFT_LEG || bendablePart == BendablePart.RIGHT_LEG) {
                pos = bendOffset(pos, model, BendablePart.TORSO, bendYPoint + 0.75);
            }
            
            if (pos.y < bendYPoint) {
                float[] bend = PlayerAnimationHandler.getPlayerAnimator().getBend(model, bendablePart);
                if (bend[0] != 0) {
                    switch (bendablePart) {
                    case TORSO:
                        pos = pos.add(0, -bendYPoint, 0).xRot(bend[0]).add(0, bendYPoint, 0);
                        break;
                    case LEFT_ARM:
                    case RIGHT_ARM:
                        pos = pos.add(0, -bendYPoint, 0).xRot(-bend[0]).add(0, bendYPoint, 0);
                        break;
                    case LEFT_LEG:
                    case RIGHT_LEG:
                        pos = pos.add(0, -bendYPoint, 0).xRot(-bend[0]).add(0, bendYPoint, 0);
                        break;
                    default:
                        break;
                    }
                }
            }
            return pos;
        }
        
        
        
        private static class SparkPseudoParticle {
            private final TextureAtlasSprite hamonSparkSprite;
            private float age;
            private final float lifeSpan = SPARK_LIFE_SPAN;
            private final ModelRenderer modelPart;
            private final double x;
            private final double y;
            private final double z;
            private final float scale;
            
            private SparkPseudoParticle(ParticleType<?> particleType, ModelRenderer modelPart, Vector3d pos) {
                this.hamonSparkSprite = CustomParticlesHelper.getSavedSpriteSet(particleType).get(RANDOM);
                this.modelPart = modelPart;
                this.x = pos.x;
                this.y = pos.y;
                this.z = pos.z;
                this.scale = 0.04F + RANDOM.nextFloat() * 0.02F;
            }
            
            static SparkPseudoParticle legSpark(BipedModel<?> model, boolean right, ParticleType<?> particleType, Vector3d offset) {
                ModelRenderer modelPart = right ? model.rightLeg : model.leftLeg;
                offset = bendOffset(offset, model, 
                        right ? BendablePart.RIGHT_LEG : BendablePart.LEFT_LEG, -0.375);
                return new SparkPseudoParticle(particleType, modelPart, offset);
            }

            static SparkPseudoParticle armSpark(BipedModel<?> model, boolean right, ParticleType<?> particleType, Vector3d offset) {
                ModelRenderer modelPart = right ? model.rightArm : model.leftArm;
                double xPivot = right ? -0.0625 : 0.0625;
                offset = bendOffset(offset, model, 
                        right ? BendablePart.RIGHT_ARM : BendablePart.LEFT_ARM, -0.225);
                return new SparkPseudoParticle(particleType, modelPart, offset.add(xPivot, 0, 0));
            }
            
            static SparkPseudoParticle torsoSpark(BipedModel<?> model, ParticleType<?> particleType, Vector3d offset) {
                offset = bendOffset(offset, model, 
                        BendablePart.TORSO, -0.375);
                return new SparkPseudoParticle(particleType, model.body, offset);
            }
            
            static SparkPseudoParticle headSpark(BipedModel<?> model, ParticleType<?> particleType, Vector3d offset) {
                return new SparkPseudoParticle(particleType, model.head, offset);
            }
            
            private boolean addTimeDelta(float delta) {
                return (age += delta) >= lifeSpan;
            }
            
            public void render(MatrixStack matrixStack, IVertexBuilder bufferBuilder, Vector3f[] avector3f, BipedModel<?> model) {
                float u0 = hamonSparkSprite.getU0();
                float u1 = hamonSparkSprite.getU1();
                float v0 = hamonSparkSprite.getV0();
                float v1 = hamonSparkSprite.getV1();

                matrixStack.pushPose();

                translateTo(matrixStack, modelPart, x, y, z);
                matrixStack.scale(scale, scale, scale);

                Matrix4f matrix4f = matrixStack.last().pose();

                bufferBuilder.vertex(matrix4f, avector3f[0].x(), avector3f[0].y(), avector3f[0].z())
                .uv(u1, v1).color(255, 255, 255, 255).uv2(0xF000F0).endVertex();
                bufferBuilder.vertex(matrix4f, avector3f[1].x(), avector3f[1].y(), avector3f[1].z())
                .uv(u1, v0).color(255, 255, 255, 255).uv2(0xF000F0).endVertex();
                bufferBuilder.vertex(matrix4f, avector3f[2].x(), avector3f[2].y(), avector3f[2].z())
                .uv(u0, v0).color(255, 255, 255, 255).uv2(0xF000F0).endVertex();
                bufferBuilder.vertex(matrix4f, avector3f[3].x(), avector3f[3].y(), avector3f[3].z())
                .uv(u0, v1).color(255, 255, 255, 255).uv2(0xF000F0).endVertex();

                matrixStack.popPose();
            }

            private void translateTo(MatrixStack matrixStack, @Nullable ModelRenderer modelRenderer, double x, double y, double z) {
                if (modelRenderer != null) {
                    modelRenderer.translateAndRotate(matrixStack);
                }

                matrixStack.translate(x, -y, -z);

                if (modelRenderer != null) {
                    if (modelRenderer.xRot != 0.0F) {
                        matrixStack.mulPose(Vector3f.XP.rotation(-modelRenderer.xRot));
                    }
                    if (modelRenderer.yRot != 0.0F) {
                        matrixStack.mulPose(Vector3f.YP.rotation(-modelRenderer.yRot));
                    }
                    if (modelRenderer.zRot != 0.0F) {
                        matrixStack.mulPose(Vector3f.ZP.rotation(-modelRenderer.zRot));
                    }
                }
            }
        }
    }
    
    
    public static Vector3d handTipPos(BipedModel<?> posedModel, HandSide hand, Vector3d offset, float yBodyRot) {
        double scale = 0.9375;
        boolean right = hand == HandSide.RIGHT;
        offset = offset.add(0, -0.65, 0);
        
        ModelRenderer modelPart = right ? posedModel.rightArm : posedModel.leftArm;
        double xPivot = right ? -0.0625 : 0.0625;
        offset = HamonEnergyRippleHandler.bendOffset(offset, posedModel, 
                right ? BendablePart.RIGHT_ARM : BendablePart.LEFT_ARM, -0.225);
        offset = offset.add(xPivot, 0, 0);
        
        
        Vector3d pos = new Vector3d(modelPart.x / 16, 1.5 - modelPart.y / 16, modelPart.z / 16);
        if (modelPart.zRot != 0.0F) {
            pos = pos.zRot(-modelPart.zRot);
        }
        if (modelPart.yRot != 0.0F) {
            pos = pos.yRot(modelPart.yRot);
        }
        if (modelPart.xRot != 0.0F) {
            pos = pos.xRot(modelPart.xRot);
        }
        
        pos = pos.add(offset.x, offset.y, offset.z);
        if (modelPart.xRot != 0.0F) {
            pos = pos.xRot(-modelPart.xRot);
        }
        if (modelPart.yRot != 0.0F) {
            pos = pos.yRot(-modelPart.yRot);
        }
        if (modelPart.zRot != 0.0F) {
            pos = pos.zRot(modelPart.zRot);
        }
        
        pos = pos.yRot(-yBodyRot * MathUtil.DEG_TO_RAD);
        pos = pos.scale(scale);
        
        return pos;
    }
}
