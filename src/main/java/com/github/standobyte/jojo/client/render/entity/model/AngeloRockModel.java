package com.github.standobyte.jojo.client.render.entity.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.github.standobyte.jojo.client.ClientUtil;
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
    public final ModelRenderer shadow;
    private final Map<ModelRenderer, List<ModelRenderer.ModelBox>> allCubesByParts;
    private final List<ModelRenderer.ModelBox> allCubes;
    private float progress;
    private final Set<ModelRenderer.ModelBox> visibleCubes;

    public AngeloRockModel() {
        texWidth = 16;
        texHeight = 16;
        
        ModelRenderer cube_r1;
        ModelRenderer cube_r2;
        ModelRenderer cube_r3;
        ModelRenderer cube_r4;
        ModelRenderer cube_r5;
        ModelRenderer cube_r6;
        ModelRenderer cube_r7;
        ModelRenderer cube_r8;
        ModelRenderer cube_r9;
        ModelRenderer cube_r10;
        ModelRenderer cube_r11;
        ModelRenderer cube_r12;
        ModelRenderer cube_r13;
        ModelRenderer cube_r14;
        ModelRenderer cube_r15;
        ModelRenderer cube_r16;
        ModelRenderer cube_r17;
        ModelRenderer cube_r18;
        ModelRenderer cube_r19;
        ModelRenderer cube_r20;
        ModelRenderer cube_r21;
        ModelRenderer cube_r22;
        ModelRenderer cube_r23;
        ModelRenderer cube_r24;
        ModelRenderer cube_r25;
        ModelRenderer cube_r26;
        ModelRenderer cube_r27;
        ModelRenderer cube_r28;
        ModelRenderer cube_r29;
        ModelRenderer cube_r30;
        ModelRenderer cube_r31;
        ModelRenderer cube_r32;
        ModelRenderer cube_r33;
        ModelRenderer cube_r34;
        ModelRenderer cube_r35;
        ModelRenderer cube_r36;
        ModelRenderer cube_r37;
        ModelRenderer cube_r38;
        ModelRenderer cube_r39;
        ModelRenderer cube_r40;
        ModelRenderer cube_r41;
        ModelRenderer cube_r42;
        ModelRenderer cube_r43;
        ModelRenderer cube_r44;
        ModelRenderer cube_r45;
        ModelRenderer cube_r46;
        ModelRenderer cube_r47;
        ModelRenderer cube_r48;
        ModelRenderer cube_r49;
        ModelRenderer cube_r50;
        ModelRenderer cube_r51;
        ModelRenderer cube_r52;
        ModelRenderer cube_r53;
        ModelRenderer cube_r54;
        ModelRenderer cube_r55;
        ModelRenderer cube_r56;
        ModelRenderer cube_r57;
        ModelRenderer cube_r58;
        ModelRenderer cube_r59;
        ModelRenderer cube_r60;
        ModelRenderer cube_r61;
        ModelRenderer cube_r62;
        ModelRenderer cube_r63;
        ModelRenderer cube_r64;
        ModelRenderer cube_r65;
        ModelRenderer cube_r66;
        ModelRenderer cube_r67;
        ModelRenderer cube_r68;
        ModelRenderer cube_r69;
        ModelRenderer cube_r70;
        ModelRenderer cube_r71;
        ModelRenderer cube_r72;
        ModelRenderer cube_r73;
        ModelRenderer cube_r74;
        ModelRenderer cube_r75;
        ModelRenderer cube_r76;
        ModelRenderer cube_r77;
        ModelRenderer cube_r78;
        ModelRenderer cube_r79;
        ModelRenderer cube_r80;
        ModelRenderer cube_r81;
        ModelRenderer cube_r82;
        ModelRenderer cube_r83;
        ModelRenderer cube_r84;
        ModelRenderer cube_r85;
        ModelRenderer cube_r86;
        ModelRenderer cube_r87;
        ModelRenderer cube_r88;
        ModelRenderer cube_r89;
        ModelRenderer cube_r90;
        ModelRenderer cube_r91;
        ModelRenderer cube_r92;
        ModelRenderer cube_r93;
        ModelRenderer cube_r94;
        ModelRenderer cube_r95;
        ModelRenderer cube_r96;
        ModelRenderer cube_r97;
        ModelRenderer cube_r98;
        ModelRenderer cube_r99;
        ModelRenderer cube_r100;
        ModelRenderer cube_r101;
        ModelRenderer cube_r102;
        ModelRenderer cube_r103;
        ModelRenderer cube_r104;
        ModelRenderer cube_r105;
        ModelRenderer cube_r106;
        ModelRenderer cube_r107;
        ModelRenderer cube_r108;
        ModelRenderer cube_r109;
        ModelRenderer cube_r110;
        ModelRenderer cube_r111;
        ModelRenderer cube_r112;
        ModelRenderer cube_r113;
        



        upperHalf = new ModelRenderer(this);
        upperHalf.setPos(0.0F, 24.0F, 0.0F);
        

        cube_r1 = new ModelRenderer(this);
        cube_r1.setPos(-4.7183F, -15.7599F, -1.9089F);
        upperHalf.addChild(cube_r1);
        ClientUtil.setRotationAngle(cube_r1, 0.6723F, 0.2515F, 0.6957F);
        cube_r1.texOffs(5, 5).addBox(-2.0F, -1.06F, -0.5482F, 4.0F, 4.0F, 2.0F, 0.0F, true);

        cube_r2 = new ModelRenderer(this);
        cube_r2.setPos(-2.4678F, -15.9199F, -2.9893F);
        upperHalf.addChild(cube_r2);
        ClientUtil.setRotationAngle(cube_r2, 0.0677F, 0.0992F, 0.7584F);
        cube_r2.texOffs(7, 4).addBox(-0.5F, -2.5F, -1.0F, 1.0F, 5.0F, 2.0F, 0.0F, true);

        cube_r3 = new ModelRenderer(this);
        cube_r3.setPos(2.0498F, -16.206F, -3.4631F);
        upperHalf.addChild(cube_r3);
        ClientUtil.setRotationAngle(cube_r3, 0.0345F, 0.0383F, -0.7368F);
        cube_r3.texOffs(5, 8).addBox(-0.5F, -2.5F, -1.0F, 1.0F, 5.0F, 2.0F, 0.0F, false);

        cube_r4 = new ModelRenderer(this);
        cube_r4.setPos(-0.25F, -7.65F, -1.0F);
        upperHalf.addChild(cube_r4);
        ClientUtil.setRotationAngle(cube_r4, -1.248F, -1.4329F, 1.2509F);
        cube_r4.texOffs(13, 9).addBox(-2.5F, -19.5F, -3.5F, 6.0F, 11.0F, 7.0F, 0.0F, false);

        cube_r5 = new ModelRenderer(this);
        cube_r5.setPos(-7.2183F, -19.1599F, -0.3589F);
        upperHalf.addChild(cube_r5);
        ClientUtil.setRotationAngle(cube_r5, -0.1753F, 0.9512F, -0.5726F);
        cube_r5.texOffs(6, 5).addBox(-1.0F, -1.06F, -0.5482F, 3.0F, 4.0F, 2.0F, 0.0F, true);

        cube_r6 = new ModelRenderer(this);
        cube_r6.setPos(-5.987F, -16.8635F, 1.834F);
        upperHalf.addChild(cube_r6);
        ClientUtil.setRotationAngle(cube_r6, -0.1058F, 1.4622F, -1.8226F);
        cube_r6.texOffs(12, 15).addBox(-2.0F, -2.0F, -3.5F, 4.0F, 6.0F, 5.0F, 0.0F, true);

        cube_r7 = new ModelRenderer(this);
        cube_r7.setPos(2.5815F, -14.5F, 3.0261F);
        upperHalf.addChild(cube_r7);
        ClientUtil.setRotationAngle(cube_r7, 2.8617F, -1.0844F, -2.8657F);
        cube_r7.texOffs(7, 8).addBox(-2.0F, -8.5F, -2.5F, 7.0F, 10.0F, 6.0F, 0.0F, false);

        cube_r8 = new ModelRenderer(this);
        cube_r8.setPos(-0.4762F, -20.5232F, 6.7451F);
        upperHalf.addChild(cube_r8);
        ClientUtil.setRotationAngle(cube_r8, -2.9596F, -0.2701F, -3.0725F);
        cube_r8.texOffs(14, 11).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

        cube_r9 = new ModelRenderer(this);
        cube_r9.setPos(6.8315F, -16.5F, 1.0261F);
        upperHalf.addChild(cube_r9);
        ClientUtil.setRotationAngle(cube_r9, -2.0839F, -1.3544F, 1.8804F);
        cube_r9.texOffs(9, 10).addBox(-2.0F, -2.5F, -1.5F, 5.0F, 4.0F, 4.0F, 0.0F, false);

        cube_r10 = new ModelRenderer(this);
        cube_r10.setPos(0.603F, -25.4857F, -1.5031F);
        upperHalf.addChild(cube_r10);
        ClientUtil.setRotationAngle(cube_r10, 0.5683F, 1.0823F, -0.8161F);
        cube_r10.texOffs(6, 4).addBox(-1.6548F, 1.5433F, -0.398F, 3.0F, 2.0F, 3.0F, 0.0F, false);

        cube_r11 = new ModelRenderer(this);
        cube_r11.setPos(0.603F, -25.4857F, -1.5031F);
        upperHalf.addChild(cube_r11);
        ClientUtil.setRotationAngle(cube_r11, 0.1394F, 1.1599F, -1.2904F);
        cube_r11.texOffs(0, 1).addBox(-2.0207F, -2.2021F, -0.398F, 3.0F, 4.0F, 3.0F, 0.0F, false);

        cube_r12 = new ModelRenderer(this);
        cube_r12.setPos(0.603F, -25.4857F, -1.5031F);
        upperHalf.addChild(cube_r12);
        ClientUtil.setRotationAngle(cube_r12, -1.0679F, 0.6081F, -2.7086F);
        cube_r12.texOffs(6, 4).addBox(-0.2518F, -3.6348F, -0.398F, 3.0F, 2.25F, 3.0F, 0.0F, false);

        cube_r13 = new ModelRenderer(this);
        cube_r13.setPos(0.7034F, -27.5561F, 1.7341F);
        upperHalf.addChild(cube_r13);
        ClientUtil.setRotationAngle(cube_r13, -0.8212F, 1.1492F, -2.3533F);
        cube_r13.texOffs(8, 2).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F, 0.0F, false);

        cube_r14 = new ModelRenderer(this);
        cube_r14.setPos(0.603F, -25.4857F, -1.5031F);
        upperHalf.addChild(cube_r14);
        ClientUtil.setRotationAngle(cube_r14, -0.8451F, 0.9323F, -2.3939F);
        cube_r14.texOffs(7, 4).addBox(0.0091F, -2.3311F, -0.398F, 2.0F, 1.0F, 3.0F, -0.001F, false);

        cube_r15 = new ModelRenderer(this);
        cube_r15.setPos(-5.5815F, -19.0F, 2.5261F);
        upperHalf.addChild(cube_r15);
        ClientUtil.setRotationAngle(cube_r15, 2.2896F, 0.2721F, 2.208F);
        cube_r15.texOffs(13, 12).addBox(-1.0F, -2.5F, -1.5F, 3.0F, 2.0F, 2.0F, 0.0F, true);

        cube_r16 = new ModelRenderer(this);
        cube_r16.setPos(-5.0865F, -21.3472F, 3.1215F);
        upperHalf.addChild(cube_r16);
        ClientUtil.setRotationAngle(cube_r16, 2.5927F, 0.2702F, 2.4116F);
        cube_r16.texOffs(9, 2).addBox(-0.5F, -1.0F, -2.0F, 2.0F, 2.0F, 3.0F, 0.0F, true);

        cube_r17 = new ModelRenderer(this);
        cube_r17.setPos(-4.8315F, -18.5F, 4.2761F);
        upperHalf.addChild(cube_r17);
        ClientUtil.setRotationAngle(cube_r17, 2.4907F, 0.6898F, 2.332F);
        cube_r17.texOffs(4, 2).addBox(-3.0F, -2.5F, -1.5F, 5.0F, 4.0F, 4.0F, 0.0F, true);

        cube_r18 = new ModelRenderer(this);
        cube_r18.setPos(0.0815F, -16.75F, 4.7761F);
        upperHalf.addChild(cube_r18);
        ClientUtil.setRotationAngle(cube_r18, 3.0235F, -0.5502F, -2.5147F);
        cube_r18.texOffs(6, 13).addBox(1.0F, -2.5F, -2.5F, 4.0F, 4.0F, 5.0F, 0.0F, false);

        cube_r19 = new ModelRenderer(this);
        cube_r19.setPos(1.3315F, -14.5F, 4.7761F);
        upperHalf.addChild(cube_r19);
        ClientUtil.setRotationAngle(cube_r19, 2.2632F, -1.155F, -2.657F);
        cube_r19.texOffs(5, 13).addBox(2.0F, -2.5F, -2.5F, 3.0F, 4.0F, 6.0F, 0.0F, false);

        cube_r20 = new ModelRenderer(this);
        cube_r20.setPos(3.551F, -16.0381F, 5.5679F);
        upperHalf.addChild(cube_r20);
        ClientUtil.setRotationAngle(cube_r20, 1.9104F, -1.2029F, -1.8779F);
        cube_r20.texOffs(9, 12).addBox(-2.5F, -3.0F, -3.0F, 5.0F, 6.0F, 4.0F, 0.0F, false);

        cube_r21 = new ModelRenderer(this);
        cube_r21.setPos(2.0633F, -19.3507F, 6.8138F);
        upperHalf.addChild(cube_r21);
        ClientUtil.setRotationAngle(cube_r21, -1.6108F, -1.2519F, 1.8394F);
        cube_r21.texOffs(4, 5).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 3.0F, 4.0F, 0.0F, false);

        cube_r22 = new ModelRenderer(this);
        cube_r22.setPos(4.3975F, -19.286F, -1.8665F);
        upperHalf.addChild(cube_r22);
        ClientUtil.setRotationAngle(cube_r22, -0.5589F, -0.9935F, 0.8948F);
        cube_r22.texOffs(3, 2).addBox(0.5302F, -1.7797F, -2.8565F, 3.0F, 5.0F, 5.0F, 0.0F, false);

        cube_r23 = new ModelRenderer(this);
        cube_r23.setPos(-5.3606F, -20.9278F, -0.6169F);
        upperHalf.addChild(cube_r23);
        ClientUtil.setRotationAngle(cube_r23, -0.1175F, 0.383F, 0.7463F);
        cube_r23.texOffs(10, 1).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 5.0F, 0.0F, true);

        cube_r24 = new ModelRenderer(this);
        cube_r24.setPos(-2.6183F, -19.0599F, -2.1089F);
        upperHalf.addChild(cube_r24);
        ClientUtil.setRotationAngle(cube_r24, 0.0339F, 0.3194F, 0.7529F);
        cube_r24.texOffs(3, 7).addBox(-2.0F, -1.06F, -0.5482F, 4.0F, 4.25F, 2.0F, 0.0F, true);

        cube_r25 = new ModelRenderer(this);
        cube_r25.setPos(-2.7183F, -19.1599F, -2.0589F);
        upperHalf.addChild(cube_r25);
        ClientUtil.setRotationAngle(cube_r25, -0.1843F, 0.3194F, 0.7529F);
        cube_r25.texOffs(0, 4).addBox(-6.0F, -2.9163F, -0.7646F, 8.0F, 2.0F, 3.0F, 0.0F, true);

        cube_r26 = new ModelRenderer(this);
        cube_r26.setPos(4.3975F, -19.286F, -1.8665F);
        upperHalf.addChild(cube_r26);
        ClientUtil.setRotationAngle(cube_r26, -0.3139F, -0.3131F, -0.7163F);
        cube_r26.texOffs(6, 9).addBox(-3.5459F, -3.5813F, -0.993F, 7.0F, 2.0F, 3.0F, 0.0F, false);

        cube_r27 = new ModelRenderer(this);
        cube_r27.setPos(4.3975F, -19.286F, -1.8665F);
        upperHalf.addChild(cube_r27);
        ClientUtil.setRotationAngle(cube_r27, -0.0957F, -0.3131F, -0.7163F);
        cube_r27.texOffs(5, 7).addBox(-3.6958F, -1.7542F, -0.6304F, 6.0F, 4.0F, 2.0F, 0.0F, false);

        cube_r28 = new ModelRenderer(this);
        cube_r28.setPos(6.275F, -21.0721F, -0.6892F);
        upperHalf.addChild(cube_r28);
        ClientUtil.setRotationAngle(cube_r28, -1.1582F, -1.2349F, 0.6829F);
        cube_r28.texOffs(6, 5).addBox(-1.0F, -2.5F, -1.0F, 4.0F, 5.0F, 3.0F, 0.0F, false);

        cube_r29 = new ModelRenderer(this);
        cube_r29.setPos(5.4626F, -23.8765F, 2.5666F);
        upperHalf.addChild(cube_r29);
        ClientUtil.setRotationAngle(cube_r29, -0.9411F, -0.8948F, -0.305F);
        cube_r29.texOffs(11, 11).addBox(-2.0F, -1.5F, -2.0F, 3.0F, 3.0F, 3.0F, 0.0F, false);

        cube_r30 = new ModelRenderer(this);
        cube_r30.setPos(4.051F, -20.5381F, 4.8179F);
        upperHalf.addChild(cube_r30);
        ClientUtil.setRotationAngle(cube_r30, -0.2797F, -1.3999F, -0.0268F);
        cube_r30.texOffs(7, 10).addBox(-2.5F, -2.0F, -3.0F, 4.0F, 5.0F, 4.0F, 0.0F, false);

        cube_r31 = new ModelRenderer(this);
        cube_r31.setPos(4.1766F, -24.3429F, 3.5363F);
        upperHalf.addChild(cube_r31);
        ClientUtil.setRotationAngle(cube_r31, 0.3178F, -0.5732F, -0.8982F);
        cube_r31.texOffs(12, 8).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 3.0F, 4.0F, 0.0F, false);

        cube_r32 = new ModelRenderer(this);
        cube_r32.setPos(-2.4315F, -22.9629F, 4.8246F);
        upperHalf.addChild(cube_r32);
        ClientUtil.setRotationAngle(cube_r32, 1.1122F, -0.3447F, -2.3343F);
        cube_r32.texOffs(11, 9).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 5.0F, 0.0F, false);

        cube_r33 = new ModelRenderer(this);
        cube_r33.setPos(0.9266F, -23.8429F, 6.0363F);
        upperHalf.addChild(cube_r33);
        ClientUtil.setRotationAngle(cube_r33, 1.4008F, -0.9866F, -1.628F);
        cube_r33.texOffs(12, 11).addBox(-1.0F, -2.0F, -1.5F, 2.0F, 4.0F, 3.0F, 0.0F, false);

        cube_r34 = new ModelRenderer(this);
        cube_r34.setPos(-4.3315F, -23.5F, 3.2761F);
        upperHalf.addChild(cube_r34);
        ClientUtil.setRotationAngle(cube_r34, 2.028F, 0.1806F, 1.9952F);
        cube_r34.texOffs(12, 12).addBox(-2.0F, -2.5F, -1.5F, 4.0F, 4.0F, 2.0F, 0.0F, true);

        cube_r35 = new ModelRenderer(this);
        cube_r35.setPos(-4.3476F, -25.3139F, 1.3424F);
        upperHalf.addChild(cube_r35);
        ClientUtil.setRotationAngle(cube_r35, 1.5414F, 0.7759F, 2.4594F);
        cube_r35.texOffs(10, 9).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 2.0F, 0.0F, true);

        cube_r36 = new ModelRenderer(this);
        cube_r36.setPos(0.603F, -25.4857F, -1.0031F);
        upperHalf.addChild(cube_r36);
        ClientUtil.setRotationAngle(cube_r36, 1.8907F, 0.9426F, 0.3011F);
        cube_r36.texOffs(4, 4).addBox(-1.2871F, 1.1259F, -2.4335F, 5.0F, 3.6F, 3.0F, -0.001F, false);

        cube_r37 = new ModelRenderer(this);
        cube_r37.setPos(0.603F, -25.4857F, -1.0031F);
        upperHalf.addChild(cube_r37);
        ClientUtil.setRotationAngle(cube_r37, -1.8531F, 0.8454F, 2.9672F);
        cube_r37.texOffs(7, 4).addBox(1.6095F, -4.6629F, -2.4335F, 2.0F, 3.6F, 3.0F, 0.0F, false);

        cube_r38 = new ModelRenderer(this);
        cube_r38.setPos(0.603F, -25.4857F, -1.0031F);
        upperHalf.addChild(cube_r38);
        ClientUtil.setRotationAngle(cube_r38, -3.1032F, 1.3848F, 1.6492F);
        cube_r38.texOffs(7, 2).addBox(0.0818F, -3.1344F, -2.4335F, 2.0F, 3.6F, 3.0F, -0.001F, false);

        cube_r39 = new ModelRenderer(this);
        cube_r39.setPos(0.603F, -25.4857F, -1.0031F);
        upperHalf.addChild(cube_r39);
        ClientUtil.setRotationAngle(cube_r39, -2.6765F, 1.3625F, 2.084F);
        cube_r39.texOffs(8, 0).addBox(0.1205F, -0.3509F, -2.4335F, 2.0F, 3.6F, 3.0F, 0.0F, false);

        cube_r40 = new ModelRenderer(this);
        cube_r40.setPos(4.7187F, -23.9366F, -2.3122F);
        upperHalf.addChild(cube_r40);
        ClientUtil.setRotationAngle(cube_r40, -2.6517F, 0.8448F, -0.0438F);
        cube_r40.texOffs(1, 5).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, -0.001F, false);

        cube_r41 = new ModelRenderer(this);
        cube_r41.setPos(-2.8507F, -23.4696F, -1.7094F);
        upperHalf.addChild(cube_r41);
        ClientUtil.setRotationAngle(cube_r41, 2.1618F, -0.8956F, -0.019F);
        cube_r41.texOffs(15, 0).addBox(-1.4772F, -0.5747F, -0.9995F, 2.0F, 3.6F, 2.0F, -0.001F, true);

        cube_r42 = new ModelRenderer(this);
        cube_r42.setPos(-2.8507F, -23.4696F, -1.7094F);
        upperHalf.addChild(cube_r42);
        ClientUtil.setRotationAngle(cube_r42, -2.7478F, -1.1841F, -1.5289F);
        cube_r42.texOffs(3, 0).addBox(-1.476F, -3.0257F, -1.0005F, 2.0F, 3.6F, 2.0F, 0.0F, true);

        cube_r43 = new ModelRenderer(this);
        cube_r43.setPos(3.4125F, -23.3533F, -2.2979F);
        upperHalf.addChild(cube_r43);
        ClientUtil.setRotationAngle(cube_r43, -2.5804F, 1.1929F, 1.7422F);
        cube_r43.texOffs(3, 0).addBox(-0.8722F, -4.0741F, -1.0F, 2.0F, 5.6F, 2.0F, 0.0F, false);

        cube_r44 = new ModelRenderer(this);
        cube_r44.setPos(4.3975F, -19.286F, -1.8665F);
        upperHalf.addChild(cube_r44);
        ClientUtil.setRotationAngle(cube_r44, -0.2419F, 0.1744F, 0.7245F);
        cube_r44.texOffs(5, 0).addBox(-1.4543F, -1.9443F, -1.0847F, 3.0F, 4.0F, 1.0F, 0.0F, false);

        cube_r45 = new ModelRenderer(this);
        cube_r45.setPos(-5.25F, -20.35F, -2.0F);
        upperHalf.addChild(cube_r45);
        ClientUtil.setRotationAngle(cube_r45, -0.3147F, 0.1053F, 0.0689F);
        cube_r45.texOffs(9, 3).addBox(-1.5F, -1.5F, -0.5F, 4.0F, 3.0F, 1.0F, 0.0F, false);

        cube_r46 = new ModelRenderer(this);
        cube_r46.setPos(-5.25F, -20.35F, -2.3F);
        upperHalf.addChild(cube_r46);
        ClientUtil.setRotationAngle(cube_r46, 0.3942F, -0.767F, -0.5787F);
        cube_r46.texOffs(6, 8).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        cube_r47 = new ModelRenderer(this);
        cube_r47.setPos(4.3975F, -19.286F, -1.8665F);
        upperHalf.addChild(cube_r47);
        ClientUtil.setRotationAngle(cube_r47, 0.5318F, -0.3653F, 1.0428F);
        cube_r47.texOffs(6, 8).addBox(-1.06F, -1.3447F, -0.8181F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        cube_r48 = new ModelRenderer(this);
        cube_r48.setPos(-0.2147F, -17.66F, -3.0913F);
        upperHalf.addChild(cube_r48);
        ClientUtil.setRotationAngle(cube_r48, 0.0953F, -1.1343F, 1.5618F);
        cube_r48.texOffs(7, 2).addBox(-1.0961F, -0.9227F, -0.777F, 2.0F, 2.0F, 5.0F, 0.0F, true);

        lowerHalf = new ModelRenderer(this);
        lowerHalf.setPos(0.0F, 24.0F, 0.0F);
        

        cube_r49 = new ModelRenderer(this);
        cube_r49.setPos(4.927F, -13.285F, -2.4268F);
        lowerHalf.addChild(cube_r49);
        ClientUtil.setRotationAngle(cube_r49, -2.6233F, -0.8447F, 2.4411F);
        cube_r49.texOffs(4, 0).addBox(-1.0F, -2.5F, -1.5F, 5.0F, 5.0F, 3.0F, 0.0F, false);

        cube_r50 = new ModelRenderer(this);
        cube_r50.setPos(5.3657F, -12.6431F, 0.2949F);
        lowerHalf.addChild(cube_r50);
        ClientUtil.setRotationAngle(cube_r50, 3.1183F, 0.3786F, -2.8293F);
        cube_r50.texOffs(3, 2).addBox(-2.5F, -2.5F, -3.5F, 4.0F, 5.0F, 5.0F, 0.0F, false);

        cube_r51 = new ModelRenderer(this);
        cube_r51.setPos(-5.487F, -8.8635F, -0.916F);
        lowerHalf.addChild(cube_r51);
        ClientUtil.setRotationAngle(cube_r51, 0.4442F, 1.0881F, -0.1495F);
        cube_r51.texOffs(11, 15).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 5.0F, 0.0F, true);

        cube_r52 = new ModelRenderer(this);
        cube_r52.setPos(3.7723F, -8.425F, 0.2908F);
        lowerHalf.addChild(cube_r52);
        ClientUtil.setRotationAngle(cube_r52, -0.0851F, 0.108F, -0.3206F);
        cube_r52.texOffs(11, 15).addBox(-3.5F, -1.5F, -3.5F, 7.0F, 4.0F, 5.0F, 0.0F, false);

        cube_r53 = new ModelRenderer(this);
        cube_r53.setPos(5.4758F, -4.0938F, -4.2116F);
        lowerHalf.addChild(cube_r53);
        ClientUtil.setRotationAngle(cube_r53, -0.3099F, -0.7723F, 0.4415F);
        cube_r53.texOffs(16, 18).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, 0.0F, false);

        cube_r54 = new ModelRenderer(this);
        cube_r54.setPos(-1.5F, -3.95F, -4.45F);
        lowerHalf.addChild(cube_r54);
        ClientUtil.setRotationAngle(cube_r54, -0.886F, -1.1655F, 0.7328F);
        cube_r54.texOffs(9, 10).addBox(-2.5F, -1.5F, -0.5F, 4.0F, 3.0F, 4.0F, 0.0F, false);

        cube_r55 = new ModelRenderer(this);
        cube_r55.setPos(4.352F, -12.1823F, -2.8899F);
        lowerHalf.addChild(cube_r55);
        ClientUtil.setRotationAngle(cube_r55, -2.4363F, -0.0266F, 2.7661F);
        cube_r55.texOffs(9, 8).addBox(-0.5952F, -2.25F, -1.5765F, 1.0F, 4.5F, 2.0F, 0.0F, false);

        cube_r56 = new ModelRenderer(this);
        cube_r56.setPos(4.352F, -12.1823F, -2.8899F);
        lowerHalf.addChild(cube_r56);
        ClientUtil.setRotationAngle(cube_r56, -2.3402F, -0.4461F, 2.3697F);
        cube_r56.texOffs(9, 7).addBox(-0.7295F, -2.25F, 0.0374F, 1.0F, 4.5F, 1.0F, 0.0F, false);

        cube_r57 = new ModelRenderer(this);
        cube_r57.setPos(-3.5134F, -12.4487F, -3.5369F);
        lowerHalf.addChild(cube_r57);
        ClientUtil.setRotationAngle(cube_r57, -2.2894F, 0.1884F, -2.6274F);
        cube_r57.texOffs(12, 9).addBox(-0.1585F, -2.5F, -2.7745F, 1.0F, 5.0F, 3.0F, 0.0F, false);

        cube_r58 = new ModelRenderer(this);
        cube_r58.setPos(-1.3033F, -12.8553F, -4.4911F);
        lowerHalf.addChild(cube_r58);
        ClientUtil.setRotationAngle(cube_r58, -1.6711F, 0.733F, -1.4169F);
        cube_r58.texOffs(20, 9).addBox(-0.1585F, -2.5F, -0.2255F, 1.0F, 5.0F, 3.0F, 0.0F, false);

        cube_r59 = new ModelRenderer(this);
        cube_r59.setPos(-1.3033F, -12.8553F, -4.4911F);
        lowerHalf.addChild(cube_r59);
        ClientUtil.setRotationAngle(cube_r59, -2.0656F, 0.5731F, -2.0506F);
        cube_r59.texOffs(14, 10).addBox(-0.1585F, -2.5F, -2.7745F, 1.0F, 5.0F, 3.0F, 0.0F, false);

        cube_r60 = new ModelRenderer(this);
        cube_r60.setPos(-0.2487F, -15.4312F, -3.2798F);
        lowerHalf.addChild(cube_r60);
        ClientUtil.setRotationAngle(cube_r60, -0.2209F, -0.121F, 2.3321F);
        cube_r60.texOffs(9, 11).addBox(-1.3976F, 0.1016F, -0.2515F, 5.0F, 1.55F, 1.0F, 0.0F, true);

        cube_r61 = new ModelRenderer(this);
        cube_r61.setPos(-0.2487F, -15.4312F, -3.2798F);
        lowerHalf.addChild(cube_r61);
        ClientUtil.setRotationAngle(cube_r61, 0.3899F, -0.1503F, 1.0645F);
        cube_r61.texOffs(6, 6).addBox(-1.853F, -2.1616F, -0.1681F, 4.4F, 1.75F, 1.0F, 0.0F, true);

        cube_r62 = new ModelRenderer(this);
        cube_r62.setPos(3.25F, -5.65F, -5.5F);
        lowerHalf.addChild(cube_r62);
        ClientUtil.setRotationAngle(cube_r62, -2.6521F, -1.0764F, 2.7163F);
        cube_r62.texOffs(14, 6).addBox(-1.5F, -1.5F, -1.5F, 6.0F, 3.0F, 5.0F, 0.0F, false);

        cube_r63 = new ModelRenderer(this);
        cube_r63.setPos(-2.25F, -6.65F, -4.5F);
        lowerHalf.addChild(cube_r63);
        ClientUtil.setRotationAngle(cube_r63, -0.5369F, -1.1655F, 0.7328F);
        cube_r63.texOffs(20, 0).addBox(-2.5F, -1.5F, -2.5F, 4.0F, 3.0F, 6.0F, 0.0F, false);

        cube_r64 = new ModelRenderer(this);
        cube_r64.setPos(1.9871F, -13.0372F, -4.96F);
        lowerHalf.addChild(cube_r64);
        ClientUtil.setRotationAngle(cube_r64, -2.3853F, -0.1654F, 2.5292F);
        cube_r64.texOffs(5, 13).addBox(-1.4012F, -2.5F, -2.2313F, 1.0F, 5.0F, 3.0F, 0.0F, false);

        cube_r65 = new ModelRenderer(this);
        cube_r65.setPos(2.2885F, -13.0099F, -4.7884F);
        lowerHalf.addChild(cube_r65);
        ClientUtil.setRotationAngle(cube_r65, -1.704F, -0.819F, 1.2939F);
        cube_r65.texOffs(9, 13).addBox(-1.4012F, -3.5F, -0.7687F, 1.0F, 6.0F, 3.0F, 0.0F, false);

        cube_r66 = new ModelRenderer(this);
        cube_r66.setPos(-0.25F, -7.65F, -1.0F);
        lowerHalf.addChild(cube_r66);
        ClientUtil.setRotationAngle(cube_r66, -1.248F, -1.4329F, 1.2509F);
        cube_r66.texOffs(13, 20).addBox(-2.5F, -8.5F, -3.5F, 6.0F, 12.0F, 7.0F, 0.0F, false);

        cube_r67 = new ModelRenderer(this);
        cube_r67.setPos(-1.0F, -3.25F, -3.6F);
        lowerHalf.addChild(cube_r67);
        ClientUtil.setRotationAngle(cube_r67, 0.417F, -1.0197F, -0.4213F);
        cube_r67.texOffs(9, 12).addBox(-2.5F, -1.5F, -3.5F, 6.0F, 5.0F, 7.0F, 0.0F, false);

        cube_r68 = new ModelRenderer(this);
        cube_r68.setPos(0.2186F, -4.2232F, -7.2086F);
        lowerHalf.addChild(cube_r68);
        ClientUtil.setRotationAngle(cube_r68, 0.4609F, -0.6966F, 0.0098F);
        cube_r68.texOffs(13, 14).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);

        cube_r69 = new ModelRenderer(this);
        cube_r69.setPos(-0.65F, -3.35F, -4.0F);
        lowerHalf.addChild(cube_r69);
        ClientUtil.setRotationAngle(cube_r69, -0.0227F, -0.8711F, 0.015F);
        cube_r69.texOffs(12, 14).addBox(-3.5F, 2.5F, -1.5F, 2.0F, 1.0F, 2.0F, 0.0F, false);

        cube_r70 = new ModelRenderer(this);
        cube_r70.setPos(-3.4F, -3.1F, -4.5F);
        lowerHalf.addChild(cube_r70);
        ClientUtil.setRotationAngle(cube_r70, -0.0936F, -0.5176F, 0.1004F);
        cube_r70.texOffs(16, 12).addBox(-3.5F, -0.5F, -1.5F, 5.0F, 4.0F, 5.0F, 0.0F, false);

        cube_r71 = new ModelRenderer(this);
        cube_r71.setPos(2.75F, -1.4F, 4.0F);
        lowerHalf.addChild(cube_r71);
        ClientUtil.setRotationAngle(cube_r71, -0.1268F, -0.7371F, 0.0587F);
        cube_r71.texOffs(9, 13).addBox(-0.5F, -3.5F, -3.5F, 4.0F, 5.0F, 7.0F, 0.0F, false);

        cube_r72 = new ModelRenderer(this);
        cube_r72.setPos(-4.0542F, -6.975F, 0.9872F);
        lowerHalf.addChild(cube_r72);
        ClientUtil.setRotationAngle(cube_r72, -0.2034F, -0.978F, 0.5399F);
        cube_r72.texOffs(10, 10).addBox(-2.0F, -0.5F, 1.5F, 2.0F, 2.0F, 2.0F, 0.0F, false);

        cube_r73 = new ModelRenderer(this);
        cube_r73.setPos(-3.2575F, -12.9546F, 3.1415F);
        lowerHalf.addChild(cube_r73);
        ClientUtil.setRotationAngle(cube_r73, 0.4444F, -0.8721F, -0.234F);
        cube_r73.texOffs(15, 11).addBox(-2.0F, -2.0F, 0.5F, 4.0F, 4.0F, 3.0F, 0.0F, false);

        cube_r74 = new ModelRenderer(this);
        cube_r74.setPos(-1.8042F, -12.475F, 3.7372F);
        lowerHalf.addChild(cube_r74);
        ClientUtil.setRotationAngle(cube_r74, 0.1569F, -0.9413F, -0.0438F);
        cube_r74.texOffs(4, 5).addBox(-2.0F, -5.5F, -3.5F, 5.0F, 8.0F, 7.0F, 0.0F, false);

        cube_r75 = new ModelRenderer(this);
        cube_r75.setPos(3.9458F, -9.475F, 6.9872F);
        lowerHalf.addChild(cube_r75);
        ClientUtil.setRotationAngle(cube_r75, 0.1576F, -1.199F, 0.0163F);
        cube_r75.texOffs(8, 8).addBox(-2.0F, -0.5F, -0.5F, 3.0F, 3.0F, 4.0F, 0.0F, false);

        cube_r76 = new ModelRenderer(this);
        cube_r76.setPos(2.1958F, -5.725F, 6.7372F);
        lowerHalf.addChild(cube_r76);
        ClientUtil.setRotationAngle(cube_r76, -0.1601F, -0.4721F, 0.2655F);
        cube_r76.texOffs(9, 9).addBox(-2.0F, -0.5F, 0.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

        cube_r77 = new ModelRenderer(this);
        cube_r77.setPos(-0.9578F, -8.4083F, 7.4336F);
        lowerHalf.addChild(cube_r77);
        ClientUtil.setRotationAngle(cube_r77, 0.6043F, -0.6785F, -0.2158F);
        cube_r77.texOffs(9, 9).addBox(-2.0F, -2.0F, -1.5F, 4.0F, 4.0F, 3.0F, 0.0F, false);

        cube_r78 = new ModelRenderer(this);
        cube_r78.setPos(-1.8042F, -7.475F, 5.9872F);
        lowerHalf.addChild(cube_r78);
        ClientUtil.setRotationAngle(cube_r78, -0.5386F, -0.8408F, 0.3432F);
        cube_r78.texOffs(9, 7).addBox(-2.0F, -2.5F, 0.5F, 4.0F, 5.0F, 3.0F, 0.0F, false);

        cube_r79 = new ModelRenderer(this);
        cube_r79.setPos(-4.5542F, -7.475F, 3.4872F);
        lowerHalf.addChild(cube_r79);
        ClientUtil.setRotationAngle(cube_r79, 0.2708F, -1.0123F, -0.0288F);
        cube_r79.texOffs(21, 9).addBox(-2.0F, -2.5F, 0.5F, 4.0F, 5.0F, 3.0F, 0.0F, false);

        cube_r80 = new ModelRenderer(this);
        cube_r80.setPos(3.794F, -0.6307F, 7.6691F);
        lowerHalf.addChild(cube_r80);
        ClientUtil.setRotationAngle(cube_r80, -0.4406F, -1.0584F, 0.0765F);
        cube_r80.texOffs(10, 9).addBox(-2.0F, -0.5F, -1.0F, 3.0F, 1.0F, 3.0F, 0.0F, false);

        cube_r81 = new ModelRenderer(this);
        cube_r81.setPos(-1.456F, -0.4307F, 7.6691F);
        lowerHalf.addChild(cube_r81);
        ClientUtil.setRotationAngle(cube_r81, 0.0695F, -0.7778F, -0.0922F);
        cube_r81.texOffs(12, 10).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);

        cube_r82 = new ModelRenderer(this);
        cube_r82.setPos(-4.3042F, -2.625F, 6.2372F);
        lowerHalf.addChild(cube_r82);
        ClientUtil.setRotationAngle(cube_r82, -0.1571F, -1.0815F, 0.1578F);
        cube_r82.texOffs(12, 10).addBox(0.0F, 1.5F, 0.5F, 2.0F, 1.0F, 2.0F, 0.0F, false);

        cube_r83 = new ModelRenderer(this);
        cube_r83.setPos(-7.7856F, -0.4913F, 1.2271F);
        lowerHalf.addChild(cube_r83);
        ClientUtil.setRotationAngle(cube_r83, -0.3339F, -1.0646F, 0.2985F);
        cube_r83.texOffs(12, 10).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);

        cube_r84 = new ModelRenderer(this);
        cube_r84.setPos(-5.0542F, -2.225F, 6.2372F);
        lowerHalf.addChild(cube_r84);
        ClientUtil.setRotationAngle(cube_r84, 2.3154F, -1.3188F, -2.2679F);
        cube_r84.texOffs(12, 10).addBox(0.0F, 0.5F, 0.5F, 2.0F, 2.0F, 2.0F, 0.0F, false);

        cube_r85 = new ModelRenderer(this);
        cube_r85.setPos(-5.5542F, -2.225F, 4.2372F);
        lowerHalf.addChild(cube_r85);
        ClientUtil.setRotationAngle(cube_r85, -0.5236F, -1.4201F, 0.6166F);
        cube_r85.texOffs(9, 9).addBox(-2.0F, -2.5F, 0.5F, 4.0F, 5.0F, 3.0F, 0.0F, false);

        cube_r86 = new ModelRenderer(this);
        cube_r86.setPos(2.5815F, -11.5F, 4.7761F);
        lowerHalf.addChild(cube_r86);
        ClientUtil.setRotationAngle(cube_r86, -2.8382F, -1.4074F, -3.1339F);
        cube_r86.texOffs(7, 3).addBox(-1.0F, -2.5F, -2.5F, 6.0F, 4.0F, 6.0F, 0.0F, false);

        cube_r87 = new ModelRenderer(this);
        cube_r87.setPos(6.5815F, -10.0F, 4.5261F);
        lowerHalf.addChild(cube_r87);
        ClientUtil.setRotationAngle(cube_r87, -2.7609F, -0.7802F, 2.6414F);
        cube_r87.texOffs(8, 9).addBox(-2.0F, -2.5F, -1.5F, 5.0F, 4.0F, 5.0F, 0.0F, false);

        cube_r88 = new ModelRenderer(this);
        cube_r88.setPos(7.0815F, -9.0F, 1.0261F);
        lowerHalf.addChild(cube_r88);
        ClientUtil.setRotationAngle(cube_r88, -1.3423F, -1.0673F, 2.2032F);
        cube_r88.texOffs(8, 9).addBox(-2.0F, -2.5F, -1.5F, 5.0F, 4.0F, 5.0F, 0.0F, false);

        cube_r89 = new ModelRenderer(this);
        cube_r89.setPos(5.5815F, -5.5F, 6.5261F);
        lowerHalf.addChild(cube_r89);
        ClientUtil.setRotationAngle(cube_r89, -2.4034F, -0.8231F, -3.0838F);
        cube_r89.texOffs(7, 8).addBox(-2.0F, -1.5F, -2.5F, 5.0F, 3.0F, 6.0F, 0.0F, false);

        cube_r90 = new ModelRenderer(this);
        cube_r90.setPos(5.5815F, -1.5F, 6.5261F);
        lowerHalf.addChild(cube_r90);
        ClientUtil.setRotationAngle(cube_r90, -3.0107F, -1.2217F, 3.1416F);
        cube_r90.texOffs(9, 10).addBox(-2.0F, -1.5F, -2.5F, 4.0F, 3.0F, 4.0F, 0.0F, false);

        cube_r91 = new ModelRenderer(this);
        cube_r91.setPos(-3.0F, -3.475F, 5.0F);
        lowerHalf.addChild(cube_r91);
        ClientUtil.setRotationAngle(cube_r91, -0.2409F, -0.9001F, 0.1395F);
        cube_r91.texOffs(9, 11).addBox(-3.5F, -2.5F, -1.5F, 7.0F, 6.0F, 5.0F, 0.0F, false);

        cube_r92 = new ModelRenderer(this);
        cube_r92.setPos(-1.0F, -3.2F, 4.5F);
        lowerHalf.addChild(cube_r92);
        ClientUtil.setRotationAngle(cube_r92, 0.0873F, -0.3054F, 0.0F);
        cube_r92.texOffs(10, 13).addBox(-2.5F, -7.5F, -3.5F, 6.0F, 11.0F, 7.0F, 0.0F, false);

        cube_r93 = new ModelRenderer(this);
        cube_r93.setPos(-5.0F, -3.4F, -1.0F);
        lowerHalf.addChild(cube_r93);
        ClientUtil.setRotationAngle(cube_r93, -0.054F, 0.3892F, -0.1415F);
        cube_r93.texOffs(1, 0).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, 0.0F, false);

        cube_r94 = new ModelRenderer(this);
        cube_r94.setPos(5.4786F, -1.3F, -5.2866F);
        lowerHalf.addChild(cube_r94);
        ClientUtil.setRotationAngle(cube_r94, -0.2128F, -1.0834F, 0.0925F);
        cube_r94.texOffs(9, 11).addBox(-2.5F, -1.5F, -1.5F, 5.0F, 3.0F, 5.0F, 0.0F, false);

        cube_r95 = new ModelRenderer(this);
        cube_r95.setPos(7.8706F, -0.1283F, -4.1168F);
        lowerHalf.addChild(cube_r95);
        ClientUtil.setRotationAngle(cube_r95, -0.1662F, 0.983F, -0.2952F);
        cube_r95.texOffs(14, 12).addBox(-1.0F, -0.6F, -1.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);

        cube_r96 = new ModelRenderer(this);
        cube_r96.setPos(7.1206F, -0.3283F, 4.8832F);
        lowerHalf.addChild(cube_r96);
        ClientUtil.setRotationAngle(cube_r96, -0.1662F, 0.983F, -0.2952F);
        cube_r96.texOffs(14, 12).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);

        cube_r97 = new ModelRenderer(this);
        cube_r97.setPos(5.5587F, -1.6588F, 0.6694F);
        lowerHalf.addChild(cube_r97);
        ClientUtil.setRotationAngle(cube_r97, 0.1223F, 0.29F, -0.184F);
        cube_r97.texOffs(14, 12).addBox(0.5F, -1.0F, 0.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);

        cube_r98 = new ModelRenderer(this);
        cube_r98.setPos(6.8087F, -0.9088F, 1.1694F);
        lowerHalf.addChild(cube_r98);
        ClientUtil.setRotationAngle(cube_r98, -0.0702F, 0.3346F, 0.18F);
        cube_r98.texOffs(9, 10).addBox(-2.5F, -1.0F, -2.0F, 5.0F, 2.0F, 4.0F, 0.0F, false);

        cube_r99 = new ModelRenderer(this);
        cube_r99.setPos(4.0723F, -3.425F, -0.7092F);
        lowerHalf.addChild(cube_r99);
        ClientUtil.setRotationAngle(cube_r99, -0.0406F, -0.4346F, 0.0962F);
        cube_r99.texOffs(9, 13).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, 0.0F, false);

        cube_r100 = new ModelRenderer(this);
        cube_r100.setPos(-2.15F, -11.2651F, -6.636F);
        lowerHalf.addChild(cube_r100);
        ClientUtil.setRotationAngle(cube_r100, -0.1454F, 0.4214F, -0.222F);
        cube_r100.texOffs(8, 6).addBox(-1.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);

        cube_r101 = new ModelRenderer(this);
        cube_r101.setPos(1.1494F, -11.197F, -7.2923F);
        lowerHalf.addChild(cube_r101);
        ClientUtil.setRotationAngle(cube_r101, -0.0389F, 0.399F, -0.2421F);
        cube_r101.texOffs(8, 6).addBox(-1.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);

        cube_r102 = new ModelRenderer(this);
        cube_r102.setPos(-0.6477F, -10.8762F, -6.5697F);
        lowerHalf.addChild(cube_r102);
        ClientUtil.setRotationAngle(cube_r102, -0.0168F, -0.0775F, 0.2906F);
        cube_r102.texOffs(8, 6).addBox(-1.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);

        cube_r103 = new ModelRenderer(this);
        cube_r103.setPos(-4.4421F, -10.6842F, -5.1124F);
        lowerHalf.addChild(cube_r103);
        ClientUtil.setRotationAngle(cube_r103, -0.1772F, 0.723F, -0.2801F);
        cube_r103.texOffs(9, 6).addBox(-0.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);

        cube_r104 = new ModelRenderer(this);
        cube_r104.setPos(-5.4296F, -9.8726F, -4.2717F);
        lowerHalf.addChild(cube_r104);
        ClientUtil.setRotationAngle(cube_r104, -0.4978F, 0.7758F, -0.4427F);
        cube_r104.texOffs(8, 5).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 2.0F, 0.0F, false);

        cube_r105 = new ModelRenderer(this);
        cube_r105.setPos(-2.1875F, -9.5652F, -6.97F);
        lowerHalf.addChild(cube_r105);
        ClientUtil.setRotationAngle(cube_r105, -0.0601F, 0.0857F, 0.053F);
        cube_r105.texOffs(3, 7).addBox(-1.0F, -0.5F, -0.5F, 3.0F, 2.0F, 3.0F, 0.0F, false);

        cube_r106 = new ModelRenderer(this);
        cube_r106.setPos(-4.7027F, -9.9043F, -5.7403F);
        lowerHalf.addChild(cube_r106);
        ClientUtil.setRotationAngle(cube_r106, 0.0328F, 0.485F, 0.1633F);
        cube_r106.texOffs(1, 0).addBox(-1.0F, 0.0F, -0.5F, 3.0F, 1.0F, 3.0F, 0.15F, false);

        cube_r107 = new ModelRenderer(this);
        cube_r107.setPos(0.5645F, -9.1516F, -6.9144F);
        lowerHalf.addChild(cube_r107);
        ClientUtil.setRotationAngle(cube_r107, -0.2547F, 0.1965F, 0.1344F);
        cube_r107.texOffs(9, 3).addBox(-1.0F, -0.75F, -0.5F, 3.0F, 2.25F, 2.0F, 0.0F, false);

        cube_r108 = new ModelRenderer(this);
        cube_r108.setPos(4.1502F, -9.3721F, -5.2316F);
        lowerHalf.addChild(cube_r108);
        ClientUtil.setRotationAngle(cube_r108, 0.0345F, -1.1595F, -0.4152F);
        cube_r108.texOffs(7, 5).addBox(-1.0F, 0.25F, -0.5F, 3.0F, 2.0F, 2.0F, 0.0F, false);

        cube_r109 = new ModelRenderer(this);
        cube_r109.setPos(3.0694F, -8.7251F, -6.8108F);
        lowerHalf.addChild(cube_r109);
        ClientUtil.setRotationAngle(cube_r109, -0.3916F, -0.5993F, 0.2489F);
        cube_r109.texOffs(7, 5).addBox(-1.0F, -0.75F, -0.5F, 3.0F, 2.25F, 2.0F, 0.0F, false);

        cube_r110 = new ModelRenderer(this);
        cube_r110.setPos(2.7692F, -11.6887F, -7.4259F);
        lowerHalf.addChild(cube_r110);
        ClientUtil.setRotationAngle(cube_r110, -0.2052F, -0.1257F, -0.2664F);
        cube_r110.texOffs(8, 6).addBox(-1.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);

        cube_r111 = new ModelRenderer(this);
        cube_r111.setPos(3.8734F, -11.3739F, -6.4403F);
        lowerHalf.addChild(cube_r111);
        ClientUtil.setRotationAngle(cube_r111, -0.5244F, -0.7512F, 0.308F);
        cube_r111.texOffs(5, 1).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

        cube_r112 = new ModelRenderer(this);
        cube_r112.setPos(4.8634F, -10.4528F, -4.4917F);
        lowerHalf.addChild(cube_r112);
        ClientUtil.setRotationAngle(cube_r112, -1.1744F, -0.9446F, 1.3766F);
        cube_r112.texOffs(8, 6).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

        cube_r113 = new ModelRenderer(this);
        cube_r113.setPos(-5.487F, -11.8635F, 0.834F);
        lowerHalf.addChild(cube_r113);
        ClientUtil.setRotationAngle(cube_r113, 1.4824F, 1.1482F, 0.2277F);
        cube_r113.texOffs(12, 15).addBox(-2.0F, -2.0F, -3.5F, 4.0F, 5.0F, 5.0F, 0.0F, true);
        
        
        allCubesByParts = new HashMap<>();
        allCubes = new ArrayList<>(113);
        visibleCubes = new HashSet<>();
        addCubesFrom(upperHalf);
        addCubesFrom(lowerHalf);
        Random random = new Random();
        List<ModelRenderer.ModelBox> cubesShuffled = new ArrayList<>(allCubes);
        Collections.shuffle(cubesShuffled, random);
        
        
        texWidth = 64;
        texHeight = 64;
        
        shadow = new ModelRenderer(this);
        shadow.setPos(0.0F, 24.0F, 0.0F);
        
        cube_r1 = new ModelRenderer(this);
        cube_r1.setPos(2.3873F, -12.992F, -4.6752F);
        shadow.addChild(cube_r1);
        ClientUtil.setRotationAngle(cube_r1, -1.704F, -0.819F, 1.2939F);
        cube_r1.texOffs(28, 10).addBox(-1.6012F, -3.5F, -0.7687F, 1.0F, 4.0F, 3.0F, 0.0F, false);

        cube_r2 = new ModelRenderer(this);
        cube_r2.setPos(1.8871F, -13.0372F, -4.96F);
        shadow.addChild(cube_r2);
        ClientUtil.setRotationAngle(cube_r2, -2.3853F, -0.1654F, 2.5292F);
        cube_r2.texOffs(24, 10).addBox(-1.6012F, -2.5F, -2.2313F, 1.0F, 5.0F, 3.0F, 0.0F, false);

        cube_r3 = new ModelRenderer(this);
        cube_r3.setPos(-3.9456F, -21.2411F, -1.8968F);
        shadow.addChild(cube_r3);
        ClientUtil.setRotationAngle(cube_r3, -0.3001F, 0.3473F, 0.4623F);
        cube_r3.texOffs(7, 5).addBox(-2.6876F, -1.25F, -0.7133F, 2.0F, 2.0F, 0.0F, 0.0F, true);

        cube_r4 = new ModelRenderer(this);
        cube_r4.setPos(-3.4199F, -12.5011F, -3.6079F);
        shadow.addChild(cube_r4);
        ClientUtil.setRotationAngle(cube_r4, -2.2894F, 0.1884F, -2.6274F);
        cube_r4.texOffs(13, 0).addBox(1.0415F, -2.25F, -2.7745F, 0.0F, 2.0F, 3.0F, 0.0F, false);

        cube_r5 = new ModelRenderer(this);
        cube_r5.setPos(-1.4037F, -12.8575F, -4.4657F);
        shadow.addChild(cube_r5);
        ClientUtil.setRotationAngle(cube_r5, -1.6711F, 0.733F, -1.4169F);
        cube_r5.texOffs(29, 0).addBox(1.0415F, -2.25F, -0.2255F, 0.0F, 3.0F, 3.0F, 0.0F, false);

        cube_r6 = new ModelRenderer(this);
        cube_r6.setPos(-1.2588F, -12.8569F, -4.5045F);
        shadow.addChild(cube_r6);
        ClientUtil.setRotationAngle(cube_r6, -2.0897F, 0.5519F, -2.0957F);
        cube_r6.texOffs(22, 1).addBox(1.0415F, -2.25F, -1.7745F, 0.0F, 3.0F, 2.0F, 0.0F, false);

        cube_r7 = new ModelRenderer(this);
        cube_r7.setPos(-2.564F, -17.1914F, -3.1438F);
        shadow.addChild(cube_r7);
        ClientUtil.setRotationAngle(cube_r7, 0.0467F, 0.3204F, 0.7819F);
        cube_r7.texOffs(1, 12).addBox(-0.5F, -1.5F, -0.3F, 1.0F, 3.0F, 0.0F, 0.0F, true);

        cube_r8 = new ModelRenderer(this);
        cube_r8.setPos(-1.6734F, -20.9604F, -1.9949F);
        shadow.addChild(cube_r8);
        ClientUtil.setRotationAngle(cube_r8, 0.0922F, 0.3246F, 1.8211F);
        cube_r8.texOffs(1, 19).addBox(-0.801F, -1.25F, -0.583F, 4.0F, 1.0F, 0.0F, 0.0F, true);

        cube_r9 = new ModelRenderer(this);
        cube_r9.setPos(-3.9456F, -21.2411F, -1.8968F);
        shadow.addChild(cube_r9);
        ClientUtil.setRotationAngle(cube_r9, -0.2864F, 0.1801F, 0.5145F);
        cube_r9.texOffs(1, 8).addBox(-0.801F, -1.25F, -0.583F, 5.0F, 1.0F, 0.0F, 0.0F, true);

        cube_r10 = new ModelRenderer(this);
        cube_r10.setPos(3.8693F, -20.9731F, -1.9272F);
        shadow.addChild(cube_r10);
        ClientUtil.setRotationAngle(cube_r10, -0.3755F, -0.269F, -0.3171F);
        cube_r10.texOffs(1, 5).addBox(0.7067F, -1.25F, -0.661F, 2.0F, 2.0F, 0.0F, 0.0F, false);

        cube_r11 = new ModelRenderer(this);
        cube_r11.setPos(0.8882F, -15.6353F, -3.4242F);
        shadow.addChild(cube_r11);
        ClientUtil.setRotationAngle(cube_r11, 0.3186F, -0.0613F, 0.9595F);
        cube_r11.texOffs(0, 20).addBox(-2.2F, -1.025F, -0.6F, 4.4F, 2.0F, 1.0F, 0.0F, true);

        cube_r12 = new ModelRenderer(this);
        cube_r12.setPos(1.748F, -17.8068F, -3.307F);
        shadow.addChild(cube_r12);
        ClientUtil.setRotationAngle(cube_r12, -0.2221F, -0.2459F, -0.6509F);
        cube_r12.texOffs(4, 13).addBox(-0.5F, -1.5F, -0.3F, 1.0F, 3.0F, 0.0F, 0.0F, false);

        cube_r13 = new ModelRenderer(this);
        cube_r13.setPos(1.0757F, -20.6782F, -2.1176F);
        shadow.addChild(cube_r13);
        ClientUtil.setRotationAngle(cube_r13, 0.0181F, -0.3923F, -1.618F);
        cube_r13.texOffs(1, 19).addBox(-2.4F, -0.5F, -0.475F, 3.0F, 1.0F, 0.0F, 0.0F, false);

        cube_r14 = new ModelRenderer(this);
        cube_r14.setPos(3.8693F, -20.9731F, -1.9272F);
        shadow.addChild(cube_r14);
        ClientUtil.setRotationAngle(cube_r14, -0.3655F, -0.147F, -0.3655F);
        cube_r14.texOffs(1, 1).addBox(-4.2131F, -1.25F, -0.5631F, 5.0F, 1.0F, 0.0F, 0.0F, false);

        cube_r15 = new ModelRenderer(this);
        cube_r15.setPos(-5.224F, -20.2983F, -2.1751F);
        shadow.addChild(cube_r15);
        ClientUtil.setRotationAngle(cube_r15, -0.1637F, 0.2928F, 0.793F);
        cube_r15.texOffs(7, 13).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, -0.1F, true);

        cube_r16 = new ModelRenderer(this);
        cube_r16.setPos(4.7567F, -19.792F, -2.5243F);
        shadow.addChild(cube_r16);
        ClientUtil.setRotationAngle(cube_r16, -0.1795F, -0.2382F, -0.8036F);
        cube_r16.texOffs(7, 13).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, -0.1F, false);

        cube_r17 = new ModelRenderer(this);
        cube_r17.setPos(-0.9248F, -9.3861F, -5.3675F);
        shadow.addChild(cube_r17);
        ClientUtil.setRotationAngle(cube_r17, -0.3668F, -1.1986F, 0.3494F);
        cube_r17.texOffs(13, 8).addBox(-0.5F, -3.0F, 0.5F, 1.0F, 4.0F, 3.0F, 0.0F, false);

        cube_r18 = new ModelRenderer(this);
        cube_r18.setPos(1.9681F, -9.4237F, -5.7792F);
        shadow.addChild(cube_r18);
        ClientUtil.setRotationAngle(cube_r18, -0.6472F, -1.3528F, 0.6415F);
        cube_r18.texOffs(18, 10).addBox(-0.5F, -3.5F, 1.5F, 1.0F, 4.5F, 2.0F, 0.0F, false);

        cube_r19 = new ModelRenderer(this);
        cube_r19.setPos(3.696F, -9.533F, -6.6861F);
        shadow.addChild(cube_r19);
        ClientUtil.setRotationAngle(cube_r19, -0.3668F, -1.1986F, 0.3494F);
        cube_r19.texOffs(13, 9).addBox(-0.5F, -3.275F, 1.5F, 1.0F, 4.0F, 2.0F, 0.0F, false);

        cube_r20 = new ModelRenderer(this);
        cube_r20.setPos(4.4742F, -9.1796F, -4.0333F);
        shadow.addChild(cube_r20);
        ClientUtil.setRotationAngle(cube_r20, -2.9397F, -0.8629F, 2.993F);
        cube_r20.texOffs(17, 6).addBox(-0.5F, -2.4F, -0.5F, 1.0F, 3.4F, 4.0F, 0.0F, false);
    }
    
    private void addCubesFrom(ModelRenderer modelPart) {
        allCubesByParts.put(modelPart, new ArrayList<>(modelPart.cubes));
        allCubes.addAll(modelPart.cubes);
        for (ModelRenderer child : modelPart.children) {
            addCubesFrom(child);
        }
    }
    
    @Override
    public void setupAnim(AngeloRockEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        float yRot = pNetHeadYaw * MathUtil.DEG_TO_RAD;
        upperHalf.yRot = yRot;
        lowerHalf.yRot = yRot;
        shadow.yRot = yRot;
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
            visibleCubes.clear();
            int renderParts = 1 + (int) (progress * allCubes.size());
            allCubes.stream().limit(renderParts).forEach(visibleCubes::add);
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
        shadow.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        matrixStack.popPose();
    }

}
