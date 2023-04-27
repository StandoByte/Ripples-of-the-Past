package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.client.render.FlameModelRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.MagiciansRedRenderer;

import net.minecraft.client.renderer.Atlases;

public class MagiciansRedFlameLayerModel extends MagiciansRedModel {

    public MagiciansRedFlameLayerModel() {
        this(64, 64);
    }

    public MagiciansRedFlameLayerModel(int textureWidth, int textureHeight) {
        super(tex -> Atlases.translucentCullBlockSheet(), textureWidth, textureHeight);
    }
    
    @Override
    protected void addLayerSpecificBoxes() {
        leftForeArm.addChild(new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0_SPRITE, MagiciansRedRenderer.FIRE_1_SPRITE)
                .addFlame(3.0F, 4.0F, 3.0F));
        rightForeArm.addChild(new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0_SPRITE, MagiciansRedRenderer.FIRE_1_SPRITE)
                .addFlame(3.0F, 4.0F, 3.0F));
        leftLeg.addChild(new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0_SPRITE, MagiciansRedRenderer.FIRE_1_SPRITE)
                .addFlame(5.0F, 4.0F, 4.0F));
        leftLowerLeg.addChild(new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0_SPRITE, MagiciansRedRenderer.FIRE_1_SPRITE)
                .addFlame(4.0F, 4.0F, 4.0F));
        rightLeg.addChild(new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0_SPRITE, MagiciansRedRenderer.FIRE_1_SPRITE)
                .addFlame(5.0F, 4.0F, 4.0F));
        rightLowerLeg.addChild(new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0_SPRITE, MagiciansRedRenderer.FIRE_1_SPRITE)
                .addFlame(4.0F, 4.0F, 4.0F));
    }
    
}
