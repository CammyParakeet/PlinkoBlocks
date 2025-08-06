package com.glance.plinko.platform.paper.config;

import com.glance.plinko.platform.paper.display.DisplayOptions;
import com.glance.plinko.platform.paper.game.physics.shape.ShapeType;
import org.bukkit.Material;

public record PlinkoObjectConfig(
    ShapeType shape,
    DisplayOptions displayOptions,
    float mass,
    float angularSpinFactor,

    float bounciness,
    float stickiness
) {
    public static PlinkoObjectConfig defaults(Material block) {
        return new PlinkoObjectConfig(
            ShapeType.OBB,
            DisplayOptions.defaultBlock(block),
            1.0F,
            0.3F,
            0.5F,
            0.0F
        );
    }
}
