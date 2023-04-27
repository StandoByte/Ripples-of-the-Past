package com.github.standobyte.jojo.client.render.entity.pose.anim;

import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;

import net.minecraft.entity.Entity;

public class RotationAnglesSnapshot<T extends Entity> extends ModelPose<T> {

    public RotationAnglesSnapshot(RotationAngle... rotations) {
        super(rotations);
    }

}
