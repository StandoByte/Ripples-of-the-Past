package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.entity.itemprojectile.KnifeEntity;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.MathHelper;

public class MobStuckArrowLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
    private List<ModelRenderer> modelParts;
    private final EntityRendererManager dispatcher;
    private ArrowEntity arrow;
    private KnifeEntity knife;
    
    public MobStuckArrowLayer(LivingRenderer<T, M> renderer) {
        super(renderer);
        this.dispatcher = renderer.getDispatcher();
    }
    
    @Override
    public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, T pLivingEntity, 
            float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        for (Type projectileType : Type.values()) {
            int num = numStuck(projectileType, pLivingEntity);
            if (num > 0) {
                if (modelParts == null) {
                    lazyInitModelParts();
                }
                if (modelParts.isEmpty()) return;
                
                Random random = new Random((long) pLivingEntity.getId());
                for (int i = 0; i < num; ++i) {
                    pMatrixStack.pushPose();
                    ModelRenderer modelrenderer = modelParts.get(random.nextInt(this.modelParts.size()));
                    ModelRenderer.ModelBox modelrenderer$modelbox = modelrenderer.getRandomCube(random);
                    modelrenderer.translateAndRotate(pMatrixStack);
                    float f = random.nextFloat();
                    float f1 = random.nextFloat();
                    float f2 = random.nextFloat();
                    float f3 = MathHelper.lerp(f, modelrenderer$modelbox.minX, modelrenderer$modelbox.maxX) / 16.0F;
                    float f4 = MathHelper.lerp(f1, modelrenderer$modelbox.minY, modelrenderer$modelbox.maxY) / 16.0F;
                    float f5 = MathHelper.lerp(f2, modelrenderer$modelbox.minZ, modelrenderer$modelbox.maxZ) / 16.0F;
                    pMatrixStack.translate((double)f3, (double)f4, (double)f5);
                    f = -1.0F * (f * 2.0F - 1.0F);
                    f1 = -1.0F * (f1 * 2.0F - 1.0F);
                    f2 = -1.0F * (f2 * 2.0F - 1.0F);
                    this.renderStuckItem(projectileType, pMatrixStack, pBuffer, pPackedLight, pLivingEntity, f, f1, f2, pPartialTicks);
                    pMatrixStack.popPose();
                }
            }
        }
    }
    
    protected int numStuck(Type projectileType, T entity) {
        switch (projectileType) {
        case ARROW:
            return entity.getArrowCount();
        case KNIFE:
            return entity.getCapability(LivingUtilCapProvider.CAPABILITY).map(
                    data -> data.getStuckObjects().getKnives().getCount()).orElse(0);
        default:
            throw new AssertionError();
        }
    }
    
    protected void renderStuckItem(Type projectileType, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, Entity entity, 
            float x, float y, float z, float partialTick) {
        float f = MathHelper.sqrt(x * x + z * z);
        switch (projectileType) {
        case ARROW:
            arrow = new ArrowEntity(entity.level, entity.getX(), entity.getY(), entity.getZ());
            arrow.yRot = (float)(Math.atan2((double)x, (double)z) * (double)(180F / (float)Math.PI));
            arrow.xRot = (float)(Math.atan2((double)y, (double)f) * (double)(180F / (float)Math.PI));
            arrow.yRotO = arrow.yRot;
            arrow.xRotO = arrow.xRot;
            dispatcher.render(arrow, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, matrixStack, buffer, packedLight);
            break;
        case KNIFE:
            knife = new KnifeEntity(entity.level, entity.getX(), entity.getY(), entity.getZ());
            knife.yRot = (float)(Math.atan2((double)x, (double)z) * (double)(180F / (float)Math.PI));
            knife.xRot = (float)(Math.atan2((double)y, (double)f) * (double)(180F / (float)Math.PI));
            knife.yRotO = knife.yRot;
            knife.xRotO = knife.xRot;
            dispatcher.render(knife, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, matrixStack, buffer, packedLight);
            break;
        }
    }
    
    private enum Type {
        ARROW,
        KNIFE
    }
    
    
    private void lazyInitModelParts() {
        if (modelParts == null) {
            M model = getParentModel();
            if (model != null) {
                List<ModelRenderer> inModModelParts = FieldUtils.getAllFieldsList(model.getClass()).stream()
                        .filter(field -> ModelRenderer.class.isAssignableFrom(field.getType()))
                        .flatMap(field -> {
                            field.setAccessible(true);
                            ModelRenderer inModModelPart;
                            try {
                                inModModelPart = (ModelRenderer) field.get(model);
                                return Stream.of(inModModelPart);
                            } catch (IllegalArgumentException | IllegalAccessException e) {
                                return Stream.empty();
                            }
                        })
                        .collect(Collectors.toList());
                
                List<ModelRenderer> prevGen = new ArrayList<>(inModModelParts);
                List<ModelRenderer> children = new ArrayList<>();
                do {
                    for (ModelRenderer modelPart : prevGen) {
                        children.addAll(ClientReflection.getChildren(modelPart));
                    }
                    inModModelParts.addAll(children);
                    prevGen = children;
                    children = new ArrayList<>();
                }
                while (!children.isEmpty());
                
                this.modelParts = inModModelParts.stream()
                        .filter(modelPart -> !ClientReflection.getCubes(modelPart).isEmpty())
                        .collect(Collectors.toList());
            }
            else {
                this.modelParts = Collections.emptyList();
            }
        }
    }

}
