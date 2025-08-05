package com.glance.plinko.platform.paper.game.physics.shape;

import com.glance.plinko.platform.paper.game.physics.PhysicsBox;

public sealed interface PegShape permits BlockPeg {
    boolean intersects(PhysicsBox other);
}
