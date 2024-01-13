package com.github.standobyte.jojo.client.render.entity.bb;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3f;

@SuppressWarnings("unused")
public class ParseGeckoModel {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ModelParsed.UV.class, ModelParsed.UV.DESERIALIZER)
            .create();
    
    public static EntityModelUnbaked parseGeckoModel(JsonElement json) {
        JsonElement modelJson = json.getAsJsonObject().get("minecraft:geometry").getAsJsonArray().get(0);
        ModelParsed modelParsed = GSON.fromJson(modelJson, ModelParsed.class);
        EntityModelUnbaked modelUnbaked = modelParsed.createUnbakedModel();
        return modelUnbaked;
    }
    
    
    
    private static class ModelParsed {
        Description description;
        List<BoneParsed> bones;
        
        static class Description {
            int texture_width = 128;
            int texture_height = 64;
        }
        
        static class BoneParsed {
            String name;
            @Nullable String parent;
            float[] pivot;
            @Nullable float[] rotation;
            List<CubeParsed> cubes;
            
            private int convertedCount = 0;
            
            ModelRenderer makeModelPart(int texWidth, int texHeight, @Nullable BoneParsed parent) {
                ModelRenderer modelPart = new ModelRenderer(texWidth, texHeight, 0, 0);
                
                float yOffset = 24;
                
                Vector3f modelPartPos;
                if (parent != null) {
                    float[] parentPivot = parent.pivot;
                    
                    modelPartPos = new Vector3f(
                              pivot[0] - parentPivot[0],
                            -(pivot[1] - parentPivot[1]),
                              pivot[2] - parentPivot[2]
                            );
                }
                else {
                    modelPartPos = new Vector3f(
                             pivot[0], 
                            -pivot[1] + yOffset, 
                             pivot[2]);
                }
                
                modelPart.x = modelPartPos.x();
                modelPart.y = modelPartPos.y();
                modelPart.z = modelPartPos.z();
                
                if (rotation != null) {
                    modelPart.xRot = rotation[0] * MathUtil.DEG_TO_RAD;
                    modelPart.yRot = rotation[1] * MathUtil.DEG_TO_RAD;
                    modelPart.zRot = rotation[2] * MathUtil.DEG_TO_RAD;
                }
                
                
                if (cubes != null) {
                    ObjectList<ModelRenderer.ModelBox> modelCubes = new ObjectArrayList<>();
                    for (CubeParsed cubeParsed : cubes) {
                        Optional<BoneParsed> cubeRotated = cubeParsed.convertRotated(this);
                        if (cubeRotated.isPresent()) {
                            ModelRenderer autoGenRotatedCube = cubeRotated.get().makeModelPart(texWidth, texHeight, this);
                            modelPart.addChild(autoGenRotatedCube);
                        }
                        else {
                            modelCubes.add(cubeParsed.makeModelBox(texWidth, texHeight, this));
                        }
                    }
                    ClientReflection.setCubes(modelPart, modelCubes);
                }
                
                return modelPart;
            }
            
            void addCubes() {
                
            }
        }
        
        static class CubeParsed {
            float[] origin;
            float[] size;
            float inflate;
            float[] pivot;
            @Nullable float[] rotation;
            boolean mirror;
            UV uv;
            
            Optional<BoneParsed> convertRotated(BoneParsed parentBone) {
                if (rotation != null && (rotation[0] != 0 || rotation[1] != 0 || rotation[2] != 0 )) {
                    String name = parentBone.name + "_r" + parentBone.convertedCount++;
                    
                    BoneParsed bone = new BoneParsed();
                    bone.name = name;
                    bone.parent = parentBone.name;
                    bone.pivot = this.pivot != null ? this.pivot : new float[] { 0, 0, 0 };
                    bone.rotation = this.rotation;
                    if (bone.cubes == null) bone.cubes = new ArrayList<>();
                    bone.cubes.add(this);
                    
                    this.pivot = new float[] { 0, 0, 0 };
                    this.rotation = null;
                    
                    return Optional.of(bone);
                }
                
                return Optional.empty();
            }
            
            ModelRenderer.ModelBox makeModelBox(float texWidth, float texHeight, BoneParsed parentBone) {
                Vector3f originJ = new Vector3f(
                          origin[0] - parentBone.pivot[0],
                        -(origin[1] - parentBone.pivot[1]) - size[1],
                          origin[2] - parentBone.pivot[2]
                        );
                
                int[] boxUV0 = uv instanceof BoxUV ? ((BoxUV) uv).uv : new int[] { 0, 0 };
                ModelRenderer.ModelBox box = new ModelRenderer.ModelBox(
                        boxUV0[0], boxUV0[1], 
                        originJ.x(), originJ.y(), originJ.z(), 
                        size[0], size[1], size[2], 
                        inflate, inflate, inflate, 
                        mirror, texWidth, texHeight);
                
                if (uv instanceof PerFaceUV) {
                    ModelRenderer.TexturedQuad[] polygons = new ModelRenderer.TexturedQuad[6];
                    float x0 = originJ.x() - inflate;
                    float y0 = originJ.y() - inflate;
                    float z0 = originJ.z() - inflate;
                    float x1 = originJ.x() + inflate + size[0];
                    float y1 = originJ.y() + inflate + size[1];
                    float z1 = originJ.z() + inflate + size[2];
                    
                    ModelRenderer.PositionTextureVertex x0y0z0 = new ModelRenderer.PositionTextureVertex(x0, y0, z0, 0.0F, 0.0F);
                    ModelRenderer.PositionTextureVertex x1y0z0 = new ModelRenderer.PositionTextureVertex(x1, y0, z0, 0.0F, 8.0F);
                    ModelRenderer.PositionTextureVertex x1y1z0 = new ModelRenderer.PositionTextureVertex(x1, y1, z0, 8.0F, 8.0F);
                    ModelRenderer.PositionTextureVertex x0y1z0 = new ModelRenderer.PositionTextureVertex(x0, y1, z0, 8.0F, 0.0F);
                    ModelRenderer.PositionTextureVertex x0y0z1 = new ModelRenderer.PositionTextureVertex(x0, y0, z1, 0.0F, 0.0F);
                    ModelRenderer.PositionTextureVertex x1y0z1 = new ModelRenderer.PositionTextureVertex(x1, y0, z1, 0.0F, 8.0F);
                    ModelRenderer.PositionTextureVertex x1y1z1 = new ModelRenderer.PositionTextureVertex(x1, y1, z1, 8.0F, 8.0F);
                    ModelRenderer.PositionTextureVertex x0y1z1 = new ModelRenderer.PositionTextureVertex(x0, y1, z1, 8.0F, 0.0F);
                    
                    Map<Direction, ModelRenderer.PositionTextureVertex[]> faceVertices = new EnumMap<>(Direction.class);
                    faceVertices.put(Direction.DOWN, new ModelRenderer.PositionTextureVertex[]{
                            x1y0z1, 
                            x0y0z1, 
                            x0y0z0, 
                            x1y0z0});
                    faceVertices.put(Direction.UP, new ModelRenderer.PositionTextureVertex[]{
                            x1y1z0, 
                            x0y1z0, 
                            x0y1z1, 
                            x1y1z1});
                    faceVertices.put(Direction.WEST, new ModelRenderer.PositionTextureVertex[]{
                            x0y0z0, 
                            x0y0z1, 
                            x0y1z1, 
                            x0y1z0});
                    faceVertices.put(Direction.NORTH, new ModelRenderer.PositionTextureVertex[]{
                            x1y0z0, 
                            x0y0z0, 
                            x0y1z0, 
                            x1y1z0});
                    faceVertices.put(Direction.EAST, new ModelRenderer.PositionTextureVertex[]{
                            x1y0z1, 
                            x1y0z0, 
                            x1y1z0, 
                            x1y1z1});
                    faceVertices.put(Direction.SOUTH, new ModelRenderer.PositionTextureVertex[]{
                            x0y0z1, 
                            x1y0z1, 
                            x1y1z1, 
                            x0y1z1});
                            
                    
                    int polygonsCount = 0;
                    Map<Direction, PerFaceUV.FaceUV> perFaceUv = ((PerFaceUV) uv).uv();
                    for (Direction direction : Direction.values()) {
                        Direction uvPart = direction.getAxis() == Axis.Z ? direction : direction.getOpposite();
                        if (perFaceUv.containsKey(uvPart)) {
                            PerFaceUV.FaceUV uv = perFaceUv.get(uvPart);
                            polygons[polygonsCount++] = new ModelRenderer.TexturedQuad(faceVertices.get(direction), 
                                    uv.uv[0], uv.uv[1], 
                                    uv.uv[0] + uv.uv_size[0], uv.uv[1] + uv.uv_size[1], 
                                    texWidth, texHeight, false, direction);
                        }
                    }
                    if (polygonsCount < polygons.length) {
                        polygons = Arrays.copyOf(polygons, polygonsCount);
                    }
                    ClientReflection.setPolygons(box, polygons);
                    
                    
                    // i'll just leave this here
                    /*
                     *    u0   u1       u2   u3  u4   u5
                     * v0       ┌────────┬────────┐
                     *          │   U    │   D    │
                     * v1  ┌────┼────────┼────┬───┴────┐ ⎫
                     *     │    │        │    │        │ ⎪
                     *     │ E  │   N    │ W  │   S    │ ⎬ size.y
                     *     │    │        │    │        │ ⎪
                     *     │    │        │    │        │ ⎪
                     * v2  └────┴────────┴────┴────────┘ ⎭
                     *     size.z               size.x
                     */
//                    float u0 = texCoordU;
//                    float u1 = texCoordU + size.z();
//                    float u2 = texCoordU + size.z() + size.x();
//                    float u4 = texCoordU + size.z() + size.x() + size.x();
//                    float u3 = texCoordU + size.z() + size.x() + size.z();
//                    float u5 = texCoordU + size.z() + size.x() + size.z() + size.x();
//                    float v0 = texCoordV;
//                    float v1 = texCoordV + size.z();
//                    float v2 = texCoordV + size.z() + size.y();
                }
                
                return box;
            }
        }
        
        
        static interface UV {
            
            static final JsonDeserializer<UV> DESERIALIZER = new JsonDeserializer<UV> () {

                @Override
                public UV deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    if (json.isJsonArray()) {
                        int[] uv = context.deserialize(json, int[].class);
                        return new BoxUV(uv);
                    }
                    else {
                        JsonObject wrappingJson = new JsonObject();
                        wrappingJson.add("uv", json);
                        PerFaceUV uv = context.deserialize(wrappingJson, PerFaceUV.class);
                        return uv;
                    }
                }
            };
        }
        
        static class BoxUV implements UV {
            int[] uv;
            
            BoxUV(int[] uv) {
                this.uv = uv;
            }
        }
        
        static class PerFaceUV implements UV {
            Map<String, FaceUV> uv;
            
            static class FaceUV {
                float[] uv;
                float[] uv_size;
            }
            
            private Map<Direction, FaceUV> uv() {
                Map<Direction, FaceUV> facesPerDirection = new EnumMap<>(Direction.class);
                for (Direction direction : Direction.values()) {
                    if (this.uv.containsKey(direction.getName())) {
                        facesPerDirection.put(direction, this.uv.get(direction.getName()));
                    }
                }
                return facesPerDirection;
            }
        }
        
        
        
        public EntityModelUnbaked createUnbakedModel() {
            int texWidth = description.texture_width;
            int texHeight = description.texture_height;
            EntityModelUnbaked model = new EntityModelUnbaked(texWidth, texHeight);
            
            Map<String, BoneParsed> bonesNamed = new HashMap<>();
            for (BoneParsed bone : bones) {
                bonesNamed.put(bone.name, bone);
            }
            
            for (BoneParsed bone : bones) {
                BoneParsed parent = bonesNamed.get(bone.parent);
                ModelRenderer modelPart = bone.makeModelPart(
                        texWidth, texHeight, parent);
                model.addModelPart(bone.name, modelPart, bone.parent);
            }
            
            return model;
        }
    }
}
