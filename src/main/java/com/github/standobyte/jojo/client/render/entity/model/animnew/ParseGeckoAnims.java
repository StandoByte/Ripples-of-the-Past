package com.github.standobyte.jojo.client.render.entity.model.animnew;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.StreamSupport;

import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation.Interpolation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.molang.KeyframeWithQuery;
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
        boolean holdOnLastFrame = false; // TODO gecko animation parsing
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
                parseKeyframes(builder, tfJson, "position", Transformation.Targets.TRANSLATE, bone.getKey());
                parseKeyframes(builder, tfJson, "scale", Transformation.Targets.SCALE, bone.getKey());
            }
        }
        
        return builder.build();
    }
    
    private static void parseKeyframes(Animation.Builder anim, JsonObject boneTfJson, String targetName, Transformation.Target target, String boneName) {
        JsonObject keyframesJson = boneTfJson.getAsJsonObject(targetName);
        if (keyframesJson != null) {
            Float2ObjectMap<KeyframeWithQuery> timeline = new Float2ObjectArrayMap<>();
            for (Map.Entry<String, JsonElement> rotationJson : keyframesJson.entrySet()) {
                float time;
                JsonObject rotation;
                try {
                    time = Float.parseFloat(rotationJson.getKey());
                    rotation = rotationJson.getValue().getAsJsonObject();
                }
                catch (NumberFormatException singleKeyframe) {
                    time = 0;
                    rotation = keyframesJson; // in this case this object is not actually a json object mapping time to keyframes, but the keyframe itself
                }
                
                JsonElement rotVecJsonElem = rotation.get("vector");
                if (rotVecJsonElem == null && rotation.has("post")) rotVecJsonElem = rotation.get("post").getAsJsonObject().get("vector");
                JsonArray rotVecJson = rotVecJsonElem.getAsJsonArray();
                
                KeyframeWithQuery rotVec = KeyframeWithQuery.parseJsonVec(rotVecJson);
                
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
                timeline.put(time, rotVec.withKeyframe(time, lerp));
            }
            
            KeyframeWithQuery[] keyframes = keyframesToArray(timeline, KeyframeWithQuery[]::new);
            anim.addBoneAnimation(boneName, new Transformation(target, keyframes));
        }
    }
    
    public static <T> T[] keyframesToArray(Float2ObjectMap<T> parsedTimeline, IntFunction<T[]> arrayConstructor) {
        return parsedTimeline.float2ObjectEntrySet().stream()
                .sorted(Comparator.comparingDouble(e -> e.getFloatKey()))
                .map(e -> e.getValue())
                .toArray(arrayConstructor);
    }
    
}
