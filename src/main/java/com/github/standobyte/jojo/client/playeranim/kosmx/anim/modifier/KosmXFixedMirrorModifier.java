package com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;

public class KosmXFixedMirrorModifier extends MirrorModifier {
    
    @Override
    protected Vec3f transformVector(Vec3f value0, TransformType type) {
        switch (type) {
        case BEND:
            // pretty sure only the y value is relevant, but why the hell is it being inverted in the mod's modifier?
            return new Vec3f(-value0.getX(), value0.getY(), value0.getZ());
        default:
            return super.transformVector(value0, type);
        }
    }

}
