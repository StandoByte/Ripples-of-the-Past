package com.github.standobyte.jojo.client.render.block;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class BlockSprites {
    public static final RenderMaterial MR_FIRE_BLOCK_0 = new RenderMaterial(
            PlayerContainer.BLOCK_ATLAS, new ResourceLocation(JojoMod.MOD_ID, "block/mr_fire_0"));
    public static final RenderMaterial MR_FIRE_BLOCK_1 = new RenderMaterial(
            PlayerContainer.BLOCK_ATLAS, new ResourceLocation(JojoMod.MOD_ID, "block/mr_fire_1"));

}
