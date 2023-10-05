package com.github.standobyte.jojo.client.render.entity.renderer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.codehaus.plexus.util.ReflectionUtils;

import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.CDBlockBulletRenderer;
import com.github.standobyte.jojo.client.render.rendertype.CustomRenderType;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;

public class GETransformationRenderer<T extends GETransformationEntity> extends EntityRenderer<T> {

    public GETransformationRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return null;
    }

    @SuppressWarnings("resource")
    @Override
    public void render(T entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (!entity.isInvisibleTo(Minecraft.getInstance().player)) {
            float age = entity.tickCount + partialTick;
            float ageMax = entity.getDuration();
            float itemSourceAge = GETransformationEntity.getRenderAsItemTime(ageMax);
            if (age < itemSourceAge) {
                Entity source = entity.getTransformationSource();
                if (source != null) {
                    float scale = 1 - age / itemSourceAge;
                    matrixStack.pushPose();
                    matrixStack.scale(scale, scale, scale);
                    this.shadowRadius = 0.15F * scale;
                    entityRenderDispatcher.getRenderer(source).render(source, yRotation, partialTick, matrixStack, buffer, packedLight);
                    matrixStack.popPose();
                }
            }
            else {
                Entity target = entity.getTransformationTarget();
                if (target != null) {
                    float progress = MathHelper.clamp((age - itemSourceAge) / (ageMax - itemSourceAge), 0, 1);
                    if (target instanceof LivingEntity) {
                        renderTransformationLiving((LivingEntity) target, entity, 
                                yRotation, partialTick, matrixStack, buffer, packedLight, progress);
                    }
                    else {
                        renderTransformationNonLiving(target, entity, 
                                yRotation, partialTick, matrixStack, buffer, packedLight, progress);
                    }
                }
            }
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
    
    private <E extends Entity> void renderTransformationNonLiving(E target, T transformationEntity, 
            float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, float progress) {
        
    }
    
    private <E extends LivingEntity, M extends EntityModel<E>> void renderTransformationLiving(E living, T transformationEntity, 
            float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, float progress) {
        LivingRenderer<E, M> renderer = (LivingRenderer<E, M>) entityRenderDispatcher.getRenderer(living);
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<E, M>(living, renderer, partialTick, matrixStack, buffer, packedLight))) return;
        
        M targetModel = renderer.getModel();
        matrixStack.pushPose();
        targetModel.attackTime = 0;

        targetModel.riding = false;
        targetModel.young = living.isBaby();
        float yHeadRotation = MathHelper.rotLerp(partialTick, living.yHeadRotO, living.yHeadRot);
        float yBodyRotation = MathHelper.rotLerp(partialTick, living.yBodyRotO, living.yBodyRot);
        float f2 = yHeadRotation - yBodyRotation;
        
        float xRotation = MathHelper.lerp(partialTick, transformationEntity.xRotO, transformationEntity.xRot);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - yBodyRotation));

        float ticks = living.tickCount + partialTick;
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        ClientReflection.scale(renderer, living, matrixStack, partialTick);
        matrixStack.scale(progress, progress, progress);
        matrixStack.translate(0.0D, (double)-1.501F, 0.0D);

        targetModel.prepareMobModel(living, 0, 0, partialTick);
        targetModel.setupAnim(living, 0, 0, ticks, f2, xRotation);
        RenderType rendertype = targetModel.renderType(renderer.getTextureLocation(living));
        if (rendertype != null) {
            this.shadowRadius = ClientReflection.getShadowRadius(renderer) * progress; // cache this?
            
            ModelStateEntry modelState = getModelState(targetModel);
            modelState.saveState();
            modelState.lerp(progress);
            
            IVertexBuilder ivertexbuilder = buffer.getBuffer(rendertype);
            int overlay = OverlayTexture.NO_OVERLAY;
            
            
            float color = 0.25F + progress * 0.75F;
            targetModel.renderToBuffer(matrixStack, ivertexbuilder, packedLight, overlay, color, color, color, 1.0F);
//            for (LayerRenderer<E, M> layerrenderer : ClientReflection.getLayers(renderer)) {
//                layerrenderer.render(matrixStack, buffer, packedLight, living, 0, 0, partialTick, ticks, f2, xRotation);
//            }
            
            float blockOverlayAlpha = 1.0F - progress;
            if (blockOverlayAlpha > 0) {
                ResourceLocation blockSprite = getBlockOverlaySprite(transformationEntity.getTransformationSource());
                if (blockSprite != null) {
                    RenderType renderTypeItem = CustomRenderType.goldExperienceLifeformOverlay(
                            blockSprite, targetModel.texWidth / 16F, targetModel.texHeight / 16F);
                    if (renderTypeItem != null) {
                        IVertexBuilder vertexBuilderItem = buffer.getBuffer(renderTypeItem);
                        targetModel.renderToBuffer(matrixStack, vertexBuilderItem, packedLight, overlay, 1.0F, 1.0F, 1.0F, blockOverlayAlpha);
                    }
                }
            }
            
            modelState.restoreState();
        }

        matrixStack.popPose();
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<E, M>(living, renderer, partialTick, matrixStack, buffer, packedLight));
    }
    
    
    
    private <S extends Entity> ResourceLocation getBlockOverlaySprite(S entity) {
        if (entity instanceof ItemEntity) {
            ItemStack item = ((ItemEntity) entity).getItem();
            if (!item.isEmpty() && item.getItem() instanceof BlockItem) {
                Block block = ((BlockItem) item.getItem()).getBlock();
                ResourceLocation tex = CDBlockBulletRenderer.getBlockTexture(block.defaultBlockState());
                return tex;
            }
        }
        
        return null;
    }
    
    
    
    private static Map<ModelRenderer, float[]> createStateZero(Collection<ModelRenderer> modelParts) {
        Map<ModelRenderer, float[]> map = new HashMap<>();
        modelParts.forEach(modelPart -> {
            ObjectList<ModelRenderer.ModelBox> boxes = ClientReflection.getCubes(modelPart);
            float minX = boxes.stream().map(box -> box.minX).min(Float::compare).orElse(0f);
            float maxX = boxes.stream().map(box -> box.maxX).max(Float::compare).orElse(0f);
            float minY = boxes.stream().map(box -> box.minY).min(Float::compare).orElse(0f);
            float maxY = boxes.stream().map(box -> box.maxY).max(Float::compare).orElse(0f);
            float minZ = boxes.stream().map(box -> box.minZ).min(Float::compare).orElse(0f);
            float maxZ = boxes.stream().map(box -> box.maxZ).max(Float::compare).orElse(0f);
            
            float x = -(minX + maxX / 2);
            float y = -(minY + maxY / 2);
            float z = -(minZ + maxZ / 2);
            y += modelPart.y;
            map.put(modelPart, new float[] {x, y, z, 0, 0, 0});
        });
        return map;
    }
    
    
    private ModelStateEntry getModelState(EntityModel<?> model) {
        return MODEL_PARTS_CACHE.computeIfAbsent(model, m -> new ModelStateEntry(model));
    }
    
    private static final Map<EntityModel<?>, ModelStateEntry> MODEL_PARTS_CACHE = new HashMap<>();
    private static class ModelStateEntry {
        // TODO replace with a singular map
        private final Map<ModelRenderer, ModelRendererState> state;
        
        private ModelStateEntry(EntityModel<?> model) {
            Collection<ModelRenderer> modelParts = getModelParts(model);
            Map<ModelRenderer, float[]> stateZero = createStateZero(modelParts);
            this.state = modelParts.stream().collect(Collectors.toMap(Function.identity(), modelPart -> {
                float[] partStateZero = stateZero.get(modelPart);
                return new ModelRendererState()
                        .withNormalState(modelPart)
                        .withStateZero(partStateZero[0], partStateZero[1], partStateZero[2], partStateZero[3], partStateZero[4], partStateZero[5]);
            }));
        }
        
        public void saveState() {
            state.entrySet().forEach(entry -> entry.getValue().saveState(entry.getKey()));
        }
        
        public void lerp(float amount) {
            state.entrySet().forEach(entry -> entry.getValue().lerp(entry.getKey(), amount));
        }
        
        public void restoreState() {
            state.entrySet().forEach(entry -> entry.getValue().restoreState(entry.getKey()));
        }
    }
    
    private static class ModelRendererState {
        private final float[] stateNormal = new float[6];
        private final float[] stateZero = new float[6];
        private final float[] stateSaved = new float[6];
        
        public ModelRendererState withStateZero(float x, float y, float z,
                float xRot, float yRot, float zRot) {
            stateZero[0] = x;
            stateZero[1] = y;
            stateZero[2] = z;
            stateZero[3] = xRot;
            stateZero[4] = yRot;
            stateZero[5] = zRot;
            return this;
        }
        
        public ModelRendererState withNormalState(float x, float y, float z,
                float xRot, float yRot, float zRot) {
            stateNormal[0] = x;
            stateNormal[1] = y;
            stateNormal[2] = z;
            stateNormal[3] = xRot;
            stateNormal[4] = yRot;
            stateNormal[5] = zRot;
            return this;
        }
        
        public ModelRendererState withNormalState(ModelRenderer modelRenderer) {
            return withNormalState(modelRenderer.x, modelRenderer.y, modelRenderer.z, modelRenderer.xRot, modelRenderer.yRot, modelRenderer.zRot);
        }
        
        public void saveState(ModelRenderer modelRenderer) {
            stateSaved[0] = modelRenderer.x;
            stateSaved[1] = modelRenderer.y;
            stateSaved[2] = modelRenderer.z;
            stateSaved[3] = modelRenderer.xRot;
            stateSaved[4] = modelRenderer.yRot;
            stateSaved[5] = modelRenderer.zRot;
        }
        
        public void restoreState(ModelRenderer modelRenderer) {
            modelRenderer.x = stateSaved[0];
            modelRenderer.y = stateSaved[1];
            modelRenderer.z = stateSaved[2];
            modelRenderer.xRot = stateSaved[3];
            modelRenderer.yRot = stateSaved[4];
            modelRenderer.zRot = stateSaved[5];
        }
        
        public void lerp(ModelRenderer modelRenderer, float lerp) {
            modelRenderer.x = MathHelper.lerp(lerp, stateZero[0], stateNormal[0]);
            modelRenderer.y = MathHelper.lerp(lerp, stateZero[1], stateNormal[1]);
            modelRenderer.z = MathHelper.lerp(lerp, stateZero[2], stateNormal[2]);
            modelRenderer.xRot = MathHelper.lerp(lerp, stateZero[3], stateNormal[3]);
            modelRenderer.yRot = MathHelper.lerp(lerp, stateZero[4], stateNormal[4]);
            modelRenderer.zRot = MathHelper.lerp(lerp, stateZero[5], stateNormal[5]);
        }
    }
    
    
    
    private static Collection<ModelRenderer> getModelParts(EntityModel<?> model) {
        Set<ModelRenderer> modelParts = new HashSet<>();
        if (model instanceof AgeableModel) {
            AgeableModel<?> ageable = (AgeableModel<?>) model;
            ClientReflection.getHeadParts(ageable).forEach(modelPart -> addSubPartsAndSelf(modelParts, modelPart));
            ClientReflection.getBodyParts(ageable).forEach(modelPart -> addSubPartsAndSelf(modelParts, modelPart));
        }
        else if (model instanceof SegmentedModel) {
            SegmentedModel<?> segmented = (SegmentedModel<?>) model;
            segmented.parts().forEach(modelPart -> addSubPartsAndSelf(modelParts, modelPart));
        }
        else {
            ReflectionUtils.getFieldsIncludingSuperclasses(model.getClass()).forEach(field -> {
                if (ModelRenderer.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Object obj = field.get(model);
                        if (obj != null) {
                            ModelRenderer modelPart = (ModelRenderer) obj;
                            addSubPartsAndSelf(modelParts, modelPart);
                        }
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                else if (ModelRenderer[].class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Object obj = field.get(model);
                        if (obj != null) {
                            for (ModelRenderer modelPart : (ModelRenderer[]) obj)
                            addSubPartsAndSelf(modelParts, modelPart);
                        }
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        }
        
        return modelParts;
    }
    
    private static void addSubPartsAndSelf(Set<ModelRenderer> modelParts, ModelRenderer modelRenderer) {
        modelParts.add(modelRenderer);
        ObjectList<ModelRenderer> children = ClientReflection.getChildren(modelRenderer);
        children.forEach(child -> addSubPartsAndSelf(modelParts, child));
    }
}
