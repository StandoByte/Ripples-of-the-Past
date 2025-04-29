package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.Random;

import com.github.standobyte.jojo.action.stand.GoldExperienceCreateLifeform;
import com.github.standobyte.jojo.client.render.entity.util.ModelCubeWeightedList;
import com.github.standobyte.jojo.entity.itemprojectile.KnifeEntity;
import com.github.standobyte.jojo.mixin.Matrix4fAccessor;
import com.github.standobyte.jojo.mixin.client.LivingRendererInvoker;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.MathHelper;

public class MobStuckArrowLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
    private final LivingRenderer<T, M> renderer;
    private ModelCubeWeightedList modelCubes;
    private final EntityRendererManager dispatcher;
    private Entity arrow;
    private boolean slime;
    
    public MobStuckArrowLayer(LivingRenderer<T, M> renderer) {
        super(renderer);
        this.renderer = renderer;
        this.dispatcher = renderer.getDispatcher();
    }
    
    @Override
    public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, T pLivingEntity, 
            float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        boolean init = true;
        float[] scaleBack = DEFAULT_SCALE;
        for (Type projectileType : Type.values()) {
            int num = numStuck(projectileType, pLivingEntity);
            if (num > 0) {
                if (init) {
                    lazyInitLayer(pLivingEntity);
                    if (!modelCubes.cacheVisibleCubes()) {
                        return;
                    }
                    scaleBack = scaleBackEntity(pLivingEntity, pPartialTicks);
                    init = false;
                }
                
                Random random = new Random((long) pLivingEntity.getId() + projectileType.ordinal());
                for (int i = 0; i < num; ++i) {
                    ModelCubeWeightedList.ModelCube modelCube = modelCubes.getRandomCube(random);
                    pMatrixStack.pushPose();
                    modelCube.translateAndRotate(pMatrixStack);
                    ModelRenderer.ModelBox modelBox = modelCube.cube();
                    float minX = modelBox.minX;
                    float maxX = modelBox.maxX;
                    float minY = modelBox.minY;
                    float maxY = modelBox.maxY;
                    float minZ = modelBox.minZ;
                    float maxZ = modelBox.maxZ;
                    // TODO fix with AgeableModel
                    
                    float f = 0;
                    float f1 = 0;
                    float f2 = 0;
                    if (slime) {
                        f  = random.nextFloat() * 0.5f + 0.25f;
                        f1 = random.nextFloat() * 0.5f + 0.25f;
                        f2 = random.nextFloat() * 0.5f + 0.25f;
                    }
                    else switch (random.nextInt(6)) {
                    case 0: f = 0;                  f1 = random.nextFloat(); f2 = random.nextFloat(); break;
                    case 1: f = 1;                  f1 = random.nextFloat(); f2 = random.nextFloat(); break;
                    case 2: f = random.nextFloat(); f1 = 0;                  f2 = random.nextFloat(); break;
                    case 3: f = random.nextFloat(); f1 = 1;                  f2 = random.nextFloat(); break;
                    case 4: f = random.nextFloat(); f1 = random.nextFloat(); f2 = 0;                  break;
                    case 5: f = random.nextFloat(); f1 = random.nextFloat(); f2 = 1;                  break;
                    }
                    
                    float f3 = MathHelper.lerp(f,  minX, maxX) / 16.0F;
                    float f4 = MathHelper.lerp(f1, minY, maxY) / 16.0F;
                    float f5 = MathHelper.lerp(f2, minZ, maxZ) / 16.0F;
                    pMatrixStack.translate((double)f3, (double)f4, (double)f5);
                    f = -1.0F * (f * 2.0F - 1.0F);
                    f1 = -1.0F * (f1 * 2.0F - 1.0F);
                    f2 = -1.0F * (f2 * 2.0F - 1.0F);
                    pMatrixStack.scale(scaleBack[0], scaleBack[1], scaleBack[2]);
                    this.renderStuckItem(projectileType, pMatrixStack, pBuffer, pPackedLight, pLivingEntity, f, f1, f2, pPartialTicks, random);
                    pMatrixStack.popPose();
                }
            }
        }
    }
    
    protected int numStuck(Type projectileType, T entity) {
        switch (projectileType) {
        case ARROW:
            return GoldExperienceCreateLifeform.getStuckArrows(entity);
        case KNIFE:
            return GoldExperienceCreateLifeform.getStuckKnives(entity);
        default:
            throw new AssertionError();
        }
    }
    
    protected void renderStuckItem(Type projectileType, MatrixStack matrixStack, IRenderTypeBuffer buffer, 
            int packedLight, T entity, float x, float y, float z, float partialTick, Random random) {
        matrixStack.pushPose();
        float f = MathHelper.sqrt(x * x + z * z);
        switch (projectileType) {
        case ARROW:
            arrow = new ArrowEntity(entity.level, entity.getX(), entity.getY(), entity.getZ());
            break;
        case KNIFE:
            arrow = new KnifeEntity(entity.level, entity.getX(), entity.getY(), entity.getZ());
            break;
        }

        arrow.yRot = (float)(Math.atan2((double)x, (double)z) * (double)(180F / (float)Math.PI));
        arrow.xRot = (float)(Math.atan2((double)y, (double)f) * (double)(180F / (float)Math.PI));
        arrow.yRotO = arrow.yRot;
        arrow.xRotO = arrow.xRot;
        dispatcher.render(arrow, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, matrixStack, buffer, packedLight);
        matrixStack.popPose();
    }
    
    private static final float PLAYER_SCALE = 0.9375F;
    private static final float[] DEFAULT_SCALE = { PLAYER_SCALE, PLAYER_SCALE, PLAYER_SCALE };
    private float[] scaleBackEntity(T entity, float partialTick) {
        MatrixStack matrixStack = new MatrixStack();
        ((LivingRendererInvoker<T, M>) renderer).invokeScale(entity, matrixStack, partialTick);
        Matrix4fAccessor scaled = (Matrix4fAccessor) (Object) matrixStack.last().pose();
        float scaleX = MathUtil.getM(scaled, 0, 0);
        float scaleY = MathUtil.getM(scaled, 1, 1);
        float scaleZ = MathUtil.getM(scaled, 2, 2);
        return new float[] { 1 / scaleX * PLAYER_SCALE, 1 / scaleY * PLAYER_SCALE, 1 / scaleZ * PLAYER_SCALE };
    }
    
    private enum Type {
        ARROW,
        KNIFE
    }
    
    private void lazyInitLayer(T entityExample) {
        if (modelCubes == null) {
            M model = getParentModel();
            if (model != null) {
                this.modelCubes = ModelCubeWeightedList.fromModel(model);
                this.slime = entityExample instanceof SlimeEntity;
            }
            else {
                this.modelCubes = ModelCubeWeightedList.empty();
            }
        }
    }

}
