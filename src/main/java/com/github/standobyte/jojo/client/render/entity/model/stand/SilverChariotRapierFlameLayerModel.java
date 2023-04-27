package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.client.render.FlameModelRenderer;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;

public class SilverChariotRapierFlameLayerModel extends SilverChariotModel {
    private FlameModelRenderer rapierBladeFlame;

    public SilverChariotRapierFlameLayerModel() {
        this(64, 64);
    }

    public SilverChariotRapierFlameLayerModel(int textureWidth, int textureHeight) {
        super(tex -> Atlases.translucentCullBlockSheet(), textureWidth, textureHeight);
    }

    @Override
    protected void addLayerSpecificBoxes() {
        rapier = new ModelRenderer(this);
        rapier.setPos(0.25F, 4.5F, 0.0F);
        rightForeArm.addChild(rapier);
        setRotationAngle(rapier, 0.7854F, 0.0F, 0.0F);
        
        rapierBladeFlame = new FlameModelRenderer(this);
        rapierBladeFlame.setPos(0.0F, 0.0F, 0.0F);
        rapier.addChild(rapierBladeFlame);
        rapierBladeFlame.addFlame(0, 0, -3.0F, 1, 3, Direction.NORTH);
        rapierBladeFlame.addFlame(0, 0, -4.5F, 1, 3, Direction.NORTH);
        rapierBladeFlame.addFlame(0, 0, -6.0F, 1, 3, Direction.NORTH);
        rapierBladeFlame.addFlame(0, 0, -7.5F, 1, 3, Direction.NORTH);
        rapierBladeFlame.addFlame(0, 0, -9.0F, 1, 3, Direction.NORTH);
        rapierBladeFlame.addFlame(0, 0, -10.5F, 1, 3, Direction.NORTH);
        rapierBladeFlame.addFlame(0, 0, -12.0F, 1, 3, Direction.NORTH);
        rapierBladeFlame.addFlame(0, 0, -13.5F, 1, 3, Direction.NORTH);
    }
}
