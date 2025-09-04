package com.glance.plinko.platform.paper.animation;

import com.glance.plinko.platform.paper.display.Transformer;
import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Slf4j
public class ObjectAnimator {

    private final UUID animatorId = UUID.randomUUID();
    private final PlinkoObject object;
    private final PlinkoAnimation animation;
    private final Display display;
    private int currentTick = 0;

    public ObjectAnimator(
        @NotNull PlinkoAnimation animation,
        @NotNull PlinkoObject object,
        @NotNull Display display
    ) {
        this.animation = animation;
        this.object = object;
        this.display = display;
    }

    public boolean tick() {
        PlinkoKeyframe frame = animation.getFrame(currentTick++);
        if (frame == null) return true;

        Transformer.renderAt(display, frame.position(), 1, transform -> {
            transform.getLeftRotation().set(frame.rotation());
            transform.getScale().set(frame.scale3f());
        });

        return currentTick >= animation.totalTicks();
    }

}
