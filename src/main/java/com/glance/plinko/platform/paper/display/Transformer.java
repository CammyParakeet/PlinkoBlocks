package com.glance.plinko.platform.paper.display;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Consumer;

@UtilityClass
public class Transformer {

    public void applyTransform(Display display, Transformation transform, boolean interpolated) {
        applyTransform(display, transform, interpolated ? 1 : -1);
    }

    public void applyTransform(Display display, Transformation transform, int frameDuration) {
        display.setTransformation(transform);
        if (frameDuration > 0) {
            display.setInterpolationDelay(0);
            display.setInterpolationDuration(frameDuration);
        }
    }

    public void modifyTransform(
            @NotNull Display display,
            boolean interpolated,
            @NotNull Consumer<Transformation> modifier
    ) {
        modifyTransform(display, interpolated ? 1 : -1, modifier);
    }

    public void modifyTransform(
        @NotNull Display display,
        int frameDuration,
        @NotNull Consumer<Transformation> modifier
    ) {
        Transformation current = display.getTransformation();
        modifier.accept(current);
        applyTransform(display, current, frameDuration);
    }

    public void renderAt(
        @NotNull Display display,
        @NotNull Vector renderPosition,
        int duration,
        @Nullable Vector originOverride,
        @NotNull Consumer<Transformation> extraAction
    ) {
        Vector anchor = originOverride != null ? originOverride : display.getLocation().toVector();
        Vector delta = renderPosition.clone().subtract(anchor);
        Vector3f deltaVec = delta.toVector3f();

        Transformation transform = display.getTransformation();
        transform.getTranslation().set(deltaVec);

        extraAction.accept(transform);

        applyTransform(display, transform, duration);
    }

    public void renderAt(
            @NotNull Display display,
            @NotNull Vector renderPosition,
            int duration,
            @NotNull Consumer<Transformation> extraAction
    ) {
        renderAt(display, renderPosition, duration, null, extraAction);
    }

    public void renderAt(
            @NotNull Display display,
            @NotNull Vector renderPosition,
            @NotNull Consumer<Transformation> extraAction
    ) {
        renderAt(display, renderPosition, 1, null, extraAction);
    }

    public void renderAt(
            @NotNull Display display,
            @NotNull Vector renderPosition
    ) {
        renderAt(display, renderPosition, 1, null, t -> {});
    }

}
