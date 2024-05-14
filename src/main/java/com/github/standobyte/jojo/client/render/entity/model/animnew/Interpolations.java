package com.github.standobyte.jojo.client.render.entity.model.animnew;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Keyframe;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation.Interpolation;
import com.github.standobyte.jojo.util.general.MathUtil;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.util.math.vector.Vector3f;

public class Interpolations {
    private static final Map<String, Interpolation> LERP_MODES = new HashMap<>();
    private static final Map<String, Function<Float, Interpolation>> ARG_LERP_MODES = new HashMap<>();
    
    public static Interpolation addLerpMode(String name, Interpolation lerp) {
        LERP_MODES.put(name, lerp);
        return lerp;
    }
    
    public static Function<Float, Interpolation> addLerpMode(String name, Function<Float, Interpolation> lerp) {
        ARG_LERP_MODES.put(name, lerp);
        return lerp;
    }
    
    public static final Interpolation LINEAR = addLerpMode("linear", (dest, delta, keyframes, start, end, scale) -> {
        Vector3f startVec = keyframes[start].target();
        Vector3f endVec = keyframes[end].target();
        dest.set(startVec.x(), startVec.y(), startVec.z());
        dest.lerp(endVec, delta);
        dest.mul(scale);
        return dest;
    });
    
    public static final Interpolation CUBIC = addLerpMode("catmullrom", (dest, delta, keyframes, start, end, scale) -> {
        Vector3f p0Vec = keyframes[Math.max(0, start - 1)].target();
        Vector3f startVec = keyframes[start].target();
        Vector3f endVec = keyframes[end].target();
        Vector3f p3Vec = keyframes[Math.min(keyframes.length - 1, end + 1)].target();
        dest.set(
                MathUtil.catmullRom(delta, p0Vec.x(), startVec.x(), endVec.x(), p3Vec.x()) * scale, 
                MathUtil.catmullRom(delta, p0Vec.y(), startVec.y(), endVec.y(), p3Vec.y()) * scale, 
                MathUtil.catmullRom(delta, p0Vec.z(), startVec.z(), endVec.z(), p3Vec.z()) * scale);
        return dest;
    });
    
