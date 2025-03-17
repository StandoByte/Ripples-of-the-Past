package com.github.standobyte.jojo.client.render.entity.animnew.molang;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.animnew.mojang.Keyframe;
import com.github.standobyte.jojo.client.render.entity.animnew.mojang.Transformation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.minecraft.util.math.vector.Vector3f;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.MochaFunction;

public class KeyframeWithQuery {
    private Keyframe keyframe;
    private final Vector3f keyframeTarget;
    @Nullable private final IFloatSupplier[] query;
    
    public static KeyframeWithQuery constant(Vector3f vec) {
        return new KeyframeWithQuery(vec, null);
    }
    
    private KeyframeWithQuery(Vector3f keyframeTarget, @Nullable IFloatSupplier[] query) {
        this.keyframeTarget = keyframeTarget;
        this.query = query;
    }
    
    public KeyframeWithQuery withKeyframe(float timestamp, Transformation.Interpolation interpolation) {
        keyframe = new Keyframe(timestamp, keyframeTarget, interpolation);
        return this;
    }
    
    public Keyframe getKeyframe() {
        return keyframe;
    }
    
    public void evaluate() {
        if (query != null) {
            keyframeTarget.set(query[0].get(), query[1].get(), query[2].get());
        }
    }
    
    
    public static KeyframeWithQuery parseJsonVec(JsonArray vecJson) {
        boolean isNumericLiteral = true;
        IFloatSupplier[] elements = new IFloatSupplier[3];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = IFloatSupplier.elementFromJson(vecJson.get(i));
            isNumericLiteral &= elements[i].isNumericLiteral();
        }
        if (isNumericLiteral) {
            return KeyframeWithQuery.constant(new Vector3f(elements[0].get(), elements[1].get(), elements[2].get()));
        }
        else {
            return new KeyframeWithQuery(new Vector3f(0, 0, 0), new IFloatSupplier[] { elements[0], elements[1], elements[2] });
        }
    }
    
    public static interface IFloatSupplier {
        float get();
        default boolean isNumericLiteral() { return false; }
        
        
        public static IFloatSupplier elementFromJson(JsonElement json) {
            if (!json.isJsonPrimitive()) {
                throw new IllegalArgumentException();
            }
            JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
            try {
                return new IFloatSupplier.Numeric(jsonPrimitive.getAsFloat());
            }
            catch (NumberFormatException e) {
                String string = jsonPrimitive.getAsString();
                return new IFloatSupplier.Molang(string, !string.contains(AnimMolangQuery.NAMESPACE));
            }
        }
        
        public static class Numeric implements IFloatSupplier {
            protected final float value;
            
            public Numeric(float value) {
                this.value = value;
            }

            @Override
            public float get() {
                return value;
            }
            
            @Override
            public boolean isNumericLiteral() {
                return true;
            }
            
        }
        
        public static class Molang implements IFloatSupplier {
            protected final MochaFunction function;
            protected boolean compile;
            
            public Molang(String expression, boolean compile) {
                this.function = makeFunction(expression, compile);
            }
            
            private static MochaFunction makeFunction(String molang, boolean compile) {
                MochaEngine<?> interpreter = MolangInterpreter.get();
                if (compile) {
                    try {
                        MochaFunction function = interpreter.compile(molang);
                        return function;
                    }
                    catch (Exception e) {
                        JojoMod.getLogger().error("Failed to compile a Molang expression ({}) into bytecode.", molang, e);
                    }
                }
                MochaFunction function = interpreter.prepareEval(molang);
                return function;
            }
            
            @Override
            public float get() {
                return (float) function.evaluate();
            }
            
        }
    }
    
}
