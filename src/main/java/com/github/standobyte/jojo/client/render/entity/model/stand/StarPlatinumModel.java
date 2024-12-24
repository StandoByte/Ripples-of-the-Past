package com.github.standobyte.jojo.client.render.entity.model.stand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.standobyte.jojo.entity.stand.stands.StarPlatinumEntity;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

//Made with Blockbench 4.8.3

public class StarPlatinumModel extends HumanoidStandModel<StarPlatinumEntity> {
    protected ModelRenderer leftShoulder;
    protected ModelRenderer rightShoulder;
    protected ModelRenderer frontFabric;
    protected ModelRenderer backFabric;

    public StarPlatinumModel() {
        super();
        
        // hair goes brrrrr
        ModelRenderer hair;
        ModelRenderer hair1;
        ModelRenderer hair2;
        ModelRenderer hair3;
        ModelRenderer hair4;
        ModelRenderer hair5;
        ModelRenderer hair6;
        ModelRenderer hair7;
        ModelRenderer hair8;
        ModelRenderer hair9;
        ModelRenderer hair10;
        ModelRenderer hair11;
        ModelRenderer hair12;
        ModelRenderer hair13;
        ModelRenderer hair14;
        ModelRenderer hair15;
        ModelRenderer hair16;
        ModelRenderer hair17;
        ModelRenderer hair18;
        ModelRenderer hair19;
        ModelRenderer hair20;
        ModelRenderer hair21;
        ModelRenderer hair22;
        ModelRenderer hair23;
        ModelRenderer hair24;
        ModelRenderer hair25;
        ModelRenderer hair26;
        ModelRenderer hair27;
        ModelRenderer hair28;
        ModelRenderer hair29;
        ModelRenderer hair30;
        ModelRenderer hair31;
        ModelRenderer hair32;
        ModelRenderer hair33;
        ModelRenderer hair34;
        ModelRenderer hair35;
        ModelRenderer hair36;
        ModelRenderer hair37;
        ModelRenderer hair38;
        ModelRenderer hair39;
        ModelRenderer hair40;
        ModelRenderer hair41;
        ModelRenderer hair42;
        ModelRenderer hair43;
        ModelRenderer hair44;
        ModelRenderer hair45;
        ModelRenderer hair46;
        ModelRenderer hair47;
        ModelRenderer hair48;

        root = new ModelRenderer(this);
        root.setPos(0.0F, 0.0F, 0.0F);
        

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        root.addChild(head);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        head.texOffs(45, 14).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 1.0F, 8.0F, 0.075F, false);
        head.texOffs(24, 0).addBox(-4.5F, -4.0F, -3.0F, 1.0F, 2.0F, 2.0F, 0.0F, false);
        head.texOffs(24, 4).addBox(3.5F, -4.0F, -3.0F, 1.0F, 2.0F, 2.0F, 0.0F, true);
        head.texOffs(5, 7).addBox(-0.5F, -0.9F, -4.0F, 1.0F, 1.0F, 0.0F, 0.1F, false);

        hair = new ModelRenderer(this);
        hair.setPos(0.0F, -8.0F, 0.0F);
        head.addChild(hair);
        

        hair1 = new ModelRenderer(this);
        hair1.setPos(-3.6F, 1.2F, -4.0F);
        hair.addChild(hair1);
        setRotationAngle(hair1, -0.1309F, -0.1571F, 0.0F);
        hair1.texOffs(44, 0).addBox(-1.0F, -3.0F, -0.5F, 2.0F, 4.0F, 2.0F, 0.0F, false);

        hair2 = new ModelRenderer(this);
        hair2.setPos(0.0F, -3.0F, -0.5F);
        hair1.addChild(hair2);
        setRotationAngle(hair2, -0.9163F, 0.0F, 0.0F);
        hair2.texOffs(36, 0).addBox(-0.9F, -9.0F, 0.0F, 2.0F, 9.0F, 2.0F, 0.0F, false);

        hair3 = new ModelRenderer(this);
        hair3.setPos(-1.8F, 1.1F, -4.0F);
        hair.addChild(hair3);
        setRotationAngle(hair3, -0.1309F, -0.096F, 0.0F);
        hair3.texOffs(44, 6).addBox(-1.0F, -3.0F, -0.5F, 2.0F, 4.0F, 2.0F, 0.0F, false);

