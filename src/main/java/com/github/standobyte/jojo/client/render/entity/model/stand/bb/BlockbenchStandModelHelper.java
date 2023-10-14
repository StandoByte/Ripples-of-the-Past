package com.github.standobyte.jojo.client.render.entity.model.stand.bb;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.pose.XRotationModelRenderer;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

public class BlockbenchStandModelHelper {

    /*
     * Allows adding models exported from Blockbench with minimal edits to the model file
     * 
     * Add the .java model file exported from Blockbench to your project, 
     * then create another class for your actual model (which will contain stuff like animations).
     * Call this method from your model's constructor to copy the ModelRenderer values from the Blockbench method.
     * 
     * Example: CrazyDiamondModel2 and CrazyDiamondModelConvertExample.
     * CrazyDiamondModel2 can be used interchangeably with CrazyDiamondModel in CrazyDiamondRenderer.
     *
     */
    public static void fillFromBlockbenchExport(
            EntityModel<?> bbSourceModel, EntityModel<?> inModModel) {
        Field[] bbModelPartFields = bbSourceModel.getClass().getDeclaredFields();
        Map<String, ModelRenderer> bbModelParts = new HashMap<>();

        try {
            for (Field bbModelPartField : bbModelPartFields) {
                if (bbModelPartField.getType() == ModelRenderer.class) {
                    bbModelPartField.setAccessible(true);
                    bbModelParts.put(bbModelPartField.getName(), (ModelRenderer) bbModelPartField.get(bbSourceModel));
                }
            }
            
            replaceModelParts(inModModel, bbModelParts);
        } catch (Exception e) {
            JojoMod.getLogger().error("Failed to add model parts to {} via Blockbench helper", inModModel.getClass().getName());
            e.printStackTrace();
        }
        
        inModModel.texWidth = bbSourceModel.texWidth;
        inModModel.texHeight = bbSourceModel.texHeight;
    }
    
    private static void replaceModelParts(EntityModel<?> inModModel, Map<String, ModelRenderer> source) throws IllegalArgumentException, IllegalAccessException {
        List<Field> inModModelParts = FieldUtils.getAllFieldsList(inModModel.getClass()).stream()
                .filter(field -> ModelRenderer.class.isAssignableFrom(field.getType()))
                .collect(Collectors.toList());
        List<ModelRenderer> modelParts = new ArrayList<>();
        Map<ModelRenderer, ModelRenderer> remapChildren = new HashMap<>();
        
        for (Map.Entry<String, ModelRenderer> entry : source.entrySet()) {
            String name = entry.getKey();
            ModelRenderer blockbenchPart = entry.getValue();
            
            Iterator<Field> it = inModModelParts.iterator();
            while (it.hasNext()) {
                Field inModPartField = it.next();
                if (inModPartField.getName().equals(name)) {
                    boolean xRotJank = false;
                    if (!inModPartField.getType().isAssignableFrom(blockbenchPart.getClass())) {
                        if (inModPartField.getType() == XRotationModelRenderer.class && blockbenchPart.getClass() == ModelRenderer.class) {
                            xRotJank = true;
                        }
                        else {
                            RuntimeException e = new ClassCastException(blockbenchPart.getClass() + " can't be cast to " + inModPartField.getType());
                            e.printStackTrace();
                            throw e;
                        }
                    }
                    
                    inModPartField.setAccessible(true);
                    
                    if (xRotJank) {
                        ModelRenderer jank = jankToKeepAddonsWorkingForNow(blockbenchPart);
                        remapChildren.put(blockbenchPart, jank);
                        blockbenchPart = jank;
                    }
                    modelParts.add(blockbenchPart);
                    inModPartField.set(inModModel, blockbenchPart);
                    
                    it.remove();
                }
            }
        }
        
        for (ModelRenderer modelPart : modelParts) {
            ObjectList<ModelRenderer> children = ClientReflection.getChildren(modelPart);
            if (!children.isEmpty()) {
                remapChildren.forEach((oldPart, newPart) -> {
                    Collections.replaceAll(children, oldPart, newPart);
                });
            }
        }
    }
    
