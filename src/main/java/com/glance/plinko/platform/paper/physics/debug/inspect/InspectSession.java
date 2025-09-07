package com.glance.plinko.platform.paper.physics.debug.inspect;

import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import com.glance.plinko.platform.paper.physics.collision.CollisionResult;
import com.glance.plinko.platform.paper.physics.debug.inspect.visual.InspectVisualHandler;
import com.glance.plinko.platform.paper.utils.math.VelocityState;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class InspectSession {

    private final PlinkoObject[] objects = new PlinkoObject[2];
    private final Display[] displays = new Display[2];

    private final VelocityState[] linear = new VelocityState[] { new VelocityState(), new VelocityState() };
    private final VelocityState[] angular = new VelocityState[] { new VelocityState(), new VelocityState() };

    @Getter
    private final InspectVisualHandler visualHandler = new InspectVisualHandler();

    @Getter
    @Setter
    private CollisionResult lastResult;

    public void set(int index, PlinkoObject object, Display display) {
        this.objects[index] = object;
        this.displays[index] = display;

        if (object != null) {
            // Sync linear state
            Vector3f v = object.getVelocity();
            if (v != null) this.linear[index].setFromVector(v);

            // Sync angular state
            Vector3f w = object.getAngularVelocity();
            if (w != null) this.angular[index].setFromVector(w);
        }
    }

    public PlinkoObject getObject(int index) {
        return objects[index];
    }

    public Display getDisplay(int index) {
        return displays[index];
    }

    public VelocityState linearState(int index) { return linear[index]; }
    public VelocityState angularState(int index) { return angular[index]; }

    /**
     * Pushes the current linear/angular state into the object (keeps them in sync)
     */
    public void applyLinearToObject(int index) {
        PlinkoObject obj = objects[index];
        if (obj == null) return;
        obj.setVelocity(linear[index].asVector(new Vector3f()));
    }

    public void applyAngularToObject(int index) {
        PlinkoObject obj = objects[index];
        if (obj == null) return;
        obj.setAngularVelocity(angular[index].asVector(new Vector3f()));
    }

    public void updateVisuals(@NotNull Player player) {
        this.visualHandler.update(player, this);
    }

    public void clearVisuals() {
        for (Display d : displays) {
            d.remove();
        }
        visualHandler.clearAll();

    }

}
