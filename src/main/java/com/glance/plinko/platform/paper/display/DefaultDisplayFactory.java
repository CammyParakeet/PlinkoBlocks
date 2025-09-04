package com.glance.plinko.platform.paper.display;

import com.glance.plinko.platform.paper.game.simulation.PlinkoRunContext;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;

@Slf4j
@Singleton
public final class DefaultDisplayFactory implements PlinkoDisplayFactory {

    @Override
    public @NotNull Display createDisplay(@NotNull PlinkoRunContext context) {
        return DisplayUtils.spawnDisplay(context.origin(), context.config().displayOptions());
    }

    @Override
    public @NotNull Display createPegDisplay(@NotNull Location location, @NotNull DisplayOptions options) {
        return DisplayUtils.spawnDisplay(location, options);
    }

}