    private static XRotationModelRenderer jankToKeepAddonsWorkingForNow(ModelRenderer modelPart) {
        XRotationModelRenderer deepCopy = new XRotationModelRenderer(256, 256, 0, 0);
        
        deepCopy.x = modelPart.x;
        deepCopy.y = modelPart.y;
        deepCopy.z = modelPart.z;
        deepCopy.xRot = modelPart.xRot;
        deepCopy.yRot = modelPart.yRot;
        deepCopy.zRot = modelPart.zRot;
        deepCopy.mirror = modelPart.mirror;
        deepCopy.visible = modelPart.visible;
        
        ClientReflection.setCubes(deepCopy, ClientReflection.getCubes(modelPart));
        ClientReflection.setChildren(deepCopy, ClientReflection.getChildren(modelPart));
        
        return deepCopy;
    }
    
    
    
    public static <M extends StandEntityModel<?>> void fillFromGeckoJson(JsonObject json, M inModModel) {
        JsonArray geometryArray = json.getAsJsonArray("minecraft:geometry");
        JsonObject geometry = geometryArray.get(0).getAsJsonObject();
        
        JsonObject description = geometry.getAsJsonObject("description");
        int texWidth = (int) description.get("texture_width").getAsDouble();
        int texHeight = (int) description.get("texture_height").getAsDouble();
        inModModel.texWidth = texWidth;
        inModModel.texHeight = texHeight;
        
        JsonArray bonesJson = geometry.getAsJsonArray("bones");
        if (bonesJson != null) {
            Map<String, ParsedBone> bones = new HashMap<>();
            for (JsonElement boneJsonElement : bonesJson) {
                ParsedBone bone = ParsedBone.fromJson(boneJsonElement.getAsJsonObject());
                bones.put(bone.name, bone);
            }

            Map<String, ModelRenderer> modelPartsNamed = new HashMap<>();
            for (ParsedBone boneParsed : bones.values()) {
                ModelRenderer modelPart = boneParsed.createModelRenderer(
                        texWidth, texHeight, boneParsed.parentName.map(bones::get));
                modelPartsNamed.put(boneParsed.name, modelPart);
            }
            
            for (ParsedBone boneParsed : bones.values()) {
                boneParsed.parentName.ifPresent(parentName -> {
                    ModelRenderer parent = modelPartsNamed.get(parentName);
                    ModelRenderer child = modelPartsNamed.get(boneParsed.name);
                    if (parent != null && child != null) {
                        parent.addChild(child);
                    }
                });
            }
            
            try {
                replaceModelParts(inModModel, modelPartsNamed);
            } catch (Exception e) {
                JojoMod.getLogger().error("Failed to import Geckolib format model as {}", inModModel.getClass().getName());
                e.printStackTrace();
            }
        }
    }
    
    private static class ParsedBone {
        public final String name;
        public final Optional<String> parentName;
        public final Vector3f pivot;
        public final Optional<Vector3f> rotation;
        public final List<ParsedCube> cubes;
        
        public ParsedBone(String name, Optional<String> parentName, Vector3f pivot, 
                Optional<Vector3f> rotation, ParsedCube... cubes) {
            this.name = name;
            this.parentName = parentName;
            this.pivot = pivot;
            this.rotation = rotation;
            this.cubes = ImmutableList.copyOf(cubes);
        }
        
        public static ParsedBone fromJson(JsonObject json) {
            String name = json.get("name").getAsString();
            Optional<String> parentName = Optional.ofNullable(json.get("parent")).map(JsonElement::getAsString);
            Vector3f pivot = Optional.ofNullable(json.get("pivot")).map(JsonElement::getAsJsonArray)
                    .map(arr -> vecFromJsonArray(arr))
                    .orElse(new Vector3f());
            Optional<Vector3f> rotation = Optional.ofNullable(json.get("rotation")).map(JsonElement::getAsJsonArray)
                    .map(arr -> vecFromJsonArray(arr));
            ParsedCube[] cubes = Optional.ofNullable(json.get("cubes")).map(JsonElement::getAsJsonArray).map(cubesJson -> {
                return StreamSupport.stream(cubesJson.spliterator(), false)
                .map(JsonElement::getAsJsonObject).map(ParsedCube::fromJson)
                .toArray(ParsedCube[]::new);
            }).orElse(new ParsedCube[0]);
            
            return new ParsedBone(name, parentName, pivot, rotation, cubes);
        }
        
