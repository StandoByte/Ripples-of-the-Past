package com.github.standobyte.jojo.client.particle.custom;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;

import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.item.AjaStoneItem;
import com.github.standobyte.jojo.item.OilItem;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.TridentItem;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.HandSide;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class FirstPersonHamonAura {
    private final Queue<FirstPersonPseudoParticle> particlesToAdd = Queues.newArrayDeque();
    private final Map<IParticleRenderType, Map<HandSide, Queue<FirstPersonPseudoParticle>>> particles = Maps.newIdentityHashMap();
    
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
        for (Map<HandSide, Queue<FirstPersonPseudoParticle>> particles : this.particles.values()) {
            tickParticles(particles.get(HandSide.LEFT));
            tickParticles(particles.get(HandSide.RIGHT));
        }
        
        FirstPersonPseudoParticle particle;
        if (!particlesToAdd.isEmpty()) {
            while((particle = particlesToAdd.poll()) != null) {
                Map<HandSide, Queue<FirstPersonPseudoParticle>> particlesByRenderType = this.particles.computeIfAbsent(particle.getRenderType(), 
                        t -> Util.make(new EnumMap<>(HandSide.class), map -> {
                            map.put(HandSide.LEFT, EvictingQueue.create(16384));
                            map.put(HandSide.RIGHT, EvictingQueue.create(16384));
                        }));
                particlesByRenderType.get(particle.handSide).add(particle);
            }
        }

    }
    
    private void tickParticles(Queue<FirstPersonPseudoParticle> particles) {
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
                    crashreportcategory.setDetail("Particle Type", particle.getRenderType()::toString);
                    throw new ReportedException(crashreport);
                }
                if (!particle.isAlive()) {
                    iterator.remove();
                }
            }
        }
    }
    
    public static boolean auraRendersAtItem(ItemStack itemStack, HandSide handSide) {
        if (MCUtil.itemHandFree(itemStack)) {
            return true;
        }
        
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) cameraEntity;
            Optional<HamonData> hamonOptional = INonStandPower.getNonStandPowerOptional(entity).resolve()
                    .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
            return hamonOptional.map(hamon -> {
                Item item = itemStack.getItem();
                return entity.getMainArm() == handSide && (hamon.isSkillLearned(ModHamonSkills.METAL_SILVER_OVERDRIVE.get()) || OilItem.remainingOiledUses(itemStack).isPresent()) && MCUtil.isItemWeapon(itemStack)
                        || hamon.isSkillLearned(ModHamonSkills.PLANT_ITEM_INFUSION.get()) && HamonUtil.isItemLivingMatter(itemStack)
                        || hamon.isSkillLearned(ModHamonSkills.THROWABLES_INFUSION.get()) && (item == Items.EGG || item == Items.SNOWBALL || item == ModItems.MOLOTOV.get() || ((item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) && PotionUtils.getPotion(itemStack) == Potions.WATER))
                        || hamon.isSkillLearned(ModHamonSkills.ARROW_INFUSION.get()) && (item instanceof ShootableItem || item instanceof TridentItem || item == ModItems.KNIFE.get() || item == ModItems.BLADE_HAT.get())
                        || hamon.isSkillLearned(ModHamonSkills.CLACKER_VOLLEY.get()) && item == ModItems.CLACKERS.get()
                        || hamon.isSkillLearned(ModHamonSkills.AJA_STONE_KEEPER.get()) && item instanceof AjaStoneItem
                        || hamon.isSkillLearned(ModHamonSkills.SATIPOROJA_SCARF.get()) && item == ModItems.SATIPOROJA_SCARF.get()
                        || Streams.stream(hamon.getLearnedSkills())
                            .flatMap(AbstractHamonSkill::getRewardActions)
                            .anyMatch(action -> action.renderHamonAuraOnItem(itemStack, handSide));
            }).orElse(false);
        }
        
        return false;
    }
    
    public static void itemMatrixTransform(MatrixStack matrixStack, HandSide handSide, ItemStack itemStack) {
        boolean flag = handSide != HandSide.LEFT;
        float f = flag ? 1.0F : -1.0F;
        matrixStack.translate(f * 0.64000005, -0.6, -0.71999997);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(f * 45.0F));
        matrixStack.translate(-f, 3.6, 3.5);
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * 120.0F));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(200.0F));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(f * -135.0F));
        matrixStack.translate(f * 5.3, 0, 0);
    }


    @SuppressWarnings("deprecation")
    public void renderParticles(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, HandSide handSide) {
        Minecraft mc = Minecraft.getInstance();
        LightTexture lightTexture = mc.gameRenderer.lightTexture();
        float partialTicks = ClientUtil.getPartialTick();
        
        lightTexture.turnOnLightLayer();
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

        enable.run();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        
        for (Map.Entry<IParticleRenderType, Map<HandSide, Queue<FirstPersonPseudoParticle>>> particlesByRenderType : particles.entrySet()) {
            IParticleRenderType renderType = particlesByRenderType.getKey();
            if (renderType == IParticleRenderType.NO_RENDER || 
                    renderType == HamonAuraParticleRenderType.HAMON_AURA && !ClientModSettings.getSettingsReadOnly().firstPersonHamonAura) {
                continue;
            }
            Queue<FirstPersonPseudoParticle> particles = particlesByRenderType.getValue().get(handSide);
            if (particles == null || particles.isEmpty() || 
                    renderType == HamonAuraParticleRenderType.HAMON_AURA && !auraRendersAtItem(mc.player.getItemInHand(MCUtil.getHand(mc.player, handSide)), handSide)) {
                continue;
            }
            renderType.begin(bufferbuilder, Minecraft.getInstance().textureManager);
            
            for (FirstPersonPseudoParticle particle : particles) {
                try {
                    particle.render(bufferbuilder, partialTicks);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                    CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                    crashreportcategory.setDetail("Particle", particle::toString);
                    crashreportcategory.setDetail("Particle Type", renderType::toString);
                    throw new ReportedException(crashreport);
                }
            }
            
            renderType.end(tessellator);
        }

        RenderSystem.popMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
        RenderSystem.disableBlend();
        RenderSystem.defaultAlphaFunc();
        lightTexture.turnOffLightLayer();
        RenderSystem.disableFog();
    }
    
    
    
    public static abstract class FirstPersonPseudoParticle {
        protected static final Random RANDOM = new Random();
        protected double xo;
        protected double yo;
        protected double zo;
        protected double x;
        protected double y;
        protected double z;
        protected double xd;
        protected double yd;
        protected double zd;
        protected boolean removed;
        protected int age;
        protected int lifetime = (int)(4.0F / (RANDOM.nextFloat() * 0.9F + 0.1F));
        protected float rCol = 1;
        protected float gCol = 1;
        protected float bCol = 1;
        protected float alpha = 1;
        protected float quadSize = 0.1F * (RANDOM.nextFloat() * 0.5F + 0.5F) * 2.0F;
        protected TextureAtlasSprite sprite;
        protected final IAnimatedSprite sprites;

        protected Quaternion renderRot = new Quaternion(Quaternion.ONE);
        protected final HandSide handSide;
        protected float yRot;
        protected float xRot;
        
        protected static final float RIGHT_Y_ROT = 57.5f * MathUtil.DEG_TO_RAD;
        protected static final float RIGHT_X_ROT = -62.5f * MathUtil.DEG_TO_RAD;
        protected static final float LEFT_Y_ROT = -RIGHT_Y_ROT;
        protected static final float LEFT_X_ROT = RIGHT_X_ROT;
        protected static final Quaternion RIGHT_ROT;
        protected static final Quaternion LEFT_ROT;
        static {
            RIGHT_ROT = new Quaternion(Quaternion.ONE);
            RIGHT_ROT.mul(Vector3f.YP.rotation(RIGHT_Y_ROT));
            RIGHT_ROT.mul(Vector3f.XP.rotation(RIGHT_X_ROT));
            LEFT_ROT = new Quaternion(Quaternion.ONE);
            LEFT_ROT.mul(Vector3f.YP.rotation(LEFT_Y_ROT));
            LEFT_ROT.mul(Vector3f.XP.rotation(LEFT_X_ROT));
        }
        
        public FirstPersonPseudoParticle(double x, double y, double z, 
                IAnimatedSprite sprites, HandSide handSide) {
            this.setPos(x, y, z);
            this.xo = x;
            this.yo = y;
            this.zo = z;
            this.sprites = sprites;
            
            this.handSide = handSide;
            switch (handSide) {
            case LEFT:
                yRot = LEFT_Y_ROT;
                xRot = LEFT_X_ROT;
                renderRot = LEFT_ROT;
                break;
            case RIGHT:
                yRot = RIGHT_Y_ROT;
                xRot = RIGHT_X_ROT;
                renderRot = RIGHT_ROT;
                break;
            }
        }
        
        public abstract IParticleRenderType getRenderType();
        
        protected void remove() {
            this.removed = true;
        }

        public boolean isAlive() {
            return !this.removed;
        }

        protected void setPos(double pX, double pY, double pZ) {
            this.x = pX;
            this.y = pY;
            this.z = pZ;
        }

        protected void move(double pX, double pY, double pZ) {
            if (pX != 0.0 || pY != 0.0 || pZ != 0.0) {
                Vector3d moveVec = new Vector3d(pX, pY, pZ);
                moveVec = moveVec.xRot(-xRot);
                moveVec = moveVec.yRot(yRot);
                this.x += moveVec.x;
                this.y += moveVec.y;
                this.z += moveVec.z;
            }
        }
        
        public void render(IVertexBuilder buffer, float partialTick) {
            float f = (float)(MathHelper.lerp((double)partialTick, this.xo, this.x));
            float f1 = (float)(MathHelper.lerp((double)partialTick, this.yo, this.y));
            float f2 = (float)(MathHelper.lerp((double)partialTick, this.zo, this.z));
            Quaternion quaternion = renderRot;

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

        protected float getQuadSize(float pScaleFactor) {
            return quadSize;
        }
        
        protected int getLightColor(float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            return mc.getEntityRenderDispatcher().getPackedLightCoords(mc.player, partialTick);
        }
        
        public void tick() {
            xo = x;
            yo = y;
            zo = z;
            if (age++ >= lifetime) {
                remove();
            }
        }
        
        
        @Override
        public String toString() {
            return this.getClass().getSimpleName() + ", Pos (" + this.x + "," + this.y + "," + this.z + "), RGBA (" + this.rCol + "," + this.gCol + "," + this.bCol + "," + this.alpha + "), Age " + this.age;
        }
    }
    
    
    
    public static class HamonAuraPseudoParticle extends FirstPersonPseudoParticle {
        protected final double fallSpeed;
        protected final int startingSpriteRandom;
        
        public HamonAuraPseudoParticle(double x, double y, double z, 
                IAnimatedSprite sprites, HandSide handSide) {
            super(x, y, z, sprites, handSide);
            
            this.lifetime = (int)(4.0F / (RANDOM.nextFloat() * 0.9F + 0.1F));

            this.xd = (Math.random() * 2.0 - 1.0) * 0.4;
            this.yd = (Math.random() * 2.0 - 1.0) * 0.4;
            this.zd = (Math.random() * 2.0 - 1.0) * 0.4;
            double f = (Math.random() + Math.random() + 1.0) * 0.15;
            double f1 = MathHelper.sqrt(xd * xd + yd * yd + zd * zd);
            this.xd = xd / f1 * f * 0.4;
            this.yd = yd / f1 * f * 0.4 + 0.1;
            this.zd = zd / f1 * f * 0.4;

            this.fallSpeed = 0.0005;
            this.xd *= 0.05;
            this.yd *= 0.1;
            this.zd *= 0.05;
            float f3 = 1.2F + 0.6F * RANDOM.nextFloat();
            this.quadSize *= 0.75F * f3;
//            this.lifetime = (int)(8 / (RANDOM.nextDouble() * 0.8 + 0.2));
//            this.lifetime = (int)((float) lifetime * f3);
//            this.lifetime = Math.max(lifetime, 1);

            lifetime = 25 + RANDOM.nextInt(10);
            startingSpriteRandom = RANDOM.nextInt(lifetime);
            setSpriteFromAge(sprites);
            alpha = 0.25F;
        }
        
        @Override
        public IParticleRenderType getRenderType() {
            return HamonAuraParticleRenderType.HAMON_AURA;
        }
        
        @Override
        protected int getLightColor(float partialTick) {
            return 0xF000F0;
        }
        
        @Override
        public void tick() {
            super.tick();
            if (!removed) {
                setSpriteFromAge(sprites);
                yd += fallSpeed;
                move(xd, yd, zd);
                if (y == yo) {
                    xd *= 1.1D;
                    zd *= 1.1D;
                }

                xd *= 0.96;
                yd *= 0.96;
                zd *= 0.96;
            }
        }

        protected static final float ALPHA_MIN = 0.05F;
        protected static final float ALPHA_DIFF = 0.3F;
        @Override
        public void render(IVertexBuilder buffer, float partialTick) {
            float ageF = ((float) age + partialTick) / (float) lifetime;
            float alphaFunc = ageF <= 0.5F ? ageF * 2 : (1 - ageF) * 2;
            this.alpha = ALPHA_MIN + alphaFunc * ALPHA_DIFF;
            super.render(buffer, partialTick);
        }

        protected void setSpriteFromAge(IAnimatedSprite pSprite) {
            setSprite(pSprite.get((age + startingSpriteRandom) % lifetime, lifetime));
        }

        protected void setSprite(TextureAtlasSprite pSprite) {
            this.sprite = pSprite;
        }
        
        @Override
        protected float getQuadSize(float pScaleFactor) {
            return quadSize * MathHelper.clamp(((float)age + pScaleFactor) / (float)lifetime * 32.0F, 0.0F, 1.0F);
        }
        
    }
}
