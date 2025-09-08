package com.glance.plinko.platform.paper.physics.collision.manifold;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Manifold {
    final List<Vector3f> points = new ArrayList<>(8);
    final Vector3f centroid = new Vector3f();
}
