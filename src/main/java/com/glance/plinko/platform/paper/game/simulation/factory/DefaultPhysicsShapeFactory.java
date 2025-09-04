package com.glance.plinko.platform.paper.game.simulation.factory;

import com.glance.plinko.platform.paper.game.simulation.PlinkoRunContext;
import com.glance.plinko.platform.paper.physics.shape.OrientedBox;
import com.glance.plinko.platform.paper.physics.shape.PhysicsShape;
import com.glance.plinko.platform.paper.physics.shape.ShapeType;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Vector3f;

@Singleton
public final class DefaultPhysicsShapeFactory implements PhysicsShapeFactory {

    @Override
    public PhysicsShape create(@NotNull PlinkoRunContext context) {
        ShapeType type = context.config().shape();
        Vector3f scale = context.config().displayOptions().scale().toVector3f();
        Vector3f halfSize = new Vector3f(scale).mul(0.5F);
        Vector3f center = new Vector3f(
            (float) context.origin().getX(),
            (float) context.origin().getY(),
            (float) context.origin().getZ()
        );

        return switch (type) {
            case OBB -> new OrientedBox(center, halfSize, new Matrix3f());
            case SPHERE -> throw new UnsupportedOperationException("SPHERE not yet implemented");
        };
    }

}
