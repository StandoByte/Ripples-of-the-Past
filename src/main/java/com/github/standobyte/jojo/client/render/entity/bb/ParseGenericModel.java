package com.github.standobyte.jojo.client.render.entity.bb;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.MeshModelBox;
import com.github.standobyte.jojo.client.render.MeshModelBox.Builder.MeshFaceBuilder;
import com.github.standobyte.jojo.util.general.MathUtil;
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
    public static final Gson GSON = new GsonBuilder()
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
    
    
    
    public static class ModelParsed implements IParsedModel {
        Resolution resolution;
        List<Element> elements;
        List<BlockbenchObj> outliner;
        
        static class Resolution {
            int width;
            int height;
        }
        
        
        public static abstract class Element {
            boolean export = true;
            
            String name;
            UUID uuid;
            boolean visibility;
            float[] origin;
            float[] rotation;
            String render_order;
            boolean allow_mirror_modeling;
            
            public abstract ModelRenderer.ModelBox makeCube(float[] parentOrigin, int texWidth, int texHeight);
            
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
                                        JojoMod.getLogger().error(e);
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
                    
                    bone.export = this.export;
                    
                    return Optional.of(bone);
                }
                
                return Optional.empty();
            }
        }
        
        public static class ElementMesh extends Element {
            Map<String, float[]> vertices;
            Map<String, MeshFace> faces;
            
            class MeshFace {
                Map<String, float[]> uv;
                String[] vertices;
                int texture;
            }

            private static final Set<String> visitedVertices = new LinkedHashSet<>(4);
            @Override
            public ModelRenderer.ModelBox makeCube(float[] parentOrigin, int texWidth, int texHeight) {
                if (origin == null) origin = new float[] { 0, 0, 0 };
                if (parentOrigin == null) parentOrigin = new float[] { 0, 0, 0 };
                
                MeshModelBox.Builder meshBuilder = new MeshModelBox.Builder(true, texWidth, texHeight);
                for (Map.Entry<String, MeshFace> meshFace : faces.entrySet()) {
                    MeshFace face = meshFace.getValue();
                    if (face.vertices.length > 2) {
                        visitedVertices.clear();
                        for (String vertex : face.vertices) {
                            visitedVertices.add(vertex);
                        }
                        Vertex[] verticesArr = new Vertex[visitedVertices.size()];
                        int i = 0;
                        for (String vertexId : visitedVertices) {
                            verticesArr[i++] = new Vertex(vertices.get(vertexId), face.uv.get(vertexId));
                        }
                        sortVertices(verticesArr);
                        
                        MeshFaceBuilder faceBuilder = meshBuilder.startFaceCalcNormal();
                        for (Vertex vertex : verticesArr) {
                            faceBuilder.withVertex(
                                    vertex.pos[0] + origin[0] - parentOrigin[0], 
                                    vertex.pos[1] + origin[1] - parentOrigin[1], 
                                    vertex.pos[2] + origin[2] - parentOrigin[2], 
                                    vertex.uv[0], vertex.uv[1]);
                        }
                        faceBuilder.createFace();
                    }
                }
                
                return meshBuilder.buildCube();
            }
        }
        
        // record moment
        private static class Vertex {
            final float[] pos;
            final float[] uv;
            
            Vertex(final float[] pos, final float[] uv) {
                this.pos = pos;
                this.uv = uv;
            }
        }
        
        private static void sortVertices(Vertex[] vertices) {
            if (vertices.length < 4) return;

            if (MeshVerticesHelper.magicFunction(vertices[1].pos, vertices[2].pos, vertices[0].pos, vertices[3].pos)) {
                ArrayUtils.swap(vertices, 0, 1);
                ArrayUtils.swap(vertices, 0, 2);
            } else if (MeshVerticesHelper.magicFunction(vertices[0].pos, vertices[1].pos, vertices[2].pos, vertices[3].pos)) {
                ArrayUtils.swap(vertices, 1, 2);
            }
        }
        
        public static class ElementCube extends Element {
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
            public ModelRenderer.ModelBox makeCube(float[] parentOrigin, int texWidth, int texHeight) {
                float size[] = { 
                        to[0] - from[0], 
                        to[1] - from[1], 
                        to[2] - from[2] };
                
                Vector3f originJ = new Vector3f(
                      -(from[0] - parentOrigin[0]),
                        -(to[1] - parentOrigin[1]) + size[1],
                          to[2] - parentOrigin[2]
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
                box.polygons = polygons;
                
                return box;
            }
        }
        
        
        static class GroupParsed extends BlockbenchObj {
            String name;
            float[] origin;
            @Nullable float[] rotation;
            UUID uuid;
            boolean export = true;
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
        
        static abstract class BlockbenchObj {
            
            static final JsonDeserializer<BlockbenchObj> DESERIALIZER = new JsonDeserializer<BlockbenchObj>() {

                @Override
                public BlockbenchObj deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    UUID uuid = null;
                    try {
                        uuid = context.deserialize(json, UUID.class);
                    }
                    catch (JsonParseException e) {}

                    BlockbenchObj obj;
                    if (uuid != null) {
                        ElementUUID uuidChild = new ElementUUID(uuid);
                        obj = uuidChild;
                    }
                    else {
                        obj = context.deserialize(json, GroupParsed.class);
                    }
                    return obj;
                }
            };
        }
        
        static class ElementUUID extends BlockbenchObj {
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
                if (element.export) {
                    initElements.put(element.uuid, element);
                }
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
                    modelPart.cubes.clear();
                    modelPart.cubes.addAll(childModelCubes);
                }
            }
            else if (bbObj instanceof ModelParsed.ElementUUID && parent != null) {
                ModelParsed.Element element = initElements.get(((ModelParsed.ElementUUID) bbObj).uuid);
                if (element == null || !element.export) return;

                Optional<GroupParsed> cubeRotated = element.convertRotated(parentParsed);
                if (cubeRotated.isPresent()) {
                    GroupParsed autoGenRotatedCube = cubeRotated.get();
                    addBlockbenchObjectRecursive(model, autoGenRotatedCube, null, parent, parentParsed);
                }
                else {
                    parentCubesCollection.add(element.makeCube(parentParsed.origin, model.texWidth, model.texHeight));
                }
            }
        }
    }
}
