package com.github.standobyte.jojo.client.render.entity.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.github.standobyte.jojo.entity.AngeloRockEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class AngeloRockModel extends EntityModel<AngeloRockEntity> {
    public final ModelRenderer upperHalf;
    public final ModelRenderer lowerHalf;
    private final Map<ModelRenderer, List<ModelRenderer.ModelBox>> allCubesByParts;
    private final List<ModelRenderer.ModelBox> allCubes;
    private float progress;
    private final Set<ModelRenderer.ModelBox> visibleCubes;

    public AngeloRockModel() {
        texWidth = 16;
        texHeight = 16;

        upperHalf = new ModelRenderer(this);
        upperHalf.setPos(0.0F, 24.0F, 0.0F);
        upperHalf.texOffs(-11, -6).addBox(-8.0F, -32.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        upperHalf.texOffs(-11, -6).addBox(0.0F, -32.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        upperHalf.texOffs(-11, -6).addBox(-8.0F, -24.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        upperHalf.texOffs(-11, -6).addBox(0.0F, -24.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        upperHalf.texOffs(-11, -6).addBox(-8.0F, -32.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        upperHalf.texOffs(-11, -6).addBox(0.0F, -32.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        upperHalf.texOffs(-11, -6).addBox(-8.0F, -24.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        upperHalf.texOffs(-11, -6).addBox(0.0F, -24.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        lowerHalf = new ModelRenderer(this);
        lowerHalf.setPos(0.0F, 24.0F, 0.0F);
        lowerHalf.texOffs(-11, -6).addBox(-8.0F, -16.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        lowerHalf.texOffs(-11, -6).addBox(0.0F, -16.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        lowerHalf.texOffs(-11, -6).addBox(-8.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        lowerHalf.texOffs(-11, -6).addBox(0.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        lowerHalf.texOffs(-11, -6).addBox(-8.0F, -16.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        lowerHalf.texOffs(-11, -6).addBox(0.0F, -16.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        lowerHalf.texOffs(-11, -6).addBox(-8.0F, -8.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        lowerHalf.texOffs(-11, -6).addBox(0.0F, -8.0F, 0.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        allCubesByParts = new HashMap<>();
        allCubes = new ArrayList<>(100);
        visibleCubes = new HashSet<>();
        addCubesFrom(upperHalf);
        addCubesFrom(lowerHalf);
    }
    
    private void addCubesFrom(ModelRenderer modelPart) {
        allCubesByParts.put(modelPart, new ArrayList<>(modelPart.cubes));
        allCubes.addAll(modelPart.cubes);
    }
    
    @Override
    public void setupAnim(AngeloRockEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        float yRot = pNetHeadYaw * MathUtil.DEG_TO_RAD;
        upperHalf.yRot = yRot;
        lowerHalf.yRot = yRot;
    }
    
    public void setCreationAnim(AngeloRockEntity pEntity, float progress) {
        progress = MathHelper.clamp(progress, 0, 1);
        if (progress == 0 && this.progress == 0 || progress == 1 && this.progress == 1) return;
        this.progress = progress;
        if (progress == 0) return;
        
        if (progress == 1) {
            visibleCubes.addAll(allCubes);
        }
        else {
            Random random = new Random(pEntity.getId());
            List<ModelRenderer.ModelBox> cubesShuffled = new ArrayList<>(allCubes);
            Collections.shuffle(cubesShuffled, random);
            visibleCubes.clear();
            int renderParts = 1 + (int) (progress * cubesShuffled.size());
            cubesShuffled.stream().limit(renderParts).forEach(visibleCubes::add);
        }
        for (Map.Entry<ModelRenderer, List<ModelRenderer.ModelBox>> modelPartEntry : this.allCubesByParts.entrySet()) {
            ModelRenderer modelPart = modelPartEntry.getKey();
            if (modelPart.visible) {
                List<ModelRenderer.ModelBox> allCubes = modelPartEntry.getValue();
                modelPart.cubes.clear();
                allCubes.stream().filter(visibleCubes::contains).forEach(modelPart.cubes::add);
            }
        }
    }
    
    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (progress == 0) return;
        matrixStack.pushPose();
        matrixStack.translate(0, -1.5, 0);
        upperHalf.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        lowerHalf.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        matrixStack.popPose();
    }

}
