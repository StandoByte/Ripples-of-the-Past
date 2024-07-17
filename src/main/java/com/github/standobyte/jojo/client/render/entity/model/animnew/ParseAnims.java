package com.github.standobyte.jojo.client.render.entity.model.animnew;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Keyframe;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation.Interpolation;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import net.minecraft.util.math.vector.Vector3f;

public class ParseAnims {
    
    
    
    public static Animation parseAnim(JsonObject animJson) {
        float lengthSecs = animJson.get("animation_length").getAsFloat();
        Animation.Builder builder = Animation.Builder.create(lengthSecs);

        boolean loop = false;
        boolean holdOnLastFrame = false; // TODO
        JsonElement loopJson = animJson.get("loop");
        if (loopJson != null && loopJson.isJsonPrimitive()) {
            String loopMode = loopJson.getAsString();
            if ("hold_on_last_frame".equals(loopMode)) {
                holdOnLastFrame = true;
            }
            else {
                loop = loopJson.getAsBoolean();
            }
        }
        if (loop) {
            builder.looping();
        }
        
        JsonObject boneAnims = animJson.getAsJsonObject("bones");
        if (boneAnims != null) {
            for (Map.Entry<String, JsonElement> bone : boneAnims.entrySet()) {
                JsonObject tfJson = bone.getValue().getAsJsonObject();
                parseKeyframes(builder, tfJson, "rotation", Transformation.Targets.ROTATE, bone.getKey());
            }
        }
        
        return builder.build();
    }
    
    private static void parseKeyframes(Animation.Builder anim, JsonObject boneTfJson, String targetName, Transformation.Target target, String boneName) {
        JsonObject keyframesJson = boneTfJson.getAsJsonObject(targetName);
        if (keyframesJson != null) {
            Float2ObjectMap<Keyframe> timeline = new Float2ObjectArrayMap<>();
            for (Map.Entry<String, JsonElement> rotationJson : keyframesJson.entrySet()) {
                float time = Float.parseFloat(rotationJson.getKey());
                
                JsonObject rotation = rotationJson.getValue().getAsJsonObject();
                JsonElement rotVecJsonElem = rotation.get("vector");
                if (rotVecJsonElem == null && rotation.has("post")) rotVecJsonElem = rotation.get("post").getAsJsonObject().get("vector");
                JsonArray rotVecJson = rotVecJsonElem.getAsJsonArray();
                Vector3f rotVec = fromJson(rotVecJson);
                if ("rotation".equals(targetName)) {
                    rotVec.mul(MathUtil.DEG_TO_RAD);
                }
                
                String easingName = Optional.ofNullable(rotation.get("easing"))
                        .map(JsonElement::getAsString)
                        .orElse(rotation.has("lerp_mode") ? rotation.get("lerp_mode").getAsString() : "linear");
                double[] easingArgs = Optional.ofNullable(rotation.get("easingArgs"))
                        .map(JsonElement::getAsJsonArray)
                        .map(json -> {
                            return StreamSupport.stream(json.spliterator(), false)
                            .mapToDouble(JsonElement::getAsDouble)
                            .toArray();
                        })
                        .orElse(new double[0]);
                Interpolation lerp = Interpolations.getLerpMode(easingName, easingArgs);
                timeline.put(time, new Keyframe(time, rotVec, lerp));
            }
            
            Keyframe[] keyframes = timeline.float2ObjectEntrySet().stream()
                    .sorted(Comparator.comparingDouble(e -> e.getFloatKey()))
                    .map(e -> e.getValue())
                    .toArray(Keyframe[]::new);
            anim.addBoneAnimation(boneName, new Transformation(target, keyframes));
        }
    }
    
    private static Vector3f fromJson(JsonArray array) {
        return new Vector3f(
                array.get(0).getAsFloat(),
                array.get(1).getAsFloat(),
                array.get(2).getAsFloat());
    }
}
