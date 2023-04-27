package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.client.render.FlameModelRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.SCRapierEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.util.Direction;

public class SCRapierFlameModel extends EntityModel<SCRapierEntity> {
    private FlameModelRenderer flame;

    public SCRapierFlameModel() {
        super(tex -> Atlases.translucentCullBlockSheet());
        
        flame = new FlameModelRenderer(this);
        flame.setPos(-0.5F, 0.0F, 0.0F);
        flame.addFlame(0.0F, -1.0F, 3.0F, 1, 3, Direction.NORTH);
        flame.addFlame(0.0F, -1.0F, 4.5F, 1, 3, Direction.NORTH);
        flame.addFlame(0.0F, -1.0F, 6.0F, 1, 3, Direction.NORTH);
        flame.addFlame(0.0F, -1.0F, 7.5F, 1, 3, Direction.NORTH);
        flame.addFlame(0.0F, -1.0F, 9.0F, 1, 3, Direction.NORTH);
        flame.addFlame(0.0F, -1.0F, 10.5F, 1, 3, Direction.NORTH);
        flame.addFlame(0.0F, -1.0F, 12.0F, 1, 3, Direction.NORTH);
        flame.addFlame(0.0F, -1.0F, 13.5F, 1, 3, Direction.NORTH);
    }

    @Override
    public void setupAnim(SCRapierEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        flame.yRot = yRotationOffset * ((float)Math.PI / 180F);
        flame.xRot = xRotation * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        flame.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}