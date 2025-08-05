package com.glance.plinko.platform.paper.game.simulation;

import com.glance.plinko.platform.paper.game.physics.shape.OrientedBox;
import com.glance.plinko.platform.paper.game.physics.shape.PhysicsShape;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Getter
@Setter
public class PlinkoObject {

    private Vector3f position;
    private Vector3f velocity;
    private Quaternionf rotation;
    private Vector3f angularVelocity;
    private Vector3f scale;

    private final float mass;
    private final float angularSpinFactor;

    private final PhysicsShape baseShape;

    public PlinkoObject(
        @NotNull final PhysicsShape baseShape,
        float mass,
        float angularSpinFactor
    ) {
        this.baseShape = baseShape;
        this.mass = mass;
        this.angularSpinFactor = angularSpinFactor;

        this.position = new Vector3f(baseShape.center());
        this.velocity = new Vector3f();
        this.rotation = new Quaternionf();
        this.angularVelocity = new Vector3f();
        this.scale = new Vector3f(1.0F, 1.0F, 1.0F);
    }

    public PhysicsShape currentShape() {
        if (baseShape instanceof OrientedBox box) {
            Matrix3f rotationMatrix = new Matrix3f().rotate(rotation);
            return new OrientedBox(position, box.halfSize(), rotationMatrix);
        }

        throw new UnsupportedOperationException("Unsupported shape: " + baseShape.getClass().getSimpleName());
    }

}
