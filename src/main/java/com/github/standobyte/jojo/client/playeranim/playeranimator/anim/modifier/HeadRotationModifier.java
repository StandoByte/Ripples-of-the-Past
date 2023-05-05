package com.github.standobyte.jojo.client.playeranim.playeranimator.anim.modifier;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;

public class HeadRotationModifier extends AbstractModifier {

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
        Vec3f transform = super.get3DTransform(modelName, type, tickDelta, value0);
        if (isActive() && "head".equals(modelName)) {
            return transform.add(value0);
        }
        return transform;
    }

}