        public ModelRenderer createModelRenderer(int texWidth, int texHeight, Optional<ParsedBone> parentBone) {
            ModelRenderer modelRenderer = new ModelRenderer(texWidth, texHeight, 0, 0);
            
            float yOffset = 24;
            
            Vector3f modelPartPos;
            if (parentBone.isPresent()) {
                Vector3f parentPivot = parentBone.get().pivot;
                
                modelPartPos = new Vector3f(
                        pivot.x() - parentPivot.x(),
                        -(pivot.y() - parentPivot.y()),
                        pivot.z() - parentPivot.z()
                        );
            }
            else {
                modelPartPos = new Vector3f(
                        pivot.x(), 
                        -pivot.y() + yOffset, 
                        pivot.z());
            }
            
            modelRenderer.x = modelPartPos.x();
            modelRenderer.y = modelPartPos.y();
            modelRenderer.z = modelPartPos.z();
            
            rotation.ifPresent(rotVec -> {
                modelRenderer.xRot = rotVec.x() * MathUtil.DEG_TO_RAD;
                modelRenderer.yRot = rotVec.y() * MathUtil.DEG_TO_RAD;
                modelRenderer.zRot = rotVec.z() * MathUtil.DEG_TO_RAD;
            });
            
            ObjectList<ModelRenderer.ModelBox> modelCubes = new ObjectArrayList<>();
            int convertedCount = 0;
            for (ParsedCube cubeParsed : cubes) {
                Optional<ParsedBone> cubeRotated = cubeParsed.convertRotated(this, ++convertedCount);
                if (cubeRotated.isPresent()) {
                    modelRenderer.addChild(cubeRotated.get().createModelRenderer(texWidth, texHeight, Optional.of(this)));
                }
                else {
                    modelCubes.add(cubeParsed.createModelBox(texWidth, texHeight, this));
                }
            }
            ClientReflection.setCubes(modelRenderer, modelCubes);
            
            return modelRenderer;
        }
    }
    
    private static class ParsedCube {
        public final Vector3f origin;
        public final Vector3f size;
        public final float inflate;
        public final Optional<Vector3f> pivot;
        public final Optional<Vector3f> rotation;
        private Optional<Map<Direction, ParsedUv>> perFaceUvSupport = Optional.empty();
        private int[] boxUvPos = {0, 0};
        public final boolean mirror;
        
        private ParsedCube(Vector3f origin, Vector3f size, float inflate, Optional<Vector3f> pivot, 
                Optional<Vector3f> rotation, boolean mirror) {
            this.origin = origin;
            this.size = size;
            this.inflate = inflate;
            this.pivot = pivot;
            this.rotation = rotation;
            this.mirror = mirror;
        }
        
