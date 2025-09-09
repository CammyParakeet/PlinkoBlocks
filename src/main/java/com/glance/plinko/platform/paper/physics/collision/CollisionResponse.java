package com.glance.plinko.platform.paper.physics.collision;

import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@Slf4j
@UtilityClass
public class CollisionResponse {

    // TODO: based on the object and/or collision object/peg?
    private final float ROTATION_BOOST = 0.1f;
    private final float PENETRATION_SLOP = 0.001f;
    private final float PENETRATION_CORRECT_PCT = 0.80f;

    private final float FRICTION_COEFF = 0.0f;

    public void apply(
        @NotNull PlinkoObject object,
        @NotNull CollisionResult result,
        boolean debug
    ) {
        if (object.isImmovable()) return;

        Vector3f normal = result.normal();
        Vector3f velocity = object.getVelocity();
        float dot = velocity.dot(normal);
        float bounciness = object.getBounciness();

        // New direction the object should 'bounce off' in
        Vector3f reflected = new Vector3f(velocity)
                .sub(new Vector3f(normal).mul(2 * dot))
                .mul(bounciness);

        // Find collision area relative to object center
        Vector3f contactOffset = new Vector3f(result.centroid()).sub(object.getPosition());

        // Estimate strength of the hit based on speed and direction
        Vector3f impulse = new Vector3f(normal).mul(velocity.length());

        // Calculate the new spin to apply based on hit location
        Vector3f torque = contactOffset.cross(impulse).mul(object.getAngularSpinFactor() * ROTATION_BOOST);

        // Push the object out of the shape to prevent weird clipping
        Vector3f correction = new Vector3f(normal).mul(result.penetrationDepth());

        if (debug) {
            log.info("[Collision Debug] Object is at {}", object.getPosition());
            log.info("[Collision Debug] velocity: {}", velocity);
            log.info("[Collision Debug] normal: {}", normal);
            log.info("[Collision Debug] reflected velocity: {}", reflected);
            log.info("[Collision Debug] contactOffset: {}", contactOffset);
            log.info("[Collision Debug] impulse: {}", impulse);
            log.info("[Collision Debug] torque: {}", torque);
            log.info("[Collision Debug] correction: {}", correction);
        } else {
            object.setVelocity(reflected);
            object.getAngularVelocity().add(torque);
            object.getPosition().add(correction);
        }
    }

}
