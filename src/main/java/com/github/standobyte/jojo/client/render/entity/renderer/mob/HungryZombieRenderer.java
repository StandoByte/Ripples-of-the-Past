package com.github.standobyte.jojo.client.render.entity.renderer.mob;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;

import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.ZombieModel;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.util.ResourceLocation;

public class HungryZombieRenderer extends AbstractZombieRenderer<HungryZombieEntity, ZombieModel<HungryZombieEntity>> {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/hungry_zombie.png");

    public HungryZombieRenderer(EntityRendererManager renderManager) {
        super(renderManager, new ZombieModel<>(0.0F, false), new ZombieModel<>(0.5F, true), new ZombieModel<>(1.0F, true));
    }
    
    @Override
    public ResourceLocation getTextureLocation(ZombieEntity entity) {
        return TEXTURE;
    }
}
