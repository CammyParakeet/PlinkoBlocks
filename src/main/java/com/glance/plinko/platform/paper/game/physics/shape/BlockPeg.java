package com.glance.plinko.platform.paper.game.physics.shape;

import com.glance.plinko.platform.paper.game.physics.PhysicsBox;

public record BlockPeg() implements PegShape {

    // TODO
    @Override
    public boolean intersects(PhysicsBox other) {
        return false;
    }

}