        hair4 = new ModelRenderer(this);
        hair4.setPos(0.0F, -3.0F, -0.5F);
        hair3.addChild(hair4);
        setRotationAngle(hair4, -0.829F, 0.0F, 0.0F);
        hair4.texOffs(52, 0).addBox(-0.9F, -9.0F, 0.0F, 2.0F, 9.0F, 2.0F, 0.0F, true);

        hair5 = new ModelRenderer(this);
        hair5.setPos(0.0F, 1.0F, -4.0F);
        hair.addChild(hair5);
        setRotationAngle(hair5, -0.1309F, 0.0087F, 0.0F);
        hair5.texOffs(60, 0).addBox(-1.0F, -3.0F, -0.5F, 2.0F, 4.0F, 2.0F, 0.0F, false);

        hair6 = new ModelRenderer(this);
        hair6.setPos(0.0F, -3.0F, -0.5F);
        hair5.addChild(hair6);
        setRotationAngle(hair6, -0.7854F, 0.0F, 0.0F);
        hair6.texOffs(68, 0).addBox(-1.0F, -9.0F, 0.0F, 2.0F, 9.0F, 2.0F, 0.0F, false);

        hair7 = new ModelRenderer(this);
        hair7.setPos(1.8F, 1.2F, -4.0F);
        hair.addChild(hair7);
        setRotationAngle(hair7, -0.1309F, 0.1047F, 0.0F);
        hair7.texOffs(60, 6).addBox(-1.0F, -3.0F, -0.5F, 2.0F, 4.0F, 2.0F, 0.0F, true);

        hair8 = new ModelRenderer(this);
        hair8.setPos(0.0F, -3.0F, -0.5F);
        hair7.addChild(hair8);
        setRotationAngle(hair8, -0.8727F, 0.0F, 0.0F);
        hair8.texOffs(92, 0).addBox(-1.1F, -9.0F, 0.0F, 2.0F, 9.0F, 2.0F, 0.0F, true);

        hair9 = new ModelRenderer(this);
        hair9.setPos(3.6F, 1.3F, -4.0F);
        hair.addChild(hair9);
        setRotationAngle(hair9, -0.1309F, 0.1745F, 0.0F);
        hair9.texOffs(76, 0).addBox(-1.0F, -3.0F, -0.5F, 2.0F, 4.0F, 2.0F, 0.0F, false);

        hair10 = new ModelRenderer(this);
        hair10.setPos(0.0F, -3.0F, -0.5F);
        hair9.addChild(hair10);
        setRotationAngle(hair10, -0.9163F, 0.0F, 0.0F);
        hair10.texOffs(84, 0).addBox(-1.1F, -9.0F, 0.0F, 2.0F, 9.0F, 2.0F, 0.0F, false);

        hair11 = new ModelRenderer(this);
        hair11.setPos(-4.0F, -0.5F, -1.5F);
        hair.addChild(hair11);
        setRotationAngle(hair11, -1.2654F, -0.2531F, 0.0F);
        hair11.texOffs(36, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.0F, false);

        hair12 = new ModelRenderer(this);
        hair12.setPos(4.0F, -0.5F, -1.5F);
        hair.addChild(hair12);
        setRotationAngle(hair12, -1.2654F, 0.2531F, 0.0F);
        hair12.texOffs(36, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.0F, false);

        hair13 = new ModelRenderer(this);
        hair13.setPos(-4.0F, 1.0F, -1.5F);
        hair.addChild(hair13);
        setRotationAngle(hair13, -1.4399F, -0.2705F, 0.0F);
        hair13.texOffs(36, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.0F, false);

        hair14 = new ModelRenderer(this);
        hair14.setPos(4.0F, 1.0F, -1.5F);
        hair.addChild(hair14);
        setRotationAngle(hair14, -1.4399F, 0.2356F, 0.0F);
        hair14.texOffs(36, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 9.0F, 2.0F, 0.0F, true);

        hair15 = new ModelRenderer(this);
        hair15.setPos(-4.0F, 3.0F, -2.0F);
        hair.addChild(hair15);
        setRotationAngle(hair15, -1.5272F, -0.2095F, 0.0F);
        hair15.texOffs(68, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 9.0F, 2.0F, 0.0F, true);

