package com.glance.plinko.platform.paper.physics.collision;

import org.joml.Vector3f;

public record CollisionResponse(
    Vector3f normalUsed,
    Vector3f leverArm,
    float impulseMagnitude,
    float tangentialDamp,
    Vector3f linearVelocityDelta,
    Vector3f angularVelocityDelta,
    Vector3f correction,
    boolean hadImpact
) {}