        public static ParsedCube fromJson(JsonObject json) {
            Vector3f origin = Optional.ofNullable(json.get("origin")).map(JsonElement::getAsJsonArray)
                    .map(arr -> vecFromJsonArray(arr)).orElse(new Vector3f());
            Vector3f size = vecFromJsonArray(json.get("size").getAsJsonArray());
            double inflate = json.has("inflate") ? json.get("inflate").getAsDouble() : 0;
            Optional<Vector3f> pivot = Optional.ofNullable(json.get("pivot")).map(JsonElement::getAsJsonArray)
                    .map(arr -> vecFromJsonArray(arr));
            Optional<Vector3f> rotation = Optional.ofNullable(json.get("rotation")).map(JsonElement::getAsJsonArray)
                    .map(arr -> vecFromJsonArray(arr));
            boolean mirror = Optional.ofNullable(json.get("mirror")).map(elem -> elem.isJsonPrimitive() ? elem.getAsBoolean() : false).orElse(false);
            
            ParsedCube cube = new ParsedCube(origin, size, 
                    (float) inflate, pivot, rotation, mirror);
            
            JsonElement uvJsonElem = json.get("uv");
            if (uvJsonElem.isJsonArray()) { // box uv
                JsonArray uvJsonArr = uvJsonElem.getAsJsonArray();
                double u0 = uvJsonArr.get(0).getAsDouble();
                double v0 = uvJsonArr.get(1).getAsDouble();
                cube.boxUvPos[0] = (int) u0;
                cube.boxUvPos[1] = (int) v0;
            }
            
            else if (uvJsonElem.isJsonObject()) { // per-face uv
                JsonObject uvJson = uvJsonElem.getAsJsonObject();
                Map<Direction, ParsedUv> uv = new EnumMap<>(Direction.class);
                for (Direction direction : Direction.values()) {
                    JsonObject faceJson = uvJson.get(direction.name().toLowerCase()).getAsJsonObject();
                    JsonArray faceUv = faceJson.get("uv").getAsJsonArray();
                    JsonArray faceUvSize = faceJson.get("uv_size").getAsJsonArray();
                    uv.put(direction, new ParsedUv(
                            (float) faceUv.get(0).getAsDouble(),     (float) faceUv.get(1).getAsDouble(), 
                            (float) faceUvSize.get(0).getAsDouble(), (float) faceUvSize.get(1).getAsDouble()));
                    cube.perFaceUvSupport = Optional.of(uv);
                }
            }
            
            return cube;
        }
        
        public Optional<ParsedBone> convertRotated(ParsedBone parentBone, int convertedCount) {
            return rotation.map(rotVec -> {
                String name = parentBone.name + "_r" + convertedCount;
                Optional<String> parentName = Optional.of(parentBone.name);
                ParsedCube cube = new ParsedCube(origin, size, inflate, Optional.empty(), Optional.empty(), mirror);
                cube.boxUvPos = this.boxUvPos;
                cube.perFaceUvSupport = this.perFaceUvSupport;
                return new ParsedBone(name, parentName, pivot.orElse(new Vector3f()), Optional.of(rotVec), cube);
            });
        }
        