        hair16 = new ModelRenderer(this);
        hair16.setPos(4.0F, 3.0F, -2.0F);
        hair.addChild(hair16);
        setRotationAngle(hair16, -1.5272F, 0.2095F, 0.0F);
        hair16.texOffs(68, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 9.0F, 2.0F, 0.0F, false);

        hair17 = new ModelRenderer(this);
        hair17.setPos(-4.0F, 5.0F, 0.0F);
        hair.addChild(hair17);
        setRotationAngle(hair17, -1.6144F, -0.1309F, 0.0F);
        hair17.texOffs(52, 2).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 7.0F, 2.0F, -0.1F, false);

        hair18 = new ModelRenderer(this);
        hair18.setPos(4.0F, 5.0F, 0.0F);
        hair.addChild(hair18);
        setRotationAngle(hair18, -1.6144F, 0.1222F, 0.0F);
        hair18.texOffs(52, 2).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 7.0F, 2.0F, -0.1F, false);

        hair19 = new ModelRenderer(this);
        hair19.setPos(-4.0F, 7.0F, 2.0F);
        hair.addChild(hair19);
        setRotationAngle(hair19, -1.7453F, -0.0611F, 0.0F);
        hair19.texOffs(44, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, -0.1F, false);

        hair20 = new ModelRenderer(this);
        hair20.setPos(4.0F, 7.0F, 2.0F);
        hair.addChild(hair20);
        setRotationAngle(hair20, -1.7453F, 0.1048F, 0.0F);
        hair20.texOffs(44, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, -0.1F, false);

        hair21 = new ModelRenderer(this);
        hair21.setPos(-3.25F, -1.5F, -1.5F);
        hair.addChild(hair21);
        setRotationAngle(hair21, -1.2131F, -0.1833F, 0.0F);
        hair21.texOffs(52, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.0F, false);

        hair22 = new ModelRenderer(this);
        hair22.setPos(-1.75F, -1.5F, -1.5F);
        hair.addChild(hair22);
        setRotationAngle(hair22, -0.9949F, 0.0087F, 0.0F);
        hair22.texOffs(53, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.1F, false);

        hair23 = new ModelRenderer(this);
        hair23.setPos(0.95F, -1.5F, -1.5F);
        hair.addChild(hair23);
        setRotationAngle(hair23, -1.0472F, 0.0873F, 0.0F);
        hair23.texOffs(52, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.1F, false);

        hair24 = new ModelRenderer(this);
        hair24.setPos(3.25F, -1.5F, -1.5F);
        hair.addChild(hair24);
        setRotationAngle(hair24, -1.1781F, 0.1571F, 0.0F);
        hair24.texOffs(52, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.1F, false);

        hair25 = new ModelRenderer(this);
        hair25.setPos(-1.7F, -1.0F, -1.1F);
        hair.addChild(hair25);
        setRotationAngle(hair25, -1.1781F, -0.1047F, 0.0F);
        hair25.texOffs(52, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.2F, false);

        hair26 = new ModelRenderer(this);
        hair26.setPos(-0.3F, -1.0F, -2.1F);
        hair.addChild(hair26);
        setRotationAngle(hair26, -1.117F, 0.0175F, 0.0F);
        hair26.texOffs(52, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 9.0F, 2.0F, 0.2F, true);

        hair27 = new ModelRenderer(this);
        hair27.setPos(1.7F, -1.0F, -2.4F);
        hair.addChild(hair27);
        setRotationAngle(hair27, -1.213F, 0.0785F, 0.0F);
        hair27.texOffs(52, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 9.0F, 2.0F, 0.2F, true);

        hair28 = new ModelRenderer(this);
        hair28.setPos(-3.5F, 0.0F, -1.3F);
        hair.addChild(hair28);
        setRotationAngle(hair28, -1.3788F, -0.1483F, 0.0F);
        hair28.texOffs(52, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.1F, false);

        hair29 = new ModelRenderer(this);
        hair29.setPos(-1.6F, -1.0F, 1.8F);
        hair.addChild(hair29);
        setRotationAngle(hair29, -1.4487F, -0.1222F, 0.0F);
        hair29.texOffs(52, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.2F, true);

        hair30 = new ModelRenderer(this);
        hair30.setPos(-0.2F, -1.0F, -1.0F);
        hair.addChild(hair30);
        setRotationAngle(hair30, -1.309F, 0.0873F, 0.0F);
        hair30.texOffs(52, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 8.0F, 2.0F, 0.3F, false);

        hair31 = new ModelRenderer(this);
        hair31.setPos(1.7F, -1.0F, 0.9F);
        hair.addChild(hair31);
        setRotationAngle(hair31, -1.405F, 0.1134F, 0.0F);
        hair31.texOffs(52, 0).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 6.0F, 2.0F, 0.2F, false);

        hair32 = new ModelRenderer(this);
        hair32.setPos(3.25F, 0.0F, -1.9F);
        hair.addChild(hair32);
        setRotationAngle(hair32, -1.3614F, 0.1309F, 0.0F);
        hair32.texOffs(52, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 9.0F, 2.0F, 0.1F, false);

        hair33 = new ModelRenderer(this);
        hair33.setPos(-3.0F, 2.0F, -2.0F);
        hair.addChild(hair33);
        setRotationAngle(hair33, -1.4399F, -0.1746F, 0.0F);
        hair33.texOffs(52, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 9.0F, 2.0F, 0.1F, false);

        hair34 = new ModelRenderer(this);
        hair34.setPos(-2.1F, 0.4F, 4.0F);
        hair.addChild(hair34);
        setRotationAngle(hair34, -1.5185F, -0.0873F, 0.0F);
        hair34.texOffs(52, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, 0.2F, false);

        hair35 = new ModelRenderer(this);
        hair35.setPos(0.0F, -1.0F, 3.0F);
        hair.addChild(hair35);
        setRotationAngle(hair35, -1.6406F, 0.0873F, 0.0F);
        hair35.texOffs(68, 0).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.3F, false);

        hair36 = new ModelRenderer(this);
        hair36.setPos(1.9F, 0.0F, 4.0F);
        hair.addChild(hair36);
        setRotationAngle(hair36, -1.693F, 0.1745F, 0.0F);
        hair36.texOffs(52, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, 0.2F, true);

        hair37 = new ModelRenderer(this);
        hair37.setPos(3.25F, 2.0F, -2.0F);
        hair.addChild(hair37);
        setRotationAngle(hair37, -1.4399F, 0.1047F, 0.0F);
        hair37.texOffs(52, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 9.0F, 2.0F, 0.1F, false);

        hair38 = new ModelRenderer(this);
        hair38.setPos(-3.5F, 4.0F, 0.0F);
        hair.addChild(hair38);
        setRotationAngle(hair38, -1.5271F, -0.0785F, 0.0F);
        hair38.texOffs(68, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 7.0F, 2.0F, 0.1F, false);

        hair39 = new ModelRenderer(this);
        hair39.setPos(-2.7F, 2.4F, 4.0F);
        hair.addChild(hair39);
        setRotationAngle(hair39, -1.693F, 0.0F, 0.0F);
        hair39.texOffs(52, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, 0.2F, true);

        hair40 = new ModelRenderer(this);
        hair40.setPos(-0.2F, 1.1F, 4.0F);
        hair.addChild(hair40);
        setRotationAngle(hair40, -1.7715F, 0.1309F, 0.0F);
        hair40.texOffs(52, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, 0.3F, true);

        hair41 = new ModelRenderer(this);
        hair41.setPos(2.1F, 2.0F, 4.0F);
        hair.addChild(hair41);
        setRotationAngle(hair41, -1.8588F, 0.1047F, 0.0F);
        hair41.texOffs(52, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, 0.2F, false);

        hair42 = new ModelRenderer(this);
        hair42.setPos(3.5F, 4.0F, 0.0F);
        hair.addChild(hair42);
        setRotationAngle(hair42, -1.5271F, 0.0873F, 0.0F);
        hair42.texOffs(52, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 7.0F, 2.0F, 0.1F, false);

        hair43 = new ModelRenderer(this);
        hair43.setPos(-0.9F, 3.3F, 4.0F);
        hair.addChild(hair43);
        setRotationAngle(hair43, -1.8065F, -0.0873F, 0.0F);
        hair43.texOffs(52, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, 0.2F, false);

        hair44 = new ModelRenderer(this);
        hair44.setPos(1.1F, 4.2F, 4.0F);
        hair.addChild(hair44);
        setRotationAngle(hair44, -1.8065F, 0.1484F, 0.0F);
        hair44.texOffs(52, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, 0.2F, false);

        hair45 = new ModelRenderer(this);
        hair45.setPos(-3.5F, 6.0F, 2.0F);
        hair.addChild(hair45);
        setRotationAngle(hair45, -1.658F, 0.0436F, 0.0F);
        hair45.texOffs(52, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.1F, false);

        hair46 = new ModelRenderer(this);
        hair46.setPos(-1.0F, 5.3F, 4.0F);
        hair.addChild(hair46);
        setRotationAngle(hair46, -1.981F, 0.0175F, 0.0F);
        hair46.texOffs(52, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, 0.1F, false);

        hair47 = new ModelRenderer(this);
        hair47.setPos(1.0F, 6.2F, 4.0F);
        hair.addChild(hair47);
        setRotationAngle(hair47, -1.8588F, 0.0611F, 0.0F);
        hair47.texOffs(68, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, 0.1F, false);

        hair48 = new ModelRenderer(this);
        hair48.setPos(3.5F, 6.0F, 2.0F);
        hair.addChild(hair48);
        setRotationAngle(hair48, -1.658F, -0.0349F, 0.0F);
        hair48.texOffs(68, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.1F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        root.addChild(body);
        

        upperPart = new ModelRenderer(this);
        upperPart.setPos(0.0F, 12.0F, 0.0F);
        body.addChild(upperPart);
        

        torso = new ModelRenderer(this);
        torso.setPos(0.0F, -12.0F, 0.0F);
        upperPart.addChild(torso);
        torso.texOffs(0, 64).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        torso.texOffs(0, 48).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.1F, false);
        torso.texOffs(20, 48).addBox(-3.5F, 1.1F, -2.0F, 7.0F, 3.0F, 1.0F, 0.475F, false);
        torso.texOffs(20, 64).addBox(-3.5F, 1.1F, -2.0F, 7.0F, 3.0F, 1.0F, 0.4F, false);
        torso.texOffs(24, 73).addBox(-2.5F, 4.0F, -2.3F, 5.0F, 6.0F, 1.0F, 0.0F, false);
        torso.texOffs(0, 16).addBox(-4.0F, -1.0F, -2.75F, 8.0F, 2.0F, 5.0F, 0.1F, false);
        torso.texOffs(13, 80).addBox(1.0F, 10.0F, -2.5F, 1.0F, 1.0F, 5.0F, -0.2F, false);
        torso.texOffs(0, 80).addBox(-2.0F, 10.0F, -2.5F, 1.0F, 1.0F, 5.0F, -0.2F, false);
        torso.texOffs(108, 107).addBox(3.75F, 10.0F, -1.5F, 1.0F, 1.5F, 3.0F, 0.0F, true);
        torso.texOffs(76, 107).addBox(-4.75F, 10.0F, -1.5F, 1.0F, 1.5F, 3.0F, 0.0F, true);

        leftShoulder = new ModelRenderer(this);
        leftShoulder.setPos(3.5F, 0.55F, 0.0F);
        torso.addChild(leftShoulder);
        setRotationAngle(leftShoulder, 0.0F, 0.0F, 0.0436F);
        leftShoulder.texOffs(23, 23).addBox(-0.65F, -1.8F, -2.75F, 6.0F, 2.0F, 5.0F, 0.15F, true);
        leftShoulder.texOffs(24, 25).addBox(-0.5F, -1.66F, -2.6F, 1.0F, 2.0F, 1.0F, 0.3F, true);
        leftShoulder.texOffs(17, 25).addBox(-0.5F, -1.66F, 1.1F, 1.0F, 2.0F, 1.0F, 0.3F, true);

        rightShoulder = new ModelRenderer(this);
        rightShoulder.setPos(-3.5F, 0.55F, 0.0F);
        torso.addChild(rightShoulder);
        setRotationAngle(rightShoulder, 0.0F, 0.0F, -0.0436F);
        rightShoulder.texOffs(0, 23).addBox(-5.35F, -1.8F, -2.75F, 6.0F, 2.0F, 5.0F, 0.15F, false);
        rightShoulder.texOffs(24, 25).addBox(-0.5F, -1.66F, -2.6F, 1.0F, 2.0F, 1.0F, 0.3F, false);
        rightShoulder.texOffs(17, 25).addBox(-0.5F, -1.66F, 1.1F, 1.0F, 2.0F, 1.0F, 0.3F, false);

        frontFabric = new ModelRenderer(this);
        frontFabric.setPos(0.0F, 10.5F, -2.15F);
        torso.addChild(frontFabric);
        frontFabric.texOffs(0, 86).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);

        backFabric = new ModelRenderer(this);
        backFabric.setPos(0.0F, 10.5F, 2.15F);
        torso.addChild(backFabric);
        backFabric.texOffs(0, 92).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);

        leftArmXRot = new ModelRenderer(this);
        leftArmXRot.setPos(6.0F, -10.0F, 0.0F);
        upperPart.addChild(leftArmXRot);
        

        leftArmBone = new ModelRenderer(this);
        leftArmBone.setPos(0.0F, 0.0F, 0.0F);
        leftArmXRot.addChild(leftArmBone);
        leftArmBone.texOffs(32, 108).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        leftArmBone.texOffs(50, 105).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 6.0F, 4.0F, 0.1F, true);
        leftArmBone.texOffs(44, 109).addBox(-1.0F, 3.0F, 1.5F, 2.0F, 2.0F, 1.0F, 0.0F, false);

        leftArmJoint = new ModelRenderer(this);
        leftArmJoint.setPos(0.0F, 4.0F, 0.0F);
        leftArmBone.addChild(leftArmJoint);
        leftArmJoint.texOffs(32, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

        leftForeArm = new ModelRenderer(this);
        leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        leftArmBone.addChild(leftForeArm);
        leftForeArm.texOffs(32, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        leftForeArm.texOffs(48, 95).addBox(-2.0F, -0.1F, -2.0F, 4.0F, 2.0F, 4.0F, 0.185F, false);
        leftForeArm.texOffs(48, 95).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 3.0F, 4.0F, 0.15F, false);
        leftForeArm.texOffs(48, 114).addBox(1.6F, 3.2F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, true);
        leftForeArm.texOffs(48, 116).addBox(1.6F, 3.8F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2F, true);
        leftForeArm.texOffs(48, 119).addBox(1.6F, 4.4F, -1.5F, 1.0F, 1.0F, 3.0F, -0.2F, true);
        leftForeArm.texOffs(48, 123).addBox(1.6F, 5.0F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, true);

        rightArmXRot = new ModelRenderer(this);
        rightArmXRot.setPos(-6.0F, -10.0F, 0.0F);
        upperPart.addChild(rightArmXRot);
        

        rightArmBone = new ModelRenderer(this);
        rightArmBone.setPos(0.0F, 0.0F, 0.0F);
        rightArmXRot.addChild(rightArmBone);
        rightArmBone.texOffs(0, 108).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        rightArmBone.texOffs(18, 105).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 6.0F, 4.0F, 0.1F, false);
        rightArmBone.texOffs(12, 109).addBox(-1.0F, 3.0F, 1.5F, 2.0F, 2.0F, 1.0F, 0.0F, false);

        rightArmJoint = new ModelRenderer(this);
        rightArmJoint.setPos(0.0F, 4.0F, 0.0F);
        rightArmBone.addChild(rightArmJoint);
        rightArmJoint.texOffs(0, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        rightForeArm = new ModelRenderer(this);
        rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        rightArmBone.addChild(rightForeArm);
        rightForeArm.texOffs(0, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        rightForeArm.texOffs(16, 95).addBox(-2.0F, -0.1F, -2.0F, 4.0F, 2.0F, 4.0F, 0.185F, false);
        rightForeArm.texOffs(16, 95).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 3.0F, 4.0F, 0.15F, false);
        rightForeArm.texOffs(16, 114).addBox(-2.6F, 3.2F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);
        rightForeArm.texOffs(16, 116).addBox(-2.6F, 3.8F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2F, false);
        rightForeArm.texOffs(16, 119).addBox(-2.6F, 4.4F, -1.5F, 1.0F, 1.0F, 3.0F, -0.2F, false);
        rightForeArm.texOffs(16, 123).addBox(-2.6F, 5.0F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, false);

        leftLegXRot = new ModelRenderer(this);
        leftLegXRot.setPos(2.0F, 12.0F, 0.0F);
        body.addChild(leftLegXRot);
        

        leftLegBone = new ModelRenderer(this);
        leftLegBone.setPos(0.0F, 0.0F, 0.0F);
        leftLegXRot.addChild(leftLegBone);
        leftLegBone.texOffs(96, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        leftLegBone.texOffs(112, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.1F, false);
        leftLegBone.texOffs(108, 119).addBox(-1.15F, 4.5F, -2.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        leftLegJoint = new ModelRenderer(this);
        leftLegJoint.setPos(0.0F, 6.0F, 0.0F);
        leftLegBone.addChild(leftLegJoint);
        leftLegJoint.texOffs(96, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

        leftLowerLeg = new ModelRenderer(this);
        leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        leftLegBone.addChild(leftLowerLeg);
        leftLowerLeg.texOffs(96, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        leftLowerLeg.texOffs(112, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.15F, false);
        leftLowerLeg.texOffs(108, 102).addBox(-2.0F, 0.8F, -2.0F, 4.0F, 1.0F, 4.0F, 0.1F, true);

        rightLegXRot = new ModelRenderer(this);
        rightLegXRot.setPos(-2.0F, 12.0F, 0.0F);
        body.addChild(rightLegXRot);
        

        rightLegBone = new ModelRenderer(this);
        rightLegBone.setPos(0.0F, 0.0F, 0.0F);
        rightLegXRot.addChild(rightLegBone);
        rightLegBone.texOffs(64, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        rightLegBone.texOffs(80, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.1F, false);
        rightLegBone.texOffs(76, 119).addBox(-0.85F, 4.5F, -2.5F, 2.0F, 2.0F, 1.0F, 0.0F, false);

        rightLegJoint = new ModelRenderer(this);
        rightLegJoint.setPos(0.0F, 6.0F, 0.0F);
        rightLegBone.addChild(rightLegJoint);
        rightLegJoint.texOffs(64, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        rightLowerLeg = new ModelRenderer(this);
        rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        rightLegBone.addChild(rightLowerLeg);
        rightLowerLeg.texOffs(64, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        rightLowerLeg.texOffs(80, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.15F, false);
        rightLowerLeg.texOffs(76, 102).addBox(-2.0F, 0.8F, -2.0F, 4.0F, 1.0F, 4.0F, 0.1F, false);

        hairToAnimateManually = new ArrayList<>();
        Collections.addAll(hairToAnimateManually, hair2, hair4, hair6, hair8, hair10, 
                hair11, hair12, hair13, hair14, hair15, hair16, hair17, hair18, hair19, hair20, 
                hair21, hair22, hair23, hair24, hair25, hair26, hair27, hair28, hair29, hair30, 
                hair31, hair32, hair33, hair34, hair35, hair36, hair37, hair38, hair39, hair40, 
                hair41, hair42, hair43, hair44, hair45, hair46, hair47, hair48);
    }
    
    @Override
    public void afterInit() {
        super.afterInit();
        putNamedModelPart("leftShoulder", leftShoulder);
        putNamedModelPart("rightShoulder", rightShoulder);
        putNamedModelPart("frontFabric", frontFabric);
        putNamedModelPart("backFabric", backFabric);
    }
    
    @Override
    public void setupAnim(StarPlatinumEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        manualAnimateHair();
    }
    
    
    
    private final List<ModelRenderer> hairToAnimateManually;
    private static final float TWO_PI = (float) Math.PI * 2;
    private float ticksPrev;
    @Deprecated
    private void manualAnimateHair() {
        for (int i = 0; i < hairToAnimateManually.size(); i++) {
            ModelRenderer hair = hairToAnimateManually.get(i);
            float xV = (ticks + hair.x) / 71F;
            xV -= (int) xV;
            float yV = (ticks + hair.y) / 31F;
            yV -= (int) yV;
            float xRotAnim = MathHelper.sin(xV * TWO_PI);
            float yRotAnim = MathHelper.sin(yV * TWO_PI);

            float xVPrev = (ticksPrev + hair.x) / 71F;
            xVPrev -= (int) xVPrev;
            float yVPrev = (ticksPrev + hair.y) / 31F;
            yVPrev -= (int) yVPrev;
            xRotAnim -= MathHelper.sin(xVPrev * TWO_PI);
            yRotAnim -= MathHelper.sin(yVPrev * TWO_PI);

            hair.xRot += xRotAnim * 0.05F;
            hair.yRot += yRotAnim * 0.0125F;
        }
        ticksPrev = ticks;
    }
    
}
