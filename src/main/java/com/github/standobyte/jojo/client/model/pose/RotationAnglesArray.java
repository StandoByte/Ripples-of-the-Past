package com.github.standobyte.jojo.client.model.pose;

import java.util.Arrays;
import java.util.Iterator;

public class RotationAnglesArray implements Iterable<RotationAngle> {
    private final Iterable<RotationAngle> wrapped;

    public RotationAnglesArray(RotationAngle... rotations) {
        wrapped = Arrays.asList(rotations);
    }

    @Override
    public Iterator<RotationAngle> iterator() {
        return wrapped.iterator();
    }
}
