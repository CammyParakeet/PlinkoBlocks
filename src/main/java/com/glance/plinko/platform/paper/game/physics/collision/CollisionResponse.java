package com.glance.plinko.platform.paper.game.physics.collision;

import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@UtilityClass
public class CollisionResponse {

    // TODO: based on the object and/or collision object/peg?
    private final float ROTATION_BOOST = 0.1f;

    public void apply(
        @NotNull PlinkoObject object,
        @NotNull CollisionResult result
    ) {
        Vector3f normal = result.normal();
        Vector3f velocity = object.getVelocity();

        float dot = velocity.dot(normal);
        Vector3f reflected = new Vector3f(velocity).sub(new Vector3f(normal).mul(2 * dot));
        object.setVelocity(reflected);

        Vector3f contactOffset = new Vector3f(result.contactPoint()).sub(object.getPosition());
        Vector3f impulse = new Vector3f(normal).mul(velocity.length());

        Vector3f torque = contactOffset.cross(impulse).mul(object.getAngularSpinFactor() * ROTATION_BOOST);
        object.getAngularVelocity().add(torque);

        object.getPosition().add(new Vector3f(normal)).mul(result.penetrationDepth());
    }

}
