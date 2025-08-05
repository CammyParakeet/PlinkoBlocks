package com.glance.plinko.platform.paper.game.animation;

import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;

public class ObjectAnimator {

    private final PlinkoObject object;
    private final PlinkoAnimation animation;
    private int currentTick = 0;

    public ObjectAnimator(
        PlinkoAnimation animation,
        PlinkoObject object
    ) {
        this.animation = animation;
        this.object = object;
    }

    public boolean tick() {
        PlinkoKeyframe frame = animation.getFrame(currentTick++);
        if (frame == null) return true;

        // todo render update

        return currentTick >= animation.totalTicks();
    }

}
