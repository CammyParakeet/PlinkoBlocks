package com.glance.plinko.platform.paper.animation;

import com.glance.plinko.utils.lifecycle.Manager;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@AutoService(Manager.class)
public final class PlinkoRenderer implements Manager {

    private final Plugin plugin;
    private final Set<ObjectAnimator> activeAnimations = ConcurrentHashMap.newKeySet();
    private BukkitTask renderTask;

    @Inject
    public PlinkoRenderer(@NotNull final Plugin plugin) {
        this.plugin = plugin;
    }

    public void add(@NotNull ObjectAnimator animator) {
        this.activeAnimations.add(animator);
    }

    public void start() {
        if (renderTask != null) return;

        renderTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            Iterator<ObjectAnimator> iterator = activeAnimations.iterator();

            while (iterator.hasNext()) {
                ObjectAnimator animator = iterator.next();
                boolean finished = animator.tick();

                if (finished) {
                    iterator.remove();
                }
            }

        }, 1L, 1L);
    }

    @Override
    public void onEnable() {
        start();
    }

    public Set<ObjectAnimator> activeAnimations() {
        return Set.copyOf(activeAnimations);
    }

    @Override
    public void onDisable() {
        if (this.renderTask != null) {
            this.renderTask.cancel();
            this.renderTask = null;
        }
    }

}
