package com.github.standobyte.jojo.client.playeranim.playeranimator.anim.modifier;

import java.util.EnumSet;
import java.util.Set;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.util.general.MathUtil;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;

public class ArmsRotationModifier extends AbstractModifier {
    private final AbstractClientPlayerEntity entity;
    private final Set<HandSide> arms = EnumSet.noneOf(HandSide.class);
    
    public ArmsRotationModifier(AbstractClientPlayerEntity entity, HandSide side, HandSide... otherSide) {
        this.entity = entity;
        arms.add(side);
        for (HandSide s : otherSide) {
            arms.add(s);
        }
    }

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
        Vec3f transform = super.get3DTransform(modelName, type, tickDelta, value0);
        if (isActive() && type == TransformType.ROTATION && (
                arms.contains(HandSide.LEFT) && "leftArm".equals(modelName)
                || arms.contains(HandSide.RIGHT) && "rightArm".equals(modelName))) {
            float entityXRot = entity.xRot;
            Vector3f anglesNew = ClientUtil.rotateAngles(transform.getX(), transform.getY(), transform.getZ(), entityXRot * MathUtil.DEG_TO_RAD);
            transform = new Vec3f(anglesNew.x(), anglesNew.y(), anglesNew.z());
        }
        return transform;
    }
}
