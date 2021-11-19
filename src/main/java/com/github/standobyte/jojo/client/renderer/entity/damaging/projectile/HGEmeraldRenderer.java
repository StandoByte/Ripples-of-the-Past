package com.github.standobyte.jojo.client.renderer.entity.damaging.projectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.projectile.HGEmeraldModel;
import com.github.standobyte.jojo.client.renderer.entity.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.HGEmeraldEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class HGEmeraldRenderer extends SimpleEntityRenderer<HGEmeraldEntity, HGEmeraldModel> {

    public HGEmeraldRenderer(EntityRendererManager renderManager) {
        super(renderManager, new HGEmeraldModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hg_emerald.png"));
    }

}
