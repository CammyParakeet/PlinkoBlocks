package com.glance.plinko.platform.paper.config;

import com.glance.plinko.platform.paper.display.DisplayOptions;
import com.glance.plinko.platform.paper.physics.shape.ShapeType;
import lombok.Builder;
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
            DisplayOptions.defaultItem(block),
            1.0F,
            0.3F,
            0.5F,
            0.0F
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ShapeType shape = ShapeType.OBB;
        private DisplayOptions displayOptions = DisplayOptions.defaultItem(Material.STONE);
        private float mass = 1.0F;
        private float angularSpinFactor = 0.3F;
        private float bounciness = 0.5F;
        private float stickiness = 0.0F;

        public Builder shape(ShapeType shape) {
            this.shape = shape;
            return this;
        }

        public Builder display(DisplayOptions options) {
            this.displayOptions = options;
            return this;
        }

        public Builder mass(float mass) {
            this.mass = mass;
            return this;
        }

        public Builder angularSpinFactor(float spin) {
            this.angularSpinFactor = spin;
            return this;
        }

        public Builder bounciness(float bounciness) {
            this.bounciness = bounciness;
            return this;
        }

        public Builder stickiness(float stickiness) {
            this.stickiness = stickiness;
            return this;
        }

        public PlinkoObjectConfig build() {
            return new PlinkoObjectConfig(shape, displayOptions, mass, angularSpinFactor, bounciness, stickiness);
        }
    }
}
