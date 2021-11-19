package com.github.standobyte.jojo.client.renderer.entity.damaging.extending;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.ownerbound.repeating.SPStarFingerModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SPStarFingerEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class SPStarFingerRenderer extends ExtendingEntityRenderer<SPStarFingerEntity, SPStarFingerModel> {

    public SPStarFingerRenderer(EntityRendererManager renderManager) {
        super(renderManager, new SPStarFingerModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/sp_star_finger.png"));
    }

}
