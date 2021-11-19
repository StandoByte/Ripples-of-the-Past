package com.github.standobyte.jojo.client.model.entity.stand;

import com.github.standobyte.jojo.client.model.FlameModelRenderer;

public class MagiciansRedFlameLayerModel extends MagiciansRedModel {

    public MagiciansRedFlameLayerModel() {
        this(64, 64);
    }

    public MagiciansRedFlameLayerModel(int textureWidth, int textureHeight) {
        super(textureWidth, textureHeight);
    }
    
    @Override
    protected void addBaseBoxes() {
        leftForeArm.addChild(new FlameModelRenderer(this).addFlame(3.0F, 4.0F, 3.0F));
        rightForeArm.addChild(new FlameModelRenderer(this).addFlame(3.0F, 4.0F, 3.0F));
        leftLeg.addChild(new FlameModelRenderer(this).addFlame(5.0F, 4.0F, 4.0F));
        leftLowerLeg.addChild(new FlameModelRenderer(this).addFlame(4.0F, 4.0F, 4.0F));
        rightLeg.addChild(new FlameModelRenderer(this).addFlame(5.0F, 4.0F, 4.0F));
        rightLowerLeg.addChild(new FlameModelRenderer(this).addFlame(4.0F, 4.0F, 4.0F));
    }
    
}
