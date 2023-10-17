package com.github.standobyte.jojo.client.render.item;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.google.common.primitives.Ints;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.SpriteMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

/*
 * Was made with the help of TheGreyGhost's Minecraft by Example repository
 * https://github.com/TheGreyGhost/MinecraftByExample/tree/master/src/main/java/minecraftbyexample/mbe15_item_dynamic_item_model
 */

/**
 * Generated by StandDiscIconModel and StandDiscItemOverrideList. Contains information about
 * 1) The base disc model to be drawn; and
 * 2) The Stand icon to be drawn.
 */
@SuppressWarnings("deprecation")
public class StandDiscFinalisedModel implements IBakedModel {
    private static final Map<StandType<?>, StandDiscFinalisedModel> CACHED_MODELS = new HashMap<>(); // allows for a null key
    
    private final IBakedModel parentModel;
    @Nullable private final StandType<?> stand;
    
    public static StandDiscFinalisedModel getModel(IBakedModel parentModel, @Nullable StandType<?> stand) {
        if (CACHED_MODELS.containsKey(stand)) {
            StandDiscFinalisedModel model = CACHED_MODELS.get(stand);
            if (model != null && model.parentModel == parentModel) {
                return model;
            }
        }
        StandDiscFinalisedModel model = new StandDiscFinalisedModel(parentModel, stand);
        CACHED_MODELS.put(stand, model);
        return model;
    }

    private StandDiscFinalisedModel(IBakedModel parentModel, @Nullable StandType<?> stand) {
        this.parentModel = parentModel;
        this.stand = stand;
    }
    
    /**
     * We return a list of quads here which is used to draw the disc.
     * We do this by getting the list of quads for the base model (the disc itself), then adding an extra quad for
     *   the Stand icon. The Stand instance was provided to the constructor of the finalised model.
     *
     * @param state
     * @param side  which side: north, east, south, west, up, down, or null.  NULL is a different kind to the others
     *   see here for more information: http://minecraft.gamepedia.com/Block_models#Item_models
     * @param rand
     * @return the list of quads to be rendered
     */

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        // our Stand icon is only drawn when side is NULL.
        if (side != null) {
            return parentModel.getQuads(state, side, rand);
        }

        List<BakedQuad> combinedQuadsList = new ArrayList<>(parentModel.getQuads(state, side, rand));
        
        boolean hasIconLayer = false;
        Iterator<BakedQuad> quadIter = combinedQuadsList.iterator();
        while (quadIter.hasNext()) {
            BakedQuad quad = quadIter.next();
            if (quad.getTintIndex() == ICON_RENDER_LAYER && quad.getSprite().getName().equals(DYNAMIC_STAND_ICON_PATH)) {
                quadIter.remove();
                hasIconLayer = true;
            }
        }
        
        if (hasIconLayer) {
            combinedQuadsList.addAll(getStandIconQuad());
        }
        
