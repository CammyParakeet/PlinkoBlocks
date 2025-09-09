package com.glance.plinko.platform.paper.display;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Slf4j
@UtilityClass
public class DisplayUtils {

    public @NotNull Display spawnDisplay(
        @NotNull Location loc,
        @NotNull DisplayOptions options,
        @NotNull Consumer<Display> onSpawn
    ) {
        World world = loc.getWorld();
        Display entity;

        entity = switch (options.type()) {
            case BLOCK -> world.spawn(loc, BlockDisplay.class, bd -> {
                bd.setBlock(options.material().createBlockData());
                toAll(bd, options);
                onSpawn.accept(bd);
            });
            case ITEM -> world.spawn(loc, ItemDisplay.class, i -> {
                i.setItemStack(new ItemStack(options.material()));
                toAll(i, options);
                onSpawn.accept(i);
            });
            case TEXT -> world.spawn(loc, TextDisplay.class, t -> {
                toAll(t, options);
                onSpawn.accept(t);
            });
        };

        return entity;
    }

    public @NotNull Display spawnDisplay(
        @NotNull Location loc,
        @NotNull DisplayOptions options
    ) {
        return spawnDisplay(loc, options, d -> {});
    }

    private void toAll(@NotNull Display entity, @NotNull DisplayOptions options) {
        Transformer.modifyTransform(entity, false, t -> {
            t.getScale().set(options.scale().toVector3d());
            t.getLeftRotation().set(options.rotation());
            // other transform initialization if needed
        });

        //entity.setInterpolationDelay(0);
        //entity.setInterpolationDuration(1);
        entity.setPersistent(false);
    }

}