    public static final Interpolation IN_SINE = addLerpMode("easeInSine", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions::sin);
    });
    
    public static final Interpolation OUT_SINE = addLerpMode("easeOutSine", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.out(EasingFunctions::sin));
    });
    
    public static final Interpolation IN_OUT_SINE = addLerpMode("easeInOutSine", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.inOut(EasingFunctions::sin));
    });
    
    public static final Interpolation IN_QUAD = addLerpMode("easeInQuad", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions::quad);
    });
    
    public static final Interpolation OUT_QUAD = addLerpMode("easeOutQuad", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.out(EasingFunctions::quad));
    });
    
    public static final Interpolation IN_OUT_QUAD = addLerpMode("easeInOutQuad", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.inOut(EasingFunctions::quad));
    });
    
    public static final Interpolation IN_CUBIC = addLerpMode("easeInCubic", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions::cubic);
    });
    
    public static final Interpolation OUT_CUBIC = addLerpMode("easeOutCubic", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.out(EasingFunctions::cubic));
    });
    
    public static final Interpolation IN_OUT_CUBIC = addLerpMode("easeInOutCubic", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.inOut(EasingFunctions::cubic));
    });
    
    public static final Interpolation IN_QUART = addLerpMode("easeInQuart", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions::quart);
    });
    
    public static final Interpolation OUT_QUART = addLerpMode("easeOutQuart", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.out(EasingFunctions::quart));
    });
    
    public static final Interpolation IN_OUT_QUART = addLerpMode("easeInOutQuart", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.inOut(EasingFunctions::quart));
    });
    
    public static final Interpolation IN_QUINT = addLerpMode("easeInQuint", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions::quint);
    });
    
    public static final Interpolation OUT_QUINT = addLerpMode("easeOutQuint", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.out(EasingFunctions::quint));
    });
    
    public static final Interpolation IN_OUT_QUINT = addLerpMode("easeInOutQuint", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.inOut(EasingFunctions::quint));
    });
    
    public static final Interpolation IN_EXPO = addLerpMode("easeInExpo", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions::exp);
    });
    
    public static final Interpolation OUT_EXPO = addLerpMode("easeOutExpo", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.out(EasingFunctions::exp));
    });
    
    public static final Interpolation IN_OUT_EXPO = addLerpMode("easeInOutExpo", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.inOut(EasingFunctions::exp));
    });
    
    public static final Interpolation IN_CIRC = addLerpMode("easeInCirc", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions::circle);
    });
    
    public static final Interpolation OUT_CIRC = addLerpMode("easeOutCirc", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.out(EasingFunctions::circle));
    });
    
    public static final Interpolation IN_OUT_CIRC = addLerpMode("easeInOutCirc", (dest, delta, keyframes, start, end, scale) -> {
        return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.inOut(EasingFunctions::circle));
    });
    
    public static final Function<Float, Interpolation> STEP = addLerpMode("step", steps -> {
        return (dest, delta, keyframes, start, end, scale) -> {
            return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.step(steps));
        };
    });
    
    public static final Function<Float, Interpolation> IN_BACK = addLerpMode("easeInBack", overshoot -> {
        return (dest, delta, keyframes, start, end, scale) -> {
            return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.back(overshoot));
        };
    });
    
    public static final Function<Float, Interpolation> OUT_BACK = addLerpMode("easeOutBack", overshoot -> {
        return (dest, delta, keyframes, start, end, scale) -> {
            return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.out(EasingFunctions.back(overshoot)));
        };
    });
    
    public static final Function<Float, Interpolation> IN_OUT_BACK = addLerpMode("easeInOutBack", overshoot -> {
        return (dest, delta, keyframes, start, end, scale) -> {
            return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.inOut(EasingFunctions.back(overshoot)));
        };
    });
    
    public static final Function<Float, Interpolation> IN_ELASTIC = addLerpMode("easeInElastic", bounciness -> {
        return (dest, delta, keyframes, start, end, scale) -> {
            return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.elastic(bounciness));
        };
    });
    
    public static final Function<Float, Interpolation> OUT_ELASTIC = addLerpMode("easeOutElastic", bounciness -> {
        return (dest, delta, keyframes, start, end, scale) -> {
            return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.out(EasingFunctions.elastic(bounciness)));
        };
    });
    
    public static final Function<Float, Interpolation> IN_OUT_ELASTIC = addLerpMode("easeInOutElastic", bounciness -> {
        return (dest, delta, keyframes, start, end, scale) -> {
            return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.inOut(EasingFunctions.elastic(bounciness)));
        };
    });
    
    public static final Function<Float, Interpolation> IN_BOUNCE = addLerpMode("easeInBounce", bounciness -> {
        return (dest, delta, keyframes, start, end, scale) -> {
            return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.bounce(bounciness));
        };
    });
    
    public static final Function<Float, Interpolation> OUT_BOUNCE = addLerpMode("easeOutBounce", bounciness -> {
        return (dest, delta, keyframes, start, end, scale) -> {
            return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.out(EasingFunctions.bounce(bounciness)));
        };
    });
    
    public static final Function<Float, Interpolation> IN_OUT_BOUNCE = addLerpMode("easeInOutBounce", bounciness -> {
        return (dest, delta, keyframes, start, end, scale) -> {
            return makeLerp(dest, delta, keyframes, start, end, scale, EasingFunctions.inOut(EasingFunctions.bounce(bounciness)));
        };
    });
    
    
    
    public static Interpolation getLerpMode(String name, double[] args) {
        Interpolation noArgLerp = LERP_MODES.get(name);
        if (noArgLerp != null) {
            return noArgLerp;
        }
        
        Function<Float, Interpolation> singleArgLerp = ARG_LERP_MODES.get(name);
        if (singleArgLerp != null) {
            Float arg = args.length > 0 ? Float.valueOf((float) args[0]) : null;
            return singleArgLerp.apply(arg);
        }
        
        throw new IllegalArgumentException("Unknown interpolation type: " + name);
    }
    
    
    
    static Vector3f makeLerp(Vector3f dest, float delta, Keyframe[] keyframes, int start, int end, float scale, Float2FloatFunction lerpFunc) {
        Vector3f startVec = keyframes[start].target();
        Vector3f endVec = keyframes[end].target();
        lerp(startVec, endVec, delta, lerpFunc, dest).mul(scale);
        return dest;
    }
    
    static Vector3f lerp(Vector3f vec1, Vector3f vec2, float delta, Float2FloatFunction lerp, Vector3f dest) {
        dest.set(vec1.x(), vec1.y(), vec1.z());
        dest.lerp(vec2, lerp.get(delta));
        return dest;
    }
}
