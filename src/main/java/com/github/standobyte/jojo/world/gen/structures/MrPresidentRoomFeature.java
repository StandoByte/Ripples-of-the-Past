package com.github.standobyte.jojo.world.gen.structures;

import java.util.Random;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.world.gen.LoadMeFeature;
import com.mojang.serialization.Codec;

import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class MrPresidentRoomFeature extends Feature<NoFeatureConfig> implements LoadMeFeature {
    private final ResourceLocation roomPath = new ResourceLocation(JojoMod.MOD_ID, "mr_president_room");
    private Template roomTemplate;

    public MrPresidentRoomFeature(Codec<NoFeatureConfig> codec) {
        super(codec);
    }
    
    @Override
    public void loadTemplate(TemplateManager templateManager) {
        roomTemplate = templateManager.getOrCreate(roomPath);
    }
    
    @Override
    public boolean place(ISeedReader world, ChunkGenerator chunkGenerator, 
            Random random, BlockPos blockPos, NoFeatureConfig config) {
        if (roomTemplate != null) {
            PlacementSettings settings = new PlacementSettings().addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_AND_AIR);
            BlockPos blockpos1 = roomTemplate.getZeroPositionWithTransform(blockPos.offset(0, 0, 0), Mirror.NONE, Rotation.NONE);
            return roomTemplate.placeInWorld(world, blockpos1, blockpos1, settings, random, 2);
        }
        
        return false;
    }

}
