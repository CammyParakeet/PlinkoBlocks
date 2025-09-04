package com.glance.plinko.platform.paper.command.debug;

import com.glance.plinko.platform.paper.command.engine.CommandHandler;
import com.glance.plinko.platform.paper.display.debug.LineDisplays;
import com.google.auto.service.AutoService;
import com.google.inject.Singleton;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
@AutoService(CommandHandler.class)
public class DebugArrowCommand implements CommandHandler {

    @Command("debug-arrow [length] [thickness] [mat]")
    public void debugArrow(
            @NotNull Player sender,
            @Argument @Nullable Double length,
            @Argument @Nullable Double thickness,
            @Argument @Nullable Material mat
    ) {
        float l = (length != null) ? length.floatValue() : 1.0F;
        float t = (thickness != null) ? thickness.floatValue() : 0.1F;
        Material m = (mat != null) ? mat : Material.WHITE_CONCRETE;

        Vector dir = sender.getEyeLocation().getDirection();

        Location loc = sender.getEyeLocation().clone();
        loc.setPitch(0F);
        loc.setYaw(0F);
        LineDisplays.spawnDebugArrow(loc, dir, l, t, m);
    }

}
