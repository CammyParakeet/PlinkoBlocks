package com.glance.plinko.platform.paper.animation;

import java.util.List;
import java.util.UUID;

public record PlinkoAnimation(
        UUID id,
        List<PlinkoKeyframe> frames,
        int finalSlot
) {

    public PlinkoKeyframe getFrame(int tick) {
        return (tick >= 0 && tick < totalTicks()) ? frames.get(tick) : null;
    }

    public int totalTicks() {
        return frames.size();
    }

}
