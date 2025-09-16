package com.glance.plinko.platform.paper.physics.debug.inspect.visual;


import com.glance.plinko.platform.paper.display.Transformer;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class DebugSpinController {

    private static final class SpinState {
        final Display display;
        final Transformation original;
        final Vector3f deltaOmega = new Vector3f();
        long lastNanos;
        boolean active;

        SpinState(Display display, Transformation original, Vector3f omega) {
            this.display = display;
            this.original = original;
            this.deltaOmega.set(omega);
            this.lastNanos = System.nanoTime();
            this.active = true;
        }
    }

    private final Map<UUID, SpinState> spins = new ConcurrentHashMap<>();

    /**
     * Begin spinning this display using the given angular velocity (rad/s)
     * <p>
     * If already spinning, its angular velocity is updated and timing reset
     */
    public void start(@NotNull Display display, @NotNull Vector3f angularVelocity) {
        UUID id = display.getUniqueId();
        Transformation current = display.getTransformation();

        spins.compute(id, (k, existing) -> {
            if (existing == null) {
                return new SpinState(display, cloneTransformation(current), new Vector3f(angularVelocity));
            }

            existing.deltaOmega.set(angularVelocity);
            existing.lastNanos = System.nanoTime();
            existing.active = true;
            return existing;
        });
    }

    /** Toggle helper: if on==true -> start; else stop+restore */
    public void toggle(@NotNull Display display, @NotNull Vector3f angularVelocity, boolean on) {
        if (on) {
            start(display, angularVelocity);
        } else {
            stop(display, true);
        }
    }

    /**
     * Stop spinning. If restoreOriginal is true, the EXACT original transform is reapplied
     */
    public void stop(@NotNull Display display, boolean restoreOriginal) {
        UUID id = display.getUniqueId();
        SpinState state = spins.remove(id);
        if (state != null && restoreOriginal) {
            display.setTransformation(state.original);
        }
    }

    /** Stop all, optionally restoring originals */
    public void stopAll(boolean restoreOriginal) {
        for (SpinState s : spins.values()) {
            if (restoreOriginal) s.display.setTransformation(s.original);
        }
        spins.clear();
    }

    /**
     * Apply incremental rotations
     */
    public void update() {
        for (SpinState s : spins.values()) {
            if (!s.active) continue;

            Vector3f omega = s.deltaOmega;
            Transformer.modifyTransform(s.display, true, t -> {
                Quaternionf rot = t.getLeftRotation();
                log.warn("Should be rotating {} by {}", rot, omega);
                rot.rotateX(omega.x);
                rot.rotateY(omega.y);
                rot.rotateZ(omega.z);

                log.warn("Setting to {}", rot);
                t.getLeftRotation().set(rot);
            });
        }
    }

    private static Transformation cloneTransformation(Transformation t) {
        return new Transformation(
            new Vector3f(t.getTranslation()),
            new Quaternionf(t.getLeftRotation()),
            new Vector3f(t.getScale()),
            new Quaternionf(t.getRightRotation())
        );
    }

}
