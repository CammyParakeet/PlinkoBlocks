package com.glance.plinko.platform.paper.display;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;

@UtilityClass
public class DisplayUpdate {

    public void applyTransform(Display display, Transformation transform) {
        applyTransform(display, transform, 1);
    }

    public void applyTransform(Display display, Transformation transform, int frameDuration) {
        display.setTransformation(transform);
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(frameDuration);
    }

}
