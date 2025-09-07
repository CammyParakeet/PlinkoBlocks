package com.glance.plinko.platform.paper.utils.world;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class LocUtils {

    @NotNull public String asCoords(@NotNull Location loc) {
        return String.format("(%.2f, %.2f, %.2f)", loc.getX(), loc.getY(), loc.getZ());
    }

}

