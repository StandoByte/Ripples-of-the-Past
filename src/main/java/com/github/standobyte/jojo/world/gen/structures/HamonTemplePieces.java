package com.github.standobyte.jojo.world.gen.structures;

import java.util.List;
import java.util.Random;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class HamonTemplePieces {
    private static IStructurePieceType HAMON_TEMPLE_PIECES;

    private static final ResourceLocation PIECE_BUILDING = new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/building");
    private static final ResourceLocation PIECE_PATHWAY = new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/pathway");
    private static final ResourceLocation[] PIECE_ROCK = {
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_1"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_2"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_3"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_4"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_5"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_6"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_7"),
            new ResourceLocation(JojoMod.MOD_ID, "hamon_temple/rock_8")};

    public static void start(TemplateManager templateManager, BlockPos blockPos, List<StructurePiece> pieces, Random random) {
        pieces.add(new Piece(templateManager, PIECE_BUILDING, blockPos.offset(new BlockPos(-24, -3, -24)), Rotation.NONE));
        for (Rotation rotation : Rotation.values()) {
            pieces.add(new Piece(templateManager, PIECE_PATHWAY, 
                    blockPos.offset(new BlockPos(-1, -4, -1)).offset(new BlockPos(-22, 0, -1).rotate(rotation)), rotation));
            Rotation randomRotation = Rotation.values()[random.nextInt(Rotation.values().length)];
            randomRotation = Rotation.NONE;
            pieces.add(new Piece(templateManager, PIECE_ROCK[random.nextInt(PIECE_ROCK.length)], 
                    blockPos.offset(new BlockPos(-4, -3, -4).rotate(randomRotation)).offset(new BlockPos(-21, 0, 0).rotate(rotation)), randomRotation));
        }
    }

    public static void initPieceType() {
        HAMON_TEMPLE_PIECES = IStructurePieceType.setPieceId(Piece::new, JojoMod.MOD_ID + ":HamonTemple");
    }

    private static class Piece extends TemplateStructurePiece {
        private final ResourceLocation piece;
        private final Rotation rotation;

        public Piece(TemplateManager templateManager, ResourceLocation piece, BlockPos blockPos, Rotation rotation) {
            super(HAMON_TEMPLE_PIECES, 0);
            this.piece = piece;
            this.templatePosition = blockPos;
            this.rotation = rotation;
            this.setupPiece(templateManager);
        }

        public Piece(TemplateManager templateManager, CompoundNBT cnbt) {
           super(HAMON_TEMPLE_PIECES, cnbt);
           this.piece = new ResourceLocation(cnbt.getString("Template"));
           this.rotation = Rotation.valueOf(cnbt.getString("Rotation"));
           this.setupPiece(templateManager);
        }

        private void setupPiece(TemplateManager templateManager) {
            Template template = templateManager.getOrCreate(piece);
            PlacementSettings placementsettings = new PlacementSettings().setRotation(rotation).setMirror(Mirror.NONE);
            setup(template, templatePosition, placementsettings);
        }

        @Override
        protected void addAdditionalSaveData(CompoundNBT cnbt) {
            super.addAdditionalSaveData(cnbt);
            cnbt.putString("Template", piece.toString());
            cnbt.putString("Rotation", rotation.name());
        }

        @Override
        protected void handleDataMarker(String function, BlockPos pos, IServerWorld world, Random rand, MutableBoundingBox sbb) {}
    }
}
