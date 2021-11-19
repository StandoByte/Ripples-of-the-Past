package com.github.standobyte.jojo.client.renderer.entity.damaging.projectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.projectile.HamonBubbleCutterModel;
import com.github.standobyte.jojo.client.renderer.entity.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleCutterEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class HamonBubbleCutterRenderer extends SimpleEntityRenderer<HamonBubbleCutterEntity, HamonBubbleCutterModel> {

    public HamonBubbleCutterRenderer(EntityRendererManager renderManager) {
        super(renderManager, new HamonBubbleCutterModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hamon_bubble_cutter.png"));
    }

}
