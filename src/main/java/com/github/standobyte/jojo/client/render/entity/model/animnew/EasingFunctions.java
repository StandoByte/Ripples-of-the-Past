package com.github.standobyte.jojo.client.render.entity.model.animnew;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.util.math.MathHelper;

public class EasingFunctions {
    // The MIT license notice below applies to the easing functions below except for
    // bounce and step
    /**
     * Copyright (c) Facebook, Inc. and its affiliates.
     *
     * This source code is licensed under the MIT license found in the LICENSE file
     * in the root directory of this source tree.
     */
    
    
    /**
     * Runs an easing function backwards.
     */
    static Float2FloatFunction out(Float2FloatFunction easing) {
        return t -> 1 - easing.apply(1 - t);
    }
    
    /**
     * Makes any easing function symmetrical. The easing function will run forwards
     * for half of the duration, then backwards for the rest of the duration.
     */
    static Float2FloatFunction inOut(Float2FloatFunction easing) {
        return t -> {
            if (t < 0.5) {
                return easing.apply(t * 2) / 2;
            }
            return 1 - easing.apply((1 - t) * 2) / 2;
        };
    }
    
    
    
    /**
     * A sinusoidal function.
     * <p>
     * http://easings.net/#easeInSine
     */
    static float sin(float t) {
        return 1 - MathHelper.cos((float) ((t * Math.PI) / 2));
    }
    
    /**
     * A quadratic function, `f(t) = t * t`. Position equals the square of elapsed
     * time.
     * <p>
     * http://easings.net/#easeInQuad
     */
    static float quad(float t) {
        return t * t;
    }
    
    /**
     * A cubic function, `f(t) = t * t * t`. Position equals the cube of elapsed
     * time.
     * <p>
     * http://easings.net/#easeInCubic
     */
    static float cubic(float t) {
        return t * t * t;
    }
    
    static float quart(float t) {
        return t * t * t * t;
    }
    
    static float quint(float t) {
        return t * t * t * t * t;
    }
    
    static float exp(float t) {
        return (float) Math.pow(2, 10 * (t - 1));
    }
    
    /**
     * A circular function.
     * <p>
     * http://easings.net/#easeInCirc
     */
    static float circle(float t) {
        return 1 - MathHelper.sqrt(1 - t * t);
    }
    
    static Float2FloatFunction step(Float stepArg) {
        int steps = stepArg != null ? stepArg.intValue() : 2;
        float[] intervals = stepRange(steps);
        return t -> intervals[findIntervalBorderIndex(t, intervals, false)];
    }
    
    /**
     * Creates a simple effect where the object animates back slightly as the animation starts.
     * <p>
     * http://easings.net/#easeInBack
     */
    static Float2FloatFunction back(Float overshoot) {
        float p = overshoot == null ? 1.70158f : overshoot * 1.70158f;
        return t -> t * t * ((p + 1) * t - p);
    }
    
    /**
     * A simple elastic interaction, similar to a spring oscillating back and forth.
     * <p>
     * Default bounciness is 1, which overshoots a little bit once. 0 bounciness
     * doesn't overshoot at all, and bounciness of N > 1 will overshoot about N
     * times.
     * <p>
     * http://easings.net/#easeInElastic
     */
    static Float2FloatFunction elastic(Float bounciness) {
        float p = (bounciness == null ? 1 : bounciness) * (float) Math.PI;
        return t -> 1 - (float) Math.pow(MathHelper.cos((t * (float) Math.PI) / 2), 3) * MathHelper.cos(t * p);
    }
    

    /**
     * Provides a simple bouncing effect.
     * <p>
     * http://easings.net/#easeInBounce
     */
    static Float2FloatFunction bounce(Float bounciness) {
        float k = bounciness == null ? 0.5f : bounciness;
        Float2FloatFunction q = x -> (121.0f / 16.0f) * x * x;
        Float2FloatFunction w = x -> ((121.0f / 4.0f) * k) * (float) Math.pow(x - (6.0 / 11.0), 2) + 1 - k;
        Float2FloatFunction r = x -> 121 * k * k * (float) Math.pow(x - (9.0 / 11.0), 2) + 1 - k * k;
        Float2FloatFunction t = x -> 484 * k * k * k * (float) Math.pow(x - (10.5 / 11.0), 2) + 1 - k * k * k;
        return x -> min(q.apply(x), w.apply(x), r.apply(x), t.apply(x));
    }

    private static float min(float a, float b, float c, float d) {
        return Math.min(Math.min(a, b), Math.min(c, d));
    }

    // The MIT license notice below applies to the function findIntervalBorderIndex
    /*
     * The MIT License (MIT)
     * 
     * Copyright (c) 2015 Boris Chumichev
     * 
     * Permission is hereby granted, free of charge, to any person obtaining a copy
     * of this software and associated documentation files (the "Software"), to deal
     * in the Software without restriction, including without limitation the rights
     * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     * copies of the Software, and to permit persons to whom the Software is
     * furnished to do so, subject to the following conditions:
     * 
     * The above copyright notice and this permission notice shall be included in
     * all copies or substantial portions of the Software.
     * 
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
     * SOFTWARE.
     * 
     * /**
     *
     * Utilizes bisection method to search an interval to which point belongs to,
     * then returns an index of left or right border of the interval
     *
     * @param {Number} point
     * 
     * @param {Array} intervals
     * 
     * @param {Boolean} useRightBorder
     * 
     * @returns {Number}
     */
    private static int findIntervalBorderIndex(float point, float[] intervals, boolean useRightBorder) {
        // If point is beyond given intervals
        if (point < intervals[0])
            return 0;
        if (point > intervals[intervals.length - 1])
            return intervals.length - 1;
        // If point is inside interval
        // Start searching on a full range of intervals
        int indexOfNumberToCompare = 0;
        int leftBorderIndex = 0;
        int rightBorderIndex = intervals.length - 1;
        // Reduce searching range till it find an interval point belongs to using binary
        // search
        while (rightBorderIndex - leftBorderIndex != 1) {
            indexOfNumberToCompare = leftBorderIndex + (rightBorderIndex - leftBorderIndex) / 2;
            if (point >= intervals[indexOfNumberToCompare]) {
                leftBorderIndex = indexOfNumberToCompare;
            } else {
                rightBorderIndex = indexOfNumberToCompare;
            }
        }
        return useRightBorder ? rightBorderIndex : leftBorderIndex;
    }

    private static float[] stepRange(int steps) {
        final float stop = 1;
        if (steps < 2)
            throw new IllegalArgumentException("steps must be > 2, got:" + steps);
        float stepLength = stop / (float) steps;
        float[] stepArray = new float[steps];

        for (int i = 0; i < steps; i++) {
            stepArray[i] = i * stepLength;
        }

        return stepArray;
    };
}
