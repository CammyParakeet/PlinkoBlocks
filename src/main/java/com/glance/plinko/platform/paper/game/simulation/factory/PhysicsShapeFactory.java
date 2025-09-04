package com.glance.plinko.platform.paper.game.simulation.factory;

import com.glance.plinko.platform.paper.game.simulation.PlinkoRunContext;
import com.glance.plinko.platform.paper.physics.shape.PhysicsShape;
import org.jetbrains.annotations.NotNull;

public interface PhysicsShapeFactory {
    PhysicsShape create(@NotNull PlinkoRunContext context);
}
