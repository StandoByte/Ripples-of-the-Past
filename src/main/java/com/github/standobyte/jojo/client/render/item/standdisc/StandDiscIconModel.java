package com.github.standobyte.jojo.client.render.item.standdisc;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;

/*
 * Was made with the help of TheGreyGhost's Minecraft by Example repository
 * https://github.com/TheGreyGhost/MinecraftByExample/tree/master/src/main/java/minecraftbyexample/mbe15_item_dynamic_item_model
 */
@SuppressWarnings("deprecation")
public class StandDiscIconModel implements IBakedModel {
    private final IBakedModel baseDiscModel;
    private final ItemOverrideList discItemOverrideList;
    
    /**
     * Create our model, using the given baked model as a base to add extra BakedQuads to.
     * @param baseDiscModel         the base model
     */
    public StandDiscIconModel(IBakedModel baseDiscModel) {
        this.baseDiscModel = baseDiscModel;
        this.discItemOverrideList = new StandDiscItemOverrideList();
    }

    // called for item rendering
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return baseDiscModel.getQuads(state, side, rand);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return discItemOverrideList;
    }

    // not needed for item, but hey
    @Override
    public boolean useAmbientOcclusion() {
        return baseDiscModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return baseDiscModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return baseDiscModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return baseDiscModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return baseDiscModel.getParticleIcon();
    }

    @Override
    public ItemCameraTransforms getTransforms() {
        return baseDiscModel.getTransforms();
    }

    // This is a forge extension that is expected for blocks only.
    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        throw new AssertionError("ChessboardModel::getQuads(IModelData) should never be called");
    }

    // This is a forge extension that is expected for blocks only.
    @Override
    @Nonnull
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        throw new AssertionError("ChessboardModel::getModelData should never be called");
    }
}
