package com.github.standobyte.jojo.client.render.entity.bb;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.render.MeshModelBox;
import com.github.standobyte.jojo.client.render.MeshModelBox.Builder.MeshFaceBuilder;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

@SuppressWarnings("unused")
public class ParseGenericModel {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ModelParsed.Element.class, ModelParsed.Element.DESERIALIZER)
            .registerTypeAdapter(ModelParsed.BlockbenchObj.class, ModelParsed.BlockbenchObj.DESERIALIZER)
            .create();
    
    public static EntityModelUnbaked parseGenericModel(JsonElement json, ResourceLocation modelId) {
        ModelParsed modelParsed = GSON.fromJson(json, ModelParsed.class);
        
        modelParsed.afterParse(modelId);
        EntityModelUnbaked modelUnbaked = modelParsed.createUnbakedModel();
        return modelUnbaked;
    }
    
    
    
    private static class ModelParsed implements IParsedModel {
        Resolution resolution;
        List<Element> elements;
        List<BlockbenchObj> outliner;
        
        static class Resolution {
            int width;
            int height;
        }
        
        
        static abstract class Element {
            String name;
            UUID uuid;
            boolean visibility;
            float[] origin;
            float[] rotation;
            String render_order;
            boolean allow_mirror_modeling;
            
            abstract void addElement(ModelRenderer parent, ModelParsed.GroupParsed parentParsed, 
                    List<ModelRenderer.ModelBox> modelCubesCollection, int texWidth, int texHeight);
            
            static final JsonDeserializer<Element> DESERIALIZER = new JsonDeserializer<Element>() {
                
                @Override
                public Element deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    if (json.isJsonObject()) {
                        JsonObject jsonObj = json.getAsJsonObject();
                        if (jsonObj.has("type")) {
                            JsonElement typeElem = jsonObj.get("type");
                            if (typeElem.isJsonPrimitive()) {
                                JsonPrimitive typePrim = typeElem.getAsJsonPrimitive();
                                if (typePrim.isString()) {
                                    String type = typePrim.getAsString();
                                    try {
                                        switch (type) {
                                        case "cube":
                                            return context.deserialize(json, ElementCube.class);
                                        case "mesh":
                                            return context.deserialize(json, ElementMesh.class);
                                        default:
                                            throw new JsonParseException("Unknown element type: \"" + type + "\"");
                                        }
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                        throw e;
                                    }
                                }
                            }
                        }
                    }

                    throw new JsonParseException("No model element type present!");
                }
            };
            
            Optional<GroupParsed> convertRotated(GroupParsed parentBone) {
                if (rotation != null && (rotation[0] != 0 || rotation[1] != 0 || rotation[2] != 0 )) {
                    String name = parentBone.name + "_r" + parentBone.convertedCount++;
                    
                    GroupParsed bone = new GroupParsed();
                    bone.name = name;
                    bone.origin = this.origin != null ? this.origin : new float[] { 0, 0, 0 };
                    bone.rotation = this.rotation;
                    this.rotation = null;
                    
                    ElementUUID cube = new ElementUUID(this.uuid);
                    bone.children = new ArrayList<>();
                    bone.children.add(cube);
                    
                    bone.export = true;
                    
                    return Optional.of(bone);
                }
                
                return Optional.empty();
            }
        }
        
        static class ElementMesh extends Element {
            Map<String, float[]> vertices;
            Map<String, MeshFace> faces;
            
            class MeshFace {
                Map<String, float[]> uv;
                String[] vertices;
                int texture;
            }

            @Override
            void addElement(ModelRenderer parent, ModelParsed.GroupParsed parentParsed, 
                    List<ModelRenderer.ModelBox> modelCubesCollection, int texWidth, int texHeight) {
                if (origin == null) origin = new float[] { 0, 0, 0 };
                float[] parentOrigin = parentParsed.origin != null ? parentParsed.origin : new float[] { 0, 0, 0 };
                
                MeshModelBox.Builder meshBuilder = new MeshModelBox.Builder(true, texWidth, texHeight);
                for (Map.Entry<String, MeshFace> meshFace : faces.entrySet()) {
                    MeshFace face = meshFace.getValue();
                    if (face.vertices.length > 2) {
                        // FIXME mesh face normal (cross product)
                        MeshFaceBuilder faceBuilder = meshBuilder.startFaceCalcNormal();
                        for (int i = 0; i < face.vertices.length; ++i) {
                            // FIXME mesh vertices order
                            String vertexId = face.vertices[i];
                            
                            float[] vertexPos = vertices.get(vertexId);
                            float[] vertexUv = face.uv.get(vertexId);
                            faceBuilder.withVertex(
                                    vertexPos[0] + origin[0] - parentOrigin[0], 
                                    vertexPos[1] + origin[1] - parentOrigin[1], 
                                    vertexPos[2] + origin[2] - parentOrigin[2], 
                                    vertexUv[0], vertexUv[1]);
                        }
                        faceBuilder.createFace();
                    }
                }
                modelCubesCollection.add(meshBuilder.buildCube());
            }
        }
        
        static class ElementCube extends Element {
            boolean box_uv;
            boolean rescale;
            float[] from;
            float[] to;
            int autouv;
            float inflate;
            float uv_offset[];
            Map<String, BoxFace> faces;
            
            class BoxFace {
                float[] uv;
                Integer texture;
            }
            
            private ModelRenderer.ModelBox makeModelBox(float texWidth, float texHeight, GroupParsed parentParsed) {
                float size[] = { 
                        to[0] - from[0], 
                        to[1] - from[1], 
                        to[2] - from[2] };
                
                Vector3f originJ = new Vector3f(
                      -(from[0] - parentParsed.origin[0]),
                        -(to[1] - parentParsed.origin[1]) + size[1],
                          to[2] - parentParsed.origin[2]
                        );
                
                float x0 = originJ.x() - inflate - size[0];
                float y0 = originJ.y() - inflate - size[1];
                float z0 = originJ.z() - inflate - size[2];
                float x1 = originJ.x() + inflate;
                float y1 = originJ.y() + inflate;
                float z1 = originJ.z() + inflate;
                
                ModelRenderer.ModelBox box = new ModelRenderer.ModelBox(
                        0, 0, 
                        x0, y0, z0, 
                        size[0], size[1], size[2], 
                        0, 0, 0, 
                        false, texWidth, texHeight);
                
                ModelRenderer.TexturedQuad[] polygons = new ModelRenderer.TexturedQuad[6];
                
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
                Map<Direction, BoxFace> perFaceUv = faces();
                for (Direction direction : Direction.values()) {
                    Direction uvPart = direction.getAxis() == Axis.Z ? direction : direction.getOpposite();
                    if (perFaceUv.containsKey(uvPart)) {
                        BoxFace uv = perFaceUv.get(uvPart);
//                        if (uv.texture != null) {
                            float u0;
                            float v0;
                            float u1;
                            float v1;
                            if (direction.getAxis() == Axis.Y) {
                                u0 = uv.uv[2];
                                v0 = uv.uv[3];
                                u1 = uv.uv[0];
                                v1 = uv.uv[1];
                            }
                            else {
                                u0 = uv.uv[0];
                                v0 = uv.uv[1];
                                u1 = uv.uv[2];
                                v1 = uv.uv[3];
                            }
                            polygons[polygonsCount++] = new ModelRenderer.TexturedQuad(faceVertices.get(direction), 
                                    u0, v0, u1, v1, 
                                    texWidth, texHeight, false, direction);
//                        }
                    }
                }
                if (polygonsCount < polygons.length) {
                    polygons = Arrays.copyOf(polygons, polygonsCount);
                }
                ClientReflection.setPolygons(box, polygons);
                
                return box;
            }
            
            private Map<Direction, BoxFace> faces() {
                Map<Direction, BoxFace> facesPerDirection = new EnumMap<>(Direction.class);
                for (Direction direction : Direction.values()) {
                    if (this.faces.containsKey(direction.getName())) {
                        facesPerDirection.put(direction, this.faces.get(direction.getName()));
                    }
                }
                return facesPerDirection;
            }

            @Override
            void addElement(ModelRenderer parent, GroupParsed parentParsed, 
                    List<ModelRenderer.ModelBox> modelCubesCollection, int texWidth, int texHeight) {
                modelCubesCollection.add(makeModelBox(texWidth, texHeight, parentParsed));
            }
        }
        
        
        static class GroupParsed implements BlockbenchObj {
            String name;
            float[] origin;
            @Nullable float[] rotation;
            UUID uuid;
            boolean export;
            boolean mirror_uv;
            boolean visibility;
            int autoUv;
            List<BlockbenchObj> children;
            
            private int convertedCount = 0;
            
            ModelRenderer makeModelPart(int texWidth, int texHeight, @Nullable GroupParsed parent) {
                ModelRenderer modelPart = new ModelRenderer(texWidth, texHeight, 0, 0);
                
                float yOffset = 24;
                
                Vector3f modelPartPos;
                if (parent != null) {
                    float[] parentPivot = parent.origin;
                    
                    modelPartPos = new Vector3f(
                              origin[0] - parentPivot[0],
                            -(origin[1] - parentPivot[1]),
                              origin[2] - parentPivot[2]
                            );
                }
                else {
                    modelPartPos = new Vector3f(
                             origin[0], 
                            -origin[1] + yOffset, 
                             origin[2]);
                }
                
                modelPart.x = -modelPartPos.x();
                modelPart.y = modelPartPos.y();
                modelPart.z = modelPartPos.z();
                
                if (rotation != null) {
                    modelPart.xRot = -rotation[0] * MathUtil.DEG_TO_RAD;
                    modelPart.yRot = -rotation[1] * MathUtil.DEG_TO_RAD;
                    modelPart.zRot =  rotation[2] * MathUtil.DEG_TO_RAD;
                }
                
                return modelPart;
            }
        }
        
        static interface BlockbenchObj {
            
            static final JsonDeserializer<BlockbenchObj> DESERIALIZER = new JsonDeserializer<BlockbenchObj>() {

                @Override
                public BlockbenchObj deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    UUID uuid = null;
                    try {
                        uuid = context.deserialize(json, UUID.class);
                    }
                    catch (JsonParseException e) {}

                    if (uuid != null) {
                        ElementUUID uuidChild = new ElementUUID(uuid);
                        return uuidChild;
                    }
                    else {
                        return context.deserialize(json, GroupParsed.class);
                    }
                }
            };
        }
        
        static class ElementUUID implements BlockbenchObj {
            UUID uuid;
            
            private ElementUUID() {}
            
            private ElementUUID(UUID uuid) {
                this.uuid = uuid;
            }
        }
        
        
        
        @Override
        public void afterParse(ResourceLocation modelId) {
            if (SILVER_CHARIOT_ARMOR.equals(modelId)) {
                outliner.forEach(ModelParsed::fixArmorName);
            }
        }
        private static void fixArmorName(BlockbenchObj obj) {
            if (obj instanceof GroupParsed) {
                GroupParsed group = (GroupParsed) obj;
                group.name = group.name.replace("_armor", "");
                for (BlockbenchObj child : group.children) {
                    fixArmorName(child);
                }
            }
        }
        
        
        private Map<UUID, ModelParsed.Element> initElements = new HashMap<>();
        @Override
        public EntityModelUnbaked createUnbakedModel() {
            EntityModelUnbaked model = new EntityModelUnbaked(resolution.width, resolution.height);
            
            for (ModelParsed.Element element : elements) {
                initElements.put(element.uuid, element);
            }

            for (ModelParsed.BlockbenchObj bbObj : outliner) {
                addBlockbenchObjectRecursive(model, bbObj, null, null, null);
            }
            
            return model;
        }
        
        void addBlockbenchObjectRecursive(EntityModelUnbaked model, ModelParsed.BlockbenchObj bbObj, 
                @Nullable List<ModelRenderer.ModelBox> parentCubesCollection, 
                @Nullable ModelRenderer parent, @Nullable ModelParsed.GroupParsed parentParsed) {
            if (bbObj instanceof ModelParsed.GroupParsed) {
                ModelParsed.GroupParsed group = (ModelParsed.GroupParsed) bbObj;
                if (!group.export) return;
                
                ModelRenderer modelPart = group.makeModelPart(model.texWidth, model.texHeight, parentParsed);
                model.addModelPart(group.name, modelPart, parentParsed != null ? parentParsed.name : null);
                
                if (group.children != null) {
                    ObjectList<ModelRenderer.ModelBox> childModelCubes = new ObjectArrayList<>();
                    for (ModelParsed.BlockbenchObj child : group.children) {
                        addBlockbenchObjectRecursive(model, child, childModelCubes, modelPart, group);
                    }
                    ClientReflection.setCubes(modelPart, childModelCubes);
                }
            }
            else if (bbObj instanceof ModelParsed.ElementUUID && parent != null) {
                ModelParsed.Element element = initElements.get(((ModelParsed.ElementUUID) bbObj).uuid);

                Optional<GroupParsed> cubeRotated = element.convertRotated(parentParsed);
                if (cubeRotated.isPresent()) {
                    GroupParsed autoGenRotatedCube = cubeRotated.get();
                    addBlockbenchObjectRecursive(model, autoGenRotatedCube, null, parent, parentParsed);
                }
                else {
                    element.addElement(parent, parentParsed, parentCubesCollection, model.texWidth, model.texHeight);
                }
            }
        }
    }
}
