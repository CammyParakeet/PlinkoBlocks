package com.glance.plinko.platform.paper.display;

import com.glance.plinko.platform.paper.game.simulation.PlinkoRunContext;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;

public interface PlinkoDisplayFactory {

    @NotNull
    Display createDisplay(@NotNull PlinkoRunContext context);

    @NotNull Display createPegDisplay(
        @NotNull Location location,
        @NotNull DisplayOptions options
    );

}
