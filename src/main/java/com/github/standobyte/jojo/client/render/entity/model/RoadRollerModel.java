package com.github.standobyte.jojo.client.render.entity.model;

import com.github.standobyte.jojo.entity.RoadRollerEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class RoadRollerModel extends EntityModel<RoadRollerEntity> {
    private final ModelRenderer roadRoller;
    private final ModelRenderer frontWheel;
    private final ModelRenderer frontWheel2;
    private final ModelRenderer frontWheel3;
    private final ModelRenderer frontWheel4;
    private final ModelRenderer frontSlope;
    private final ModelRenderer mirrorLeft;
    private final ModelRenderer mirrorRight;
    private final ModelRenderer gamingChair;
    private final ModelRenderer steeringWheel;
    private final ModelRenderer lever1;
    private final ModelRenderer lever2;
    private final ModelRenderer backWheel;
    private final ModelRenderer backWheel2;
    private final ModelRenderer backWheel3;
    private final ModelRenderer backWheel4;
    private final ModelRenderer backWheelThing;
    private final ModelRenderer back;

    public RoadRollerModel() {
        texWidth = 256;
        texHeight = 256;

        roadRoller = new ModelRenderer(this);
        roadRoller.setPos(0.0F, -20.0F, 0.0F);
        roadRoller.texOffs(145, 218).addBox(-20.0F, 8.0F, -29.0F, 40.0F, 6.0F, 6.0F, 0.0F, false);
        roadRoller.texOffs(92, 0).addBox(-20.0F, 9.0F, -23.0F, 40.0F, 4.0F, 13.0F, 0.0F, false);
        roadRoller.texOffs(89, 157).addBox(-20.0F, -3.0F, -28.0F, 40.0F, 10.0F, 7.0F, 0.0F, false);
        roadRoller.texOffs(145, 230).addBox(-20.0F, 0.0F, -32.0F, 40.0F, 7.0F, 4.0F, 0.0F, false);
        roadRoller.texOffs(92, 17).addBox(-20.0F, 4.0F, -33.0F, 40.0F, 2.0F, 1.0F, 0.0F, false);
        roadRoller.texOffs(0, 0).addBox(-17.0F, 2.0F, -38.0F, 34.0F, 13.0F, 24.0F, 0.0F, false);
        roadRoller.texOffs(196, 70).addBox(-13.0F, 4.0F, -40.0F, 26.0F, 8.0F, 5.0F, 0.0F, false);
        roadRoller.texOffs(0, 143).addBox(-17.0F, -4.0F, -30.0F, 34.0F, 6.0F, 14.0F, 0.0F, false);
        roadRoller.texOffs(92, 216).addBox(13.0F, -5.0F, -29.0F, 3.0F, 1.0F, 12.0F, 0.0F, false);
        roadRoller.texOffs(0, 0).addBox(13.5F, -5.5F, -28.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        roadRoller.texOffs(86, 49).addBox(-16.0F, -5.0F, -29.0F, 3.0F, 1.0F, 12.0F, 0.0F, false);
        roadRoller.texOffs(12, 19).addBox(-15.5F, -6.0F, -28.0F, 2.0F, 1.0F, 4.0F, 0.0F, false);
        roadRoller.texOffs(186, 49).addBox(-9.0F, 4.0F, -14.0F, 18.0F, 9.0F, 12.0F, 0.0F, false);
        roadRoller.texOffs(198, 36).addBox(-9.0F, -2.0F, -15.0F, 18.0F, 6.0F, 7.0F, 0.0F, false);
        roadRoller.texOffs(152, 196).addBox(-11.0F, 1.0F, -2.0F, 22.0F, 12.0F, 9.0F, 0.0F, false);
        roadRoller.texOffs(196, 104).addBox(-8.0F, 1.9F, -1.9F, 16.0F, 2.0F, 10.0F, 0.0F, false);
        roadRoller.texOffs(3, 226).addBox(-15.0F, 0.9F, 6.9F, 30.0F, 14.0F, 5.0F, 0.0F, false);
        roadRoller.texOffs(0, 37).addBox(-15.0F, -7.0F, 10.5F, 30.0F, 9.0F, 26.0F, 0.0F, false);
        roadRoller.texOffs(116, 20).addBox(-14.0F, -6.0F, 9.5F, 28.0F, 6.0F, 1.0F, 0.0F, false);
        roadRoller.texOffs(0, 186).addBox(-17.0F, -10.0F, 17.0F, 34.0F, 4.0F, 10.0F, 0.0F, false);
        roadRoller.texOffs(0, 72).addBox(-15.0F, -11.0F, 14.5F, 30.0F, 4.0F, 22.0F, 0.0F, false);
        roadRoller.texOffs(0, 132).addBox(-15.0F, -16.0F, 12.5F, 30.0F, 9.0F, 1.0F, 0.0F, false);
        roadRoller.texOffs(109, 216).addBox(10.5F, -16.0F, 12.5F, 1.0F, 9.0F, 17.0F, 0.0F, false);
        roadRoller.texOffs(73, 216).addBox(-0.5F, -16.0F, 12.5F, 1.0F, 9.0F, 17.0F, 0.0F, false);
        roadRoller.texOffs(54, 200).addBox(-11.5F, -16.0F, 12.5F, 1.0F, 9.0F, 17.0F, 0.0F, false);
        roadRoller.texOffs(0, 200).addBox(5.5F, -12.0F, 16.0F, 8.0F, 1.0F, 19.0F, 0.0F, false);
        roadRoller.texOffs(195, 198).addBox(-4.0F, -12.0F, 16.0F, 8.0F, 1.0F, 19.0F, 0.0F, false);
        roadRoller.texOffs(188, 84).addBox(-13.5F, -12.0F, 16.0F, 8.0F, 1.0F, 19.0F, 0.0F, false);
        roadRoller.texOffs(0, 14).addBox(5.0F, -7.0F, 36.0F, 7.0F, 2.0F, 2.0F, 0.0F, false);
        roadRoller.texOffs(0, 18).addBox(5.0F, -5.0F, 36.0F, 7.0F, 4.0F, 1.0F, 0.0F, false);

        frontWheel = new ModelRenderer(this);
        frontWheel.setPos(0.0F, 11.0F, -26.0F);
        roadRoller.addChild(frontWheel);
        frontWheel.texOffs(172, 123).addBox(-18.0F, -8.0F, -3.0F, 36.0F, 16.0F, 6.0F, 0.535F, false);

        frontWheel2 = new ModelRenderer(this);
        frontWheel2.setPos(0.0F, 0.0F, 0.0F);
        frontWheel.addChild(frontWheel2);
        setRotationAngle(frontWheel2, -0.7854F, 0.0F, 0.0F);
        frontWheel2.texOffs(168, 174).addBox(-18.0F, -8.0F, -3.0F, 36.0F, 16.0F, 6.0F, 0.535F, false);

        frontWheel3 = new ModelRenderer(this);
        frontWheel3.setPos(0.0F, 0.0F, 0.0F);
        frontWheel.addChild(frontWheel3);
        setRotationAngle(frontWheel3, -1.5708F, 0.0F, 0.0F);
        frontWheel3.texOffs(84, 174).addBox(-18.0F, -8.0F, -3.0F, 36.0F, 16.0F, 6.0F, 0.535F, false);

        frontWheel4 = new ModelRenderer(this);
        frontWheel4.setPos(0.0F, 0.0F, 0.0F);
        frontWheel.addChild(frontWheel4);
        setRotationAngle(frontWheel4, -2.3562F, 0.0F, 0.0F);
        frontWheel4.texOffs(0, 163).addBox(-18.0F, -8.0F, -3.0F, 36.0F, 16.0F, 6.0F, 0.535F, false);

        frontSlope = new ModelRenderer(this);
        frontSlope.setPos(0.0F, 2.0F, -38.0F);
        roadRoller.addChild(frontSlope);
        setRotationAngle(frontSlope, 0.6435F, 0.0F, 0.0F);
        frontSlope.texOffs(183, 157).addBox(-17.0F, 0.0F, 0.0F, 34.0F, 5.0F, 10.0F, 0.0F, false);
        frontSlope.texOffs(0, 7).addBox(11.0F, -0.5F, 2.0F, 4.0F, 1.0F, 6.0F, 0.0F, false);

        mirrorLeft = new ModelRenderer(this);
        mirrorLeft.setPos(17.0F, -2.5F, -18.0F);
        roadRoller.addChild(mirrorLeft);
        setRotationAngle(mirrorLeft, 0.0F, -0.5236F, 0.0F);
        mirrorLeft.texOffs(62, 132).addBox(0.0F, -6.5F, 0.0F, 3.0F, 7.0F, 1.0F, 0.0F, false);

        mirrorRight = new ModelRenderer(this);
        mirrorRight.setPos(-17.0F, -2.5F, -18.0F);
        roadRoller.addChild(mirrorRight);
        setRotationAngle(mirrorRight, 0.0F, 0.5236F, 0.0F);
        mirrorRight.texOffs(70, 132).addBox(-3.0F, -6.5F, 0.0F, 3.0F, 7.0F, 1.0F, 0.0F, false);

        gamingChair = new ModelRenderer(this);
        gamingChair.setPos(0.0F, 2.0F, 6.0F);
        roadRoller.addChild(gamingChair);
        setRotationAngle(gamingChair, -0.1745F, 0.0F, 0.0F);
        gamingChair.texOffs(35, 200).addBox(-6.0F, -13.0F, 0.0F, 12.0F, 13.0F, 2.0F, 0.0F, false);

        steeringWheel = new ModelRenderer(this);
        steeringWheel.setPos(0.0F, -2.0F, -9.5F);
        roadRoller.addChild(steeringWheel);
        setRotationAngle(steeringWheel, -0.4363F, 0.0F, 0.0F);
        steeringWheel.texOffs(4, 37).addBox(-0.5F, -8.0F, 0.0F, 1.0F, 8.0F, 1.0F, 0.0F, false);
        steeringWheel.texOffs(0, 0).addBox(-3.0F, -9.0F, -2.5F, 6.0F, 1.0F, 6.0F, 0.0F, false);

        lever1 = new ModelRenderer(this);
        lever1.setPos(7.0F, -2.0F, -11.0F);
        roadRoller.addChild(lever1);
        setRotationAngle(lever1, -0.7854F, 0.0F, 0.0F);
        lever1.texOffs(0, 37).addBox(-0.5F, -9.0F, 0.5F, 1.0F, 9.0F, 1.0F, 0.0F, false);

        lever2 = new ModelRenderer(this);
        lever2.setPos(5.0F, -2.0F, -11.0F);
        roadRoller.addChild(lever2);
        setRotationAngle(lever2, -1.0472F, 0.0F, 0.0F);
        lever2.texOffs(20, 7).addBox(-0.5F, -9.0F, 0.5F, 1.0F, 9.0F, 1.0F, 0.0F, false);

        backWheel = new ModelRenderer(this);
        backWheel.setPos(0.0F, 7.0F, 22.0F);
        roadRoller.addChild(backWheel);
        backWheel.texOffs(82, 123).addBox(-18.0F, -12.0F, -5.0F, 36.0F, 24.0F, 10.0F, -0.05F, false);

        backWheel2 = new ModelRenderer(this);
        backWheel2.setPos(0.0F, 0.0F, 0.0F);
        backWheel.addChild(backWheel2);
        setRotationAngle(backWheel2, -0.7854F, 0.0F, 0.0F);
        backWheel2.texOffs(106, 27).addBox(-18.0F, -12.0F, -5.0F, 36.0F, 24.0F, 10.0F, -0.05F, false);

        backWheel3 = new ModelRenderer(this);
        backWheel3.setPos(0.0F, 0.0F, 0.0F);
        backWheel.addChild(backWheel3);
        setRotationAngle(backWheel3, -1.5708F, 0.0F, 0.0F);
        backWheel3.texOffs(104, 62).addBox(-18.0F, -12.0F, -5.0F, 36.0F, 24.0F, 10.0F, -0.05F, false);

        backWheel4 = new ModelRenderer(this);
        backWheel4.setPos(0.0F, 0.0F, 0.0F);
        backWheel.addChild(backWheel4);
        setRotationAngle(backWheel4, -2.3562F, 0.0F, 0.0F);
        backWheel4.texOffs(0, 98).addBox(-18.0F, -12.0F, -5.0F, 36.0F, 24.0F, 10.0F, -0.05F, false);

        backWheelThing = new ModelRenderer(this);
        backWheelThing.setPos(0.0F, 7.0F, 22.0F);
        roadRoller.addChild(backWheelThing);
        setRotationAngle(backWheelThing, -0.2618F, 0.0F, 0.0F);
        backWheelThing.texOffs(96, 96).addBox(-21.0F, -15.0F, -4.0F, 42.0F, 19.0F, 8.0F, 0.0F, false);

        back = new ModelRenderer(this);
        back.setPos(0.0F, -2.0F, 36.5F);
        roadRoller.addChild(back);
        setRotationAngle(back, 0.5236F, 0.0F, 0.0F);
        back.texOffs(84, 196).addBox(-15.0F, 0.0F, -4.0F, 30.0F, 16.0F, 4.0F, 0.0F, false);
        back.texOffs(8, 37).addBox(-5.0F, 0.0F, 0.0F, 3.0F, 1.0F, 1.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(RoadRollerEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        roadRoller.yRot = yRotationOffset * ((float)Math.PI / 180F);
        roadRoller.xRot = xRotation * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        roadRoller.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}