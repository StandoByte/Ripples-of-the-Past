package com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier;

import com.github.standobyte.jojo.util.general.MathUtil;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Easing;
import dev.kosmx.playerAnim.core.util.Vec3f;

public abstract class KosmXFixedFadeModifier extends AbstractFadeModifier {

    protected KosmXFixedFadeModifier(int length) {
        super(length);
    }
    
    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
        if (type == TransformType.ROTATION && "head".equals(modelName)) {
            value0 = new Vec3f(value0.getX(), MathUtil.wrapRadians(value0.getY()), value0.getZ());
        }
        return super.get3DTransform(modelName, type, tickDelta, value0);
    }
    
    public static KosmXFixedFadeModifier standardFadeIn(int length, Ease ease) {
        return new KosmXFixedFadeModifier(length) {
            @Override
            protected float getAlpha(String modelName, TransformType type, float progress) {
                return Easing.easingFromEnum(ease, progress);
            }
        };
    }

    public static KosmXFixedFadeModifier functionalFadeIn(int length, EasingFunction function) {
        return new KosmXFixedFadeModifier(length) {
            @Override
            protected float getAlpha(String modelName, TransformType type, float progress) {
                return function.ease(modelName, type, progress);
            }
        };
    }

}