        return combinedQuadsList;
        // FaceBakery.makeBakedQuad() can also be useful for generating quads. See mbe04: AltimeterBakedModel
    }
    
    private static final ResourceLocation DYNAMIC_STAND_ICON_PATH = new ResourceLocation(JojoMod.MOD_ID, "dynamic_stand_icon");
    private static final int ICON_RENDER_LAYER = 1;
    // return a list of BakedQuads for drawing the Stand icon (if the Stand type isn't null)
    private List<BakedQuad> getStandIconQuad() {
        List<BakedQuad> quads = new ArrayList<>();
        if (stand == null) return quads;
        SpriteMap spriteMap = ModelLoader.instance().getSpriteMap();
        if (spriteMap == null) return quads;
        
        AtlasTexture blocksStitchedTextures = spriteMap.getAtlas(AtlasTexture.LOCATION_BLOCKS);
        TextureAtlasSprite standIconTexture = blocksStitchedTextures.getSprite(stand.getIconTextureBlocksAtlas());
        // if you want to use your own texture, you can add it to the texture map using code similar to this in your ClientProxy:
        //    @SubscribeEvent
        //    public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        //    if (event.getMap().getTextureLocation() == LOCATION_BLOCKS_TEXTURE) {
        //      event.addSprite(AltimeterBakedModel.digitsTextureRL);
        //    }

        // "builtin/generated" item, which are generated from the 2D texture by adding a thickness in the z direction
        //    (i.e. north<-->south thickness), are centred around the z=0.5 plane.
        final float BUILTIN_GEN_ITEM_THICKNESS = 1/16.0F;
        final float BUILTIN_GEN_ITEM_Z_CENTRE = 0.5F;
        final float BUILTIN_GEN_ITEM_Z_MAX = BUILTIN_GEN_ITEM_Z_CENTRE + BUILTIN_GEN_ITEM_THICKNESS / 2.0F;
        final float BUILTIN_GEN_ITEM_Z_MIN = BUILTIN_GEN_ITEM_Z_CENTRE - BUILTIN_GEN_ITEM_THICKNESS / 2.0F;
        final float SOUTH_FACE_POSITION = 1.0F;  // the south face of the cube is at z = 1.0F
        final float NORTH_FACE_POSITION = 0.0F;  // the north face of the cube is at z = 0.0F
        // https://greyminecraftcoder.blogspot.com/2020/02/blocks-1144.html

        final float DISTANCE_BEHIND_SOUTH_FACE = SOUTH_FACE_POSITION - BUILTIN_GEN_ITEM_Z_MAX;
        final float DISTANCE_BEHIND_NORTH_FACE = BUILTIN_GEN_ITEM_Z_MIN - NORTH_FACE_POSITION;
        
        float xCenterPosition = 0.6875F;
        float yCenterPosition = 0.5F;
        float width = 0.375F;
        float height = 0.375F;
        
        // make a baked quad for each side of the disc i.e. front and back (south and north)
        final float DELTA_FOR_OVERLAP = 0.001F;  // add a small overlap to stop the quad from lying exactly on top of the
                                                 //   existing face, which leads to "z-fighting" where the two quads
                                                 //   fight each other to be on top.  looks awful.

        BakedQuad iconFront = createBakedQuadForFace(xCenterPosition, width,
                yCenterPosition, height,
                -DISTANCE_BEHIND_SOUTH_FACE + DELTA_FOR_OVERLAP,
                ICON_RENDER_LAYER, standIconTexture, Direction.SOUTH
                );
        BakedQuad iconBack = createBakedQuadForFace(xCenterPosition, width,
                yCenterPosition, height,
                -DISTANCE_BEHIND_NORTH_FACE + DELTA_FOR_OVERLAP,
                ICON_RENDER_LAYER, standIconTexture, Direction.NORTH
                );
        quads.add(iconFront);
        quads.add(iconBack);
        
        return quads;
    }

    /**
   // Creates a baked quad for the given face.
   // When you are directly looking at the face, the quad is centred at [centreLR, centreUD]
   // The left<->right "width" of the face is width, the bottom<-->top "height" is height.
   // The amount that the quad is displaced towards the viewer i.e. (perpendicular to the flat face you can see) is forwardDisplacement
   //   - for example, for an EAST face, a value of 0.00 lies directly on the EAST face of the cube.  a value of 0.01 lies
   //     slightly to the east of the EAST face (at x=1.01).  a value of -0.01 lies slightly to the west of the EAST face (at x=0.99).
   // The orientation of the faces is as per the diagram on this page
   //   http://greyminecraftcoder.blogspot.com.au/2014/12/block-models-texturing-quads-faces.html
   // Read this page to lechessboardarn more about how to draw a textured quad
   //   http://greyminecraftcoder.blogspot.co.at/2014/12/the-tessellator-and-worldrenderer-18.html
     * @param centreLR the centre point of the face left-right
     * @param width    width of the face
     * @param centreUD centre point of the face top-bottom
     * @param height height of the face from top to bottom
     * @param forwardDisplacement the displacement of the face (towards the front)
     * @param itemRenderLayer which item layer the quad is on
     * @param texture the texture to use for the quad
     * @param face the face to draw this quad on
     * @return
     */
    private BakedQuad createBakedQuadForFace(float centreLR, float width, float centreUD, float height, float forwardDisplacement,
            int itemRenderLayer, TextureAtlasSprite texture, Direction face) {
        float x1, x2, x3, x4;
        float y1, y2, y3, y4;
        float z1, z2, z3, z4;
        int packednormal;
        final float CUBE_MIN = 0.0F;
        final float CUBE_MAX = 1.0F;

        switch (face) {
        case UP: {
            x1 = x2 = centreLR + width/2.0F;
            x3 = x4 = centreLR - width/2.0F;
            z1 = z4 = centreUD + height/2.0F;
            z2 = z3 = centreUD - height/2.0F;
            y1 = y2 = y3 = y4 = CUBE_MAX + forwardDisplacement;
            break;
        }
        case DOWN: {
            x1 = x2 = centreLR + width/2.0F;
            x3 = x4 = centreLR - width/2.0F;
            z1 = z4 = centreUD - height/2.0F;
            z2 = z3 = centreUD + height/2.0F;
            y1 = y2 = y3 = y4 = CUBE_MIN - forwardDisplacement;
            break;
        }
        case WEST: {
            z1 = z2 = centreLR + width/2.0F;
            z3 = z4 = centreLR - width/2.0F;
            y1 = y4 = centreUD - height/2.0F;
            y2 = y3 = centreUD + height/2.0F;
            x1 = x2 = x3 = x4 = CUBE_MIN - forwardDisplacement;
            break;
        }
        case EAST: {
            z1 = z2 = centreLR - width/2.0F;
            z3 = z4 = centreLR + width/2.0F;
            y1 = y4 = centreUD - height/2.0F;
            y2 = y3 = centreUD + height/2.0F;
            x1 = x2 = x3 = x4 = CUBE_MAX + forwardDisplacement;
            break;
        }
        case NORTH: {
            x1 = x2 = centreLR - width/2.0F;
            x3 = x4 = centreLR + width/2.0F;
            y1 = y4 = centreUD - height/2.0F;
            y2 = y3 = centreUD + height/2.0F;
            z1 = z2 = z3 = z4 = CUBE_MIN - forwardDisplacement;
            break;
        }
        case SOUTH: {
            x1 = x2 = centreLR + width/2.0F;
            x3 = x4 = centreLR - width/2.0F;
            y1 = y4 = centreUD - height/2.0F;
            y2 = y3 = centreUD + height/2.0F;
            z1 = z2 = z3 = z4 = CUBE_MAX + forwardDisplacement;
            break;
        }
        default: {
            throw new AssertionError("Unexpected Direction in createBakedQuadForFace:" + face);
        }
        }
        
        // FIXME the icon glows in debug, but doesn't when using the built mod... wtf
        // the order of the vertices on the face is (from the point of view of someone looking at the front face):
        // 1 = bottom right, 2 = top right, 3 = top left, 4 = bottom left

        packednormal = calculatePackedNormal(x1, y1, z1,  x2, y2, z2,  x3, y3, z3,  x4, y4, z4);

        // give our item maximum lighting
        final int BLOCK_LIGHT = 15;
        final int SKY_LIGHT = 15;
        int lightMapValue = LightTexture.pack(BLOCK_LIGHT, SKY_LIGHT);

        final int minU = 0;
        final int maxU = 16;
        final int minV = 0;
        final int maxV = 16;
        int [] vertexData1 = vertexToInts(x1, y1, z1, Color.WHITE.getRGB(), texture, maxU, maxV, lightMapValue, packednormal);
        int [] vertexData2 = vertexToInts(x2, y2, z2, Color.WHITE.getRGB(), texture, maxU, minV, lightMapValue, packednormal);
        int [] vertexData3 = vertexToInts(x3, y3, z3, Color.WHITE.getRGB(), texture, minU, minV, lightMapValue, packednormal);
        int [] vertexData4 = vertexToInts(x4, y4, z4, Color.WHITE.getRGB(), texture, minU, maxV, lightMapValue, packednormal);
        int [] vertexDataAll = Ints.concat(vertexData1, vertexData2, vertexData3, vertexData4);
        final boolean APPLY_DIFFUSE_LIGHTING = true;
        return new BakedQuad(vertexDataAll, itemRenderLayer, face, texture, APPLY_DIFFUSE_LIGHTING);
    }

    /**
     * Converts the vertex information to the int array format expected by BakedQuads.  Useful if you don't know
     *   in advance what it should be.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param color RGBA colour format - white for no effect, non-white to tint the face with the specified colour
     * @param texture the texture to use for the face
     * @param u u-coordinate of the texture (0 - 16) corresponding to [x,y,z]
     * @param v v-coordinate of the texture (0 - 16) corresponding to [x,y,z]
     * @param lightmapvalue the blocklight+skylight packed light map value (generally: set this to maximum for items)
     *                      http://greyminecraftcoder.blogspot.com/2020/04/lighting-1144.html
     * @param normal the packed representation of the normal vector, see calculatePackedNormal().  Used for lighting item.
     * @return
     */
    private int[] vertexToInts(float x, float y, float z, int color, TextureAtlasSprite texture, float u, float v, int lightmapvalue, int normal) {
        // based on FaceBakery::storeVertexData and FaceBakery::fillVertexData

        return new int[] {
                Float.floatToRawIntBits(x),
                Float.floatToRawIntBits(y),
                Float.floatToRawIntBits(z),
                color,
                Float.floatToRawIntBits(texture.getU(u)),
                Float.floatToRawIntBits(texture.getV(v)),
                lightmapvalue,
                normal
        };
    }

    /**
     * Calculate the normal vector based on four input coordinates
     * Follows minecraft convention that the coordinates are given in anticlockwise direction from the point of view of
     * someone looking at the front of the face
     * assumes that the quad is coplanar but should produce a 'reasonable' answer even if not.
     * @return the packed normal, ZZYYXX
     */
    private int calculatePackedNormal(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4) {

        float xp = x4-x2;
        float yp = y4-y2;
        float zp = z4-z2;

        float xq = x3-x1;
        float yq = y3-y1;
        float zq = z3-z1;

        //Cross Product
        float xn = yq*zp - zq*yp;
        float yn = zq*xp - xq*zp;
        float zn = xq*yp - yq*xp;

        //Normalize
        float norm = (float)Math.sqrt(xn*xn + yn*yn + zn*zn);
        final float SMALL_LENGTH =  1.0E-4F;  //Vec3d.normalise() uses this
        if (norm < SMALL_LENGTH) norm = 1.0F;  // protect against degenerate quad

        norm = 1.0F / norm;
        xn *= norm;
        yn *= norm;
        zn *= norm;

        int x = ((byte)(xn * 127)) & 0xFF;
        int y = ((byte)(yn * 127)) & 0xFF;
        int z = ((byte)(zn * 127)) & 0xFF;
        return x | (y << 0x08) | (z << 0x10);
    }
    
    
    
    @Override
    public boolean useAmbientOcclusion() {
        return parentModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return parentModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return parentModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return parentModel.getParticleIcon();
    }

    @Override
    public ItemCameraTransforms getTransforms() {
        return parentModel.getTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        throw new UnsupportedOperationException("The finalised model does not have an override list.");
    }
}