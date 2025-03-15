package com.github.standobyte.jojo.client.render.entity.animnew;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.StreamSupport;

import com.github.standobyte.jojo.client.render.entity.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.animnew.mojang.Transformation;
import com.github.standobyte.jojo.client.render.entity.animnew.mojang.Transformation.Interpolation;
import com.github.standobyte.jojo.client.render.entity.animnew.molang.KeyframeWithQuery;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;

public class ParseGeckoAnims {
    
    // "geckolib_format_version": 2
    public static Animation parseAnim(JsonObject animJson) {
        float lengthSecs = animJson.has("animation_length") ? animJson.get("animation_length").getAsFloat() : 0;
        Animation.Builder builder = Animation.Builder.create(lengthSecs);

        boolean loop = false;
        boolean holdOnLastFrame = false;
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
                String boneName = bone.getKey();
                JsonObject tfJson = bone.getValue().getAsJsonObject();
                parseKeyframes(builder, tfJson, "rotation", Transformation.Targets.ROTATE, boneName);
                parseKeyframes(builder, tfJson, "position", Transformation.Targets.TRANSLATE, boneName);
                parseKeyframes(builder, tfJson, "scale", Transformation.Targets.SCALE, boneName);
            }
        }
        
        return builder.build();
    }
    
    private static void parseKeyframes(Animation.Builder anim, JsonObject boneTfJson, String targetName, Transformation.Target target, String boneName) {
        JsonElement element = boneTfJson.get(targetName);
        if (element == null) return;
        Float2ObjectMap<KeyframeWithQuery> timeline = new Float2ObjectArrayMap<>();
        
        if (element.isJsonObject()) {
            JsonObject keyframesJson = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> rotationJson : keyframesJson.entrySet()) {
                float time;
                JsonElement rotation;
                try {
                    time = Float.parseFloat(rotationJson.getKey());
                    rotation = rotationJson.getValue();
                }
                catch (NumberFormatException singleKeyframeFormat) {
                    time = 0;
                    rotation = keyframesJson;
                }
                parseKeyframe(timeline, time, rotation);
            }
        }
        else {
            parseKeyframe(timeline, 0, element);
        }
        
        KeyframeWithQuery[] keyframes = keyframesToArray(timeline, KeyframeWithQuery[]::new);
        anim.addBoneAnimation(boneName, new Transformation(target, keyframes));
    }
    
    private static void parseKeyframe(Float2ObjectMap<KeyframeWithQuery> keyframesTimeline, float time, JsonElement keyframeValue) {
        Optional<JsonObject> keyframeObj = keyframeValue.isJsonObject() ? Optional.of(keyframeValue.getAsJsonObject()) : Optional.empty();
        
        JsonArray rotVecJson = keyframeObj.map(keyframe -> {
            JsonElement rotVecJsonElem = keyframe.get("vector");
            if (rotVecJsonElem == null && keyframe.has("post")) rotVecJsonElem = keyframe.get("post").getAsJsonObject().get("vector");
            return rotVecJsonElem.getAsJsonArray();
        }).orElseGet(() -> keyframeValue.isJsonArray() ? keyframeValue.getAsJsonArray() : null);
        
        String easingName = keyframeObj.map(keyframe -> {
            if (keyframe.has("easing")) {
                return keyframe.get("easing").getAsString();
            }
            if (keyframe.has("lerp_mode")) {
                return keyframe.get("lerp_mode").getAsString();
            }
            return null;
        }).orElse("linear");
        double[] easingArgs = keyframeObj.map(keyframe -> keyframe.get("easingArgs"))
                .map(JsonElement::getAsJsonArray)
                .map(json -> {
                    return StreamSupport.stream(json.spliterator(), false)
                    .mapToDouble(JsonElement::getAsDouble)
                    .toArray();
                })
                .orElse(new double[0]);
        
        KeyframeWithQuery rotVec = KeyframeWithQuery.parseJsonVec(rotVecJson);
        Interpolation lerp = Interpolations.getLerpMode(easingName, easingArgs);
        keyframesTimeline.put(time, rotVec.withKeyframe(time, lerp));
    }
    
    public static <T> T[] keyframesToArray(Float2ObjectMap<T> parsedTimeline, IntFunction<T[]> arrayConstructor) {
        return parsedTimeline.float2ObjectEntrySet().stream()
                .sorted(Comparator.comparingDouble(e -> e.getFloatKey()))
                .map(e -> e.getValue())
                .toArray(arrayConstructor);
    }
    
}
