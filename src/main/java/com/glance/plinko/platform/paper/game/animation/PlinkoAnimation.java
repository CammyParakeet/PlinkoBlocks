package com.glance.plinko.platform.paper.game.animation;

import java.util.List;
import java.util.UUID;

public record PlinkoAnimation(
        UUID id,
        List<SimulatedBallFrame> frames,
        int finalSlot
) {

    public SimulatedBallFrame getFrame(int tick) {
        return (tick >= 0 && tick < totalTicks()) ? frames.get(tick) : null;
    }

    public int totalTicks() {
        return frames.size();
    }

}
