package com.glance.plinko.platform.paper.game.simulation;

import com.glance.plinko.platform.paper.game.simulation.factory.PhysicsShapeFactory;
import com.glance.plinko.platform.paper.physics.shape.OrientedBox;
import com.glance.plinko.platform.paper.physics.shape.PhysicsShape;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Slf4j
@Getter
@Setter
public class PlinkoObject {

    private static float IMMOVABLE_MASS_THRESHOLD = 1000.0F;

    private Vector3f position;
    private Vector3f velocity;
    private Quaternionf rotation;
    private Vector3f angularVelocity;
    private Vector3f scale;

    private float bounciness;
    private final float mass;
    private final float angularSpinFactor;

    private final boolean immovable;

    private final PhysicsShape baseShape;

    @Inject
    public PlinkoObject(
        @Assisted PlinkoRunContext context,
        @NotNull final PhysicsShapeFactory shapeFactory
    ) {
        this(
            shapeFactory.create(context),
            context.config().mass(),
            context.config().angularSpinFactor()
        );
    }

    public PlinkoObject(
        @NotNull final PhysicsShape baseShape,
        float mass,
        float angularSpinFactor
    ) {
        this.baseShape = baseShape;
        this.mass = mass;
        this.immovable = mass >= IMMOVABLE_MASS_THRESHOLD;

        this.angularSpinFactor = angularSpinFactor;

        log.warn("Spawning plinko object with baseShape center at {}", baseShape.center());
        this.position = new Vector3f(baseShape.center());
        this.velocity = new Vector3f();
        this.rotation = new Quaternionf();
        this.bounciness = 0.6F;
        this.angularVelocity = new Vector3f();
        this.scale = new Vector3f(1.0F, 1.0F, 1.0F);
    }

    public PhysicsShape currentShape() {
        if (baseShape instanceof OrientedBox box) {
            Matrix3f rotationMatrix = new Matrix3f().rotate(rotation);
            return new OrientedBox(position, box.halfSize(), rotationMatrix, scale);
        }

        throw new UnsupportedOperationException("Unsupported shape: " + baseShape.getClass().getSimpleName());
    }

    public Vector3f getVelocityAt(@NotNull Vector3f worldPoint) {
        Vector3f r = new Vector3f(worldPoint).sub(position);
        Vector3f spinVelocity = new Vector3f(angularVelocity).cross(r);
        return new Vector3f(velocity).add(spinVelocity);
    }

}
