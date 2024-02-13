package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.MagiciansRedModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.layer.MagiciansRedFlameLayer;
import com.github.standobyte.jojo.entity.stand.stands.MagiciansRedEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class MagiciansRedRenderer extends StandEntityRenderer<MagiciansRedEntity, StandEntityModel<MagiciansRedEntity>> {
    public static final RenderMaterial MR_FIRE_0 = new RenderMaterial(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(JojoMod.MOD_ID, "block/mr_fire_0"));
    public static final RenderMaterial MR_FIRE_1 = new RenderMaterial(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(JojoMod.MOD_ID, "block/mr_fire_1"));
    public static final Supplier<TextureAtlasSprite> FIRE_0_SPRITE = () -> MR_FIRE_0.sprite();
    public static final Supplier<TextureAtlasSprite> FIRE_1_SPRITE = () -> MR_FIRE_1.sprite();

    public MagiciansRedRenderer(EntityRendererManager renderManager) {
        super(renderManager, 
                StandModelRegistry.registerModel(new ResourceLocation(JojoMod.MOD_ID, "magicians_red"), MagiciansRedModel::new), 
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/magicians_red.png"), 0);
        addLayer(new MagiciansRedFlameLayer(this));
    }
}