        public ModelRenderer.ModelBox createModelBox(float texWidth, float texHeight, ParsedBone parentBone) {
            Vector3f originJ = new Vector3f(
                    origin.x() - parentBone.pivot.x(),
                    -(origin.y() - parentBone.pivot.y()) - size.y(),
                    origin.z() - parentBone.pivot.z()
                    );
            
            ModelRenderer.ModelBox box = new ModelRenderer.ModelBox(
                    boxUvPos[0], boxUvPos[1], 
                    originJ.x(), originJ.y(), originJ.z(), 
                    size.x(), size.y(), size.z(), 
                    inflate, inflate, inflate, 
                    mirror, texWidth, texHeight);
            
            perFaceUvSupport.ifPresent(perFaceUv -> {
                ModelRenderer.TexturedQuad[] polygons = new ModelRenderer.TexturedQuad[6];
                float x1 = originJ.x() + size.x();
                float y1 = originJ.y() + size.y();
                float z1 = originJ.z() + size.z();
                float originX = originJ.x() - inflate;
                float originY = originJ.y() - inflate;
                float originZ = originJ.z() - inflate;
                x1 = x1 + inflate;
                y1 = y1 + inflate;
                z1 = z1 + inflate;
                
                ModelRenderer.PositionTextureVertex x0y0z0 = new ModelRenderer.PositionTextureVertex(originX, originY, originZ, 0.0F, 0.0F);
                ModelRenderer.PositionTextureVertex x1y0z0 = new ModelRenderer.PositionTextureVertex(x1,      originY, originZ, 0.0F, 8.0F);
                ModelRenderer.PositionTextureVertex x1y1z0 = new ModelRenderer.PositionTextureVertex(x1,      y1,      originZ, 8.0F, 8.0F);
                ModelRenderer.PositionTextureVertex x0y1z0 = new ModelRenderer.PositionTextureVertex(originX, y1,      originZ, 8.0F, 0.0F);
                ModelRenderer.PositionTextureVertex x0y0z1 = new ModelRenderer.PositionTextureVertex(originX, originY, z1,      0.0F, 0.0F);
                ModelRenderer.PositionTextureVertex x1y0z1 = new ModelRenderer.PositionTextureVertex(x1,      originY, z1,      0.0F, 8.0F);
                ModelRenderer.PositionTextureVertex x1y1z1 = new ModelRenderer.PositionTextureVertex(x1,      y1,      z1,      8.0F, 8.0F);
                ModelRenderer.PositionTextureVertex x0y1z1 = new ModelRenderer.PositionTextureVertex(originX, y1,      z1,      8.0F, 0.0F);
                
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
                for (Direction direction : Direction.values()) { // FIXME fix per-face UV support
                    if (perFaceUv.containsKey(direction)) {
                        ParsedUv uv = perFaceUv.get(direction);
                        polygons[polygonsCount++] = new ModelRenderer.TexturedQuad(faceVertices.get(direction), 
                                uv.u, uv.u + uv.uWidth, uv.v, uv.v + uv.vHeight, texWidth, texHeight, false, direction);
                    }
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
//                float u0 = texCoordU;
//                float u1 = texCoordU + size.z();
//                float u2 = texCoordU + size.z() + size.x();
//                float u4 = texCoordU + size.z() + size.x() + size.x();
//                float u3 = texCoordU + size.z() + size.x() + size.z();
//                float u5 = texCoordU + size.z() + size.x() + size.z() + size.x();
//                float v0 = texCoordV;
//                float v1 = texCoordV + size.z();
//                float v2 = texCoordV + size.z() + size.y();
//                polygons[2] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
//                        x1y0z1, 
//                        x0y0z1, 
//                        x0y0z0, 
//                        x1y0z0}, 
//                        u1, v0, u2, v1, texWidth, texHeight, mirror, Direction.DOWN);
//                polygons[3] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
//                        x1y1z0, 
//                        x0y1z0, 
//                        x0y1z1, 
//                        x1y1z1}, u2, v1, u4, v0, texWidth, texHeight, mirror, Direction.UP);
//                polygons[1] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
//                        x0y0z0, 
//                        x0y0z1, 
//                        x0y1z1, 
//                        x0y1z0}, u0, v1, u1, v2, texWidth, texHeight, mirror, Direction.WEST);
//                polygons[4] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
//                        x1y0z0, 
//                        x0y0z0, 
//                        x0y1z0, 
//                        x1y1z0}, u1, v1, u2, v2, texWidth, texHeight, mirror, Direction.NORTH);
//                polygons[0] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
//                        x1y0z1, 
//                        x1y0z0, 
//                        x1y1z0, 
//                        x1y1z1}, u2, v1, u3, v2, texWidth, texHeight, mirror, Direction.EAST);
//                polygons[5] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
//                        x0y0z1, 
//                        x1y0z1, 
//                        x1y1z1, 
//                        x0y1z1}, u3, v1, u5, v2, texWidth, texHeight, mirror, Direction.SOUTH);
            });
            
            return box;
        }
        
        public static class ParsedUv {
            public float u;
            public float v;
            public float uWidth;
            public float vHeight;
            
            public ParsedUv(float u, float v, float uWidth, float vHeight) {
                this.u = u;
                this.v = v;
                this.uWidth = uWidth;
                this.vHeight = vHeight;
            }
        }
    }
        
    
    private static Vector3f vecFromJsonArray(JsonArray arr) {
        return new Vector3f((float) arr.get(0).getAsDouble(), (float) arr.get(1).getAsDouble(), (float) arr.get(2).getAsDouble());
    }
}
