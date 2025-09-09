package com.glance.plinko.platform.paper.physics.collision;

import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import com.glance.plinko.platform.paper.physics.shape.OrientedBox;
import com.glance.plinko.platform.paper.physics.shape.PhysicsShape;
import com.glance.plinko.platform.paper.utils.math.VectorUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Slf4j
@UtilityClass
public class CollisionResponder {

    private final float EPS = 1e-6f;

    private final float PENETRATION_SLOP = 0.001f;
    private final float PENETRATION_CORRECT_PCT = 0.80f;

    private final float FRICTION_COEFF = 0.0f;
    private final float DEFAULT_TANGENTIAL_DAMP = 0.25f;

    public CollisionResponse planResponseLite(
        @NotNull PlinkoObject primary,
        @NotNull CollisionResult result
    ) {
        return planResponseLite(primary, result, primary.getBounciness(), DEFAULT_TANGENTIAL_DAMP);
    }

    public CollisionResponse planResponseLite(
        @NotNull PlinkoObject primary,
        @NotNull CollisionResult result,
        float restitution,
        float tangentialDamp
    ) {
        Vector3f normal = new Vector3f(result.normal()).negate();
        Vector3f x = new Vector3f(primary.getPosition());           // position
        Vector3f v = new Vector3f(primary.getVelocity());           // linear vel
        Vector3f w = new Vector3f(primary.getAngularVelocity());    // angular vel
        Vector3f c = new Vector3f(result.centroid());               // contact point
        Vector3f r = new Vector3f(c).sub(x);                        // lever arm

        Vector3f contactVelocity = new Vector3f(v).add(new Vector3f(w).cross(r, new Vector3f()));
        float normalSpeed = contactVelocity.dot(normal);

        Vector3f dv = new Vector3f();
        Vector3f dw = new Vector3f();
        float normalImpulseMag = 0f, tangentImpulseMag = 0f;

        float invMass = primary.getInvMass();
        float invInertia = primary.getInvInertia();

        if (normalSpeed < 0f && invMass > 0f) {
            Vector3f rXn = new Vector3f(r).cross(normal, new Vector3f());
            float effectiveMassNormal = invMass + invInertia * rXn.dot(rXn);
            if (effectiveMassNormal < EPS) effectiveMassNormal = EPS;

            normalImpulseMag = -(1f + Math.max(0f, restitution)) * normalSpeed / effectiveMassNormal;
            Vector3f normalImpulse = new Vector3f(normal).mul(normalImpulseMag);

            dv.fma(invMass, normalImpulse);
            dw.fma(invInertia, new Vector3f(r).cross(normalImpulse, new Vector3f()));

            if (tangentialDamp > 0f) {
                Vector3f tangentVel = new Vector3f(contactVelocity).fma(-normalSpeed, normal);
                float tangentSpeed = tangentVel.length();
                if (tangentSpeed > EPS) {
                    Vector3f tangentDir = tangentVel.mul(1f / tangentSpeed);
                    Vector3f rXt = new Vector3f(r).cross(tangentDir, new Vector3f());
                    float effectiveMassTangent = invMass + invInertia * rXt.dot(rXt);
                    if (effectiveMassTangent < EPS) effectiveMassTangent = EPS;

                    tangentImpulseMag = -(tangentialDamp * tangentSpeed) / effectiveMassTangent;
                    Vector3f tangentImpulse = new Vector3f(tangentDir).mul(tangentImpulseMag);

                    dv.fma(invMass, tangentImpulse);
                    dw.fma(invInertia, new Vector3f(r).cross(tangentImpulse, new Vector3f()));
                }
            }
        }

        float pen = Math.max(result.penetrationDepth() - PENETRATION_SLOP, 0f);
        Vector3f correction = new Vector3f(normal).mul(pen * PENETRATION_CORRECT_PCT);

        return new CollisionResponse(normal, r, normalImpulseMag, tangentImpulseMag, dv, dw, correction, normalSpeed < 0f);
    }

    public void apply(
        @NotNull PlinkoObject object,
        @NotNull CollisionResult result,
        boolean debug
    ) {
        if (object.isImmovable()) return;


    }

    private Vector3f orientedNormalForPrimary(@NotNull PlinkoObject primary, @NotNull CollisionResult res) {
        Vector3f n = new Vector3f(res.normal()); // SAT normal as produced
        Vector3f pCtr = new Vector3f(primary.getPosition());
        Vector3f oCtr = tryGetCenter(res.other());
        if (oCtr != null) {
            Vector3f otherToPrimary = new Vector3f(pCtr).sub(oCtr);
            if (otherToPrimary.dot(n) < 0f) n.negate();
        } else {
            // fallback: align with centroid->primary
            Vector3f toPrimary = new Vector3f(pCtr).sub(res.centroid(), new Vector3f());
            if (toPrimary.dot(n) < 0f) n.negate();
        }
        return n.normalize();
    }

    @Nullable
    private Vector3f tryGetCenter(@NotNull PhysicsShape shape) {
        if (shape instanceof OrientedBox obb) return new Vector3f(obb.center());
        // add other shapes here as needed
        return null;
    }

}
