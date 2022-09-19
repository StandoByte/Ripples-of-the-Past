package com.github.standobyte.jojo.client.model.pose.anim;

import com.github.standobyte.jojo.client.model.pose.ModelPose;
import com.github.standobyte.jojo.client.model.pose.RotationAngle;

import net.minecraft.entity.Entity;

public class RotationAnglesSnapshot<T extends Entity> extends ModelPose<T> {

    public RotationAnglesSnapshot(RotationAngle... rotations) {
        super(rotations);
    }

}
