package com.glance.plinko.platform.paper.game.simulation;

import com.glance.plinko.platform.paper.animation.PlinkoAnimation;
import com.glance.plinko.platform.paper.animation.PlinkoKeyframe;
import com.glance.plinko.platform.paper.physics.collision.CollisionResponse;
import com.glance.plinko.platform.paper.physics.collision.CollisionResult;
import com.glance.plinko.platform.paper.physics.shape.PhysicsShape;
import com.glance.plinko.utils.lifecycle.Manager;
import com.google.auto.service.AutoService;
import com.google.inject.Singleton;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
@AutoService(Manager.class)
public class PlinkoSimulator implements Manager {

    private static final Vector3f GRAVITY = new Vector3f(0F, -0.03F, 0F); // todo configurable
    private static final int MAX_TICKS = 200; // todo configurable
    private static final float TERMINAL_VELOCITY = 0.5F; // todo depends on object - so this should be a factor

    // todo provide full board if necessary?
    public PlinkoAnimation simulate(PlinkoObject object, List<PhysicsShape> obstacles) {
        List<PlinkoKeyframe> frames = new ArrayList<>();
        boolean collisionOccurred;

        for (int tick = 0; tick < MAX_TICKS; tick++){
            collisionOccurred = false;

            // Apply gravity
            Vector3f vel = object.getVelocity();
            vel.add(GRAVITY);

            // Clamp
            if (vel.length() > TERMINAL_VELOCITY) {
                vel.normalize().mul(TERMINAL_VELOCITY);
            }

            // Move the object
            object.getPosition().add(vel);

            // Update rotation (if angular velocity used)
            Quaternionf spin = new Quaternionf().rotateXYZ(
                    object.getAngularVelocity().x,
                    object.getAngularVelocity().y,
                    object.getAngularVelocity().z
            );
            object.getRotation().mul(spin);

            // Build shape
            PhysicsShape shape = object.currentShape();

            // Collision check
            for (PhysicsShape obstacle : obstacles) {
                // todo return early on distance check

                CollisionResult result = shape.collide(obstacle);
                if (result != null) {
                    CollisionResponse.apply(object, result);
                    collisionOccurred = true;
                    break;
                }
            }

            // Build frame transformation
            Transformation transform = new Transformation(
                    object.getPosition(),
                    object.getRotation(),
                    object.getScale(),
                    new Quaternionf()
            );

            frames.add(new PlinkoKeyframe(transform, collisionOccurred, tick));
        }

        int finalSlot = -1; // TODO: slot detection at the bottom
        return new PlinkoAnimation(UUID.randomUUID(), frames, finalSlot);
    }

}
