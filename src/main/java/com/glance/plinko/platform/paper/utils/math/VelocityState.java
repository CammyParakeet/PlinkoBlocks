package com.glance.plinko.platform.paper.utils.math;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@Getter
public final class VelocityState {
    private final Vector3f direction = new Vector3f(0, 1, 0);
    private float magnitude = 0f;

    /** dir is normalized if non-zero; magnitude preserved */
    public void setDirection(@NotNull Vector3f dir) {
        if (dir.lengthSquared() > VectorUtils.EPS) {
            direction.set(dir).normalize();
        }
    }

    /** magnitude >= 0; direction preserved */
    public void scaleMagnitude(float factor) {
        this.magnitude = Math.max(0f, this.magnitude * factor);
    }

    /** magnitude >= 0; direction preserved. */
    public void setMagnitude(float mag) {
        this.magnitude = Math.max(0f, mag);
    }

    /** Replace state from a raw vector v (splits into dir + mag) */
    public void setFromVector(@NotNull Vector3f v) {
        float len = (float) Math.sqrt(v.lengthSquared());
        if (len <= 0f) {
            this.magnitude = 0f;
            // direction unchanged
        } else {
            this.direction.set(v).div(len); // normalize
            this.magnitude = len;
        }
    }

    /** Returns dir * mag into dest */
    public Vector3f asVector(@NotNull Vector3f dest) {
        return dest.set(direction).mul(magnitude);
    }

}
