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

public class MeteoritePieces {
    private static IStructurePieceType METEORITE_PIECES;
    
    private static final ResourceLocation[] PIECE_CRATER = {
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/crater_1"),
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/crater_2"),
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/crater_3"),
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/crater_4"),
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/crater_5")};
    private static final ResourceLocation[] PIECE_BODY = {
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/body_1"),
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/body_2"),
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/body_3"),
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/body_4"),
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/body_5"),
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/body_6"),
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/body_7"),
            new ResourceLocation(JojoMod.MOD_ID, "meteorite/body_8")};
    private static final ResourceLocation PIECE_TRAIL = new ResourceLocation(JojoMod.MOD_ID, "meteorite/trail");
    
    public static void start(TemplateManager templateManager, BlockPos blockPos, List<StructurePiece> pieces, Random random) {
        Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
        pieces.add(new Piece(templateManager, PIECE_CRATER[random.nextInt(PIECE_CRATER.length)], blockPos, new BlockPos(0, -2, 0), rotation));
        pieces.add(new Piece(templateManager, PIECE_TRAIL, blockPos, new BlockPos(0, 1, 0), rotation));
        pieces.add(new Piece(templateManager, PIECE_BODY[random.nextInt(PIECE_BODY.length)], blockPos, new BlockPos(2, -3, 2), rotation));
    }
    
    public static void initPieceType() {
        METEORITE_PIECES = IStructurePieceType.setPieceId(Piece::new, JojoMod.MOD_ID + ":meteorite");
    }

    private static class Piece extends TemplateStructurePiece {
        private final ResourceLocation piece;
        private final Rotation rotation;

        public Piece(TemplateManager templateManager, ResourceLocation piece, BlockPos blockPos, BlockPos pieceOffset, Rotation rotation) {
            super(METEORITE_PIECES, 0);
            this.piece = piece;
            this.templatePosition = blockPos.offset(pieceOffset.rotate(rotation));
            this.rotation = rotation;
            this.setupPiece(templateManager);
        }

        public Piece(TemplateManager templateManager, CompoundNBT cnbt) {
           super(METEORITE_PIECES, cnbt);
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
