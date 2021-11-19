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

public class PillarmanTemplePieces {
    private static IStructurePieceType PILLARMAN_TEMPLE_PIECES;
    private static final ResourceLocation PIECE_BUILDING = new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple/building");
    private static final ResourceLocation PIECE_STAIRWAY = new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple/stairway");
    private static final ResourceLocation PIECE_CORRIDOR = new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple/corridor");
    private static final ResourceLocation PIECE_BOSSROOM = new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple/bossroom");
    
    public static void start(TemplateManager templateManager, BlockPos blockPos, List<StructurePiece> pieces, Random random) {
        Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
        pieces.add(new Piece(templateManager, PIECE_BUILDING, blockPos, new BlockPos(-27, 0, -27), rotation));
        pieces.add(new Piece(templateManager, PIECE_STAIRWAY, blockPos, new BlockPos(19, -42, -2), rotation));
        pieces.add(new Piece(templateManager, PIECE_CORRIDOR, blockPos, new BlockPos(61, -43, -3), rotation));
        pieces.add(new Piece(templateManager, PIECE_BOSSROOM, blockPos, new BlockPos(102, -45, -16), rotation));
    }
    
    public static void initPieceType() {
        PILLARMAN_TEMPLE_PIECES = IStructurePieceType.setPieceId(Piece::new, JojoMod.MOD_ID + ":PillarmanTemple");
    }

    private static class Piece extends TemplateStructurePiece {
        private final ResourceLocation piece;
        private final Rotation rotation;

        public Piece(TemplateManager templateManager, ResourceLocation piece, BlockPos blockPos, BlockPos pieceOffset, Rotation rotation) {
            super(PILLARMAN_TEMPLE_PIECES, 0);
            this.piece = piece;
            this.templatePosition = blockPos.offset(pieceOffset.rotate(rotation));
            this.rotation = rotation;
            this.setupPiece(templateManager);
        }

        public Piece(TemplateManager templateManager, CompoundNBT cnbt) {
           super(PILLARMAN_TEMPLE_PIECES, cnbt);
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
