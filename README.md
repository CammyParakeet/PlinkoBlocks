# 🛠️ PlinkoBlocks Project

## Progress Report (06/08/2025)

## Core Systems Overview

### Simulation & Physics
- **PlinkoSimulator**: Handles the core simulation loop for falling Plinko objects
    - Applies gravity, velocity, terminal velocity cap
    - Performs SAT-based collision detection via `PhysicsSeparatingAxis`
    - Generates a sequence of `PlinkoKeyframe`s representing the object's motion
- **Collision System**:
    - SAT resolution implemented in `PhysicsSeparatingAxis`.
    - Generates a `CollisionResult` with contact point, normal, and penetration depth
    - Used by `CollisionResponse` to reflect velocity and apply spin

### Animation Pipeline
- **PlinkoKeyframe**: Contains transformation, tick, and collision metadata
- **PlinkoAnimation**: Wraps a list of keyframes for a simulation run
- **ObjectAnimator**:
    - Plays through a `PlinkoAnimation` frame-by-frame
    - Interpolates transforms and prepares display updates per tick
- **PlinkoRenderer**:
    - Singleton manager using Guice + AutoService
    - Maintains an active set of `ObjectAnimator` instances
    - Runs a repeating async task (`runTaskTimerAsynchronously`) to tick each animator
    - Handles cleanup of completed animations automatically

```java
@Singleton
@AutoService(Manager.class)
public class PlinkoRenderer implements Manager {
    // ...
}
```

### Plinko Object System
- **PlinkoObject**:
    - Represents a single physical instance in simulation
    - Stores position, velocity, angular rotation, shape, and display logic
- **PlinkoObjectConfig**:
    - Configurable properties like mass, shape type, scale, material, bounciness, stickiness
    - Being refactored to support `DisplayOptions` for clean rendering configuration
- **PlinkoObjectFactory**:
    - Instantiates and wires up new `PlinkoObject`s using configuration and context

### Display System
- Foundation laid for modular display update logic:
    - `PlinkoObjectUpdate`: Will handle display transform application per frame
    - Flexible rendering planned to support block, item, or model displays
    - Display entity suppliers/factories will be configurable per run via `PlinkoObjectConfig`

---

### 🧱 Collision System

The collision system is a key foundation of the simulation engine,  
providing precise and efficient physics interactions between falling Plinko objects and static obstacles (e.g., pegs).  
It currently supports **Oriented Bounding Box (OBB) vs OBB** collision using the **Separating Axis Theorem (SAT)**.

#### 📐 PhysicsShape Interface
- `PhysicsShape` is the sealed interface implemented by all collidable objects (currently only `OrientedBox`)
- Each shape has:
    - A center point
    - A `collide(...)` method which returns a `CollisionResult` if overlap occurs

#### 📦 OrientedBox
- Represents a 3D box with arbitrary rotation and size
- Used for both falling objects and static board pegs
- Stores center, half-size vector, and a 3x3 orientation matrix
- Uses 15 axes in SAT (3 local axes from each box, plus 9 cross products)

#### 📊 Collision Detection: `PhysicsSeparatingAxis`
- Implements **SAT** using the full set of separating axes between two OBBs
- If **no axis** exists along which projections do **not overlap**, a collision is detected
- Computes:
    - The **smallest overlap axis** (for collision normal)
    - **Penetration depth** (smallest overlap)
    - **Contact point** (approximate, based on object center direction)
- Returns a `CollisionResult` containing:
    - `contactPoint`: where the collision likely occurred
    - `normal`: direction to push the object out of penetration
    - `penetrationDepth`: how deeply the objects are overlapping
    - `other`: reference to the collided shape

#### 🎯 Collision Response: `CollisionResponse`
- Takes a `PlinkoObject` and a `CollisionResult` to apply realistic bounce effects
- Reflects velocity along the collision normal:
    - Accounts for object's mass, normal direction, and bounciness
    - Spin/torque can be applied using angular velocity (MVP currently basic)
- Planned: add stickiness, restitution damping, and proper rotational spin response later

#### 🔒 Static Obstacle Handling
- Static shapes like pegs are considered immovable (infinite mass)
- Collision response assumes static objects do not get displaced
- Only the falling object's state is updated on collision

---

## Current Project Structure
<details>
<summary>View</summary>

```
main/
│   ├── java.com.glance.plinko/
│   │   ├── bootstrap/
│   │   │   └── GuiceServiceLoader.java
│   │   ├── platform.paper/
│   │   │   ├── command/
│   │   │   │   ├── core
│   │   │   │   └── engine/
│   │   │   │       ├── argument/
│   │   │   │       │   └── TypedArgParser.java
│   │   │   │       ├── suggestion/
│   │   │   │       │   └── SuggestionHelpers.java
│   │   │   │       ├── CommandHandler.java
│   │   │   │       └── CommandManager.java
│   │   │   ├── config/
│   │   │   │   └── PlinkoObjectConfig.java
│   │   │   ├── display/
│   │   │   │   ├── DisplayOptions.java
│   │   │   │   ├── DisplayUpdate.java
│   │   │   │   └── PlinkoDisplayFactory.java
│   │   │   ├── event
│   │   │   ├── game/
│   │   │   │   ├── animation/
│   │   │   │   │   ├── ObjectAnimator.java
│   │   │   │   │   ├── PlinkoAnimation.java
│   │   │   │   │   ├── PlinkoKeyframe.java
│   │   │   │   │   └── PlinkoRenderer.java
│   │   │   │   ├── physics/
│   │   │   │   │   ├── collision/
│   │   │   │   │   │   ├── CollisionResponse.java
│   │   │   │   │   │   ├── CollisionResult.java
│   │   │   │   │   │   ├── PhysicsCollider.java
│   │   │   │   │   │   └── PhysicsSeparatingAxis.java
│   │   │   │   │   └── shape/
│   │   │   │   │       ├── OrientedBox.java
│   │   │   │   │       ├── PhysicsShape.java
│   │   │   │   │       └── ShapeType.java
│   │   │   │   ├── simulation/
│   │   │   │   │   ├── factory/
│   │   │   │   │   │   └── PlinkoObjectFactory.java
│   │   │   │   │   ├── PlinkoObject.java
│   │   │   │   │   ├── PlinkoRunContext.java
│   │   │   │   │   └── PlinkoSimulator.java
│   │   │   │   ├── GameManager.java
│   │   │   │   └── PlinkoBoard.java
│   │   │   ├── inject/
│   │   │   │   ├── PaperComponentScanner.java
│   │   │   │   └── PlinkoModule.java
│   │   │   ├── listener
│   │   │   └── PlinkoBlocks.java
│   │   └── utils/
│   │       ├── data
│   │       └── lifecycle/
│   │           └── Manager.java
```
</details>

---

## In-Progress / TODO

### Display System
- [ ] Implement generic `DisplayEntityFactory` interface for rendering objects based on a config

### Animation Playback
- [ ] Support rendering presimulated vs live-simulated runs
- [ ] We want to make in game tests (with commands) that will test animation and collisions using

### Game Logic
- [ ] Detect final slot from simulation (`finalSlot` in `PlinkoAnimation`)
- [ ] Connect falling object and outcome to PlinkoBoard/game logic

---

## Future Considerations

- Physics material properties (`bounciness`, `stickiness`) to influence collision response
- Optional GPU-friendly interpolation system
- Replay system using saved `PlinkoAnimation` objects
- Multi-entity display support (e.g., particles, effects per tick)
- Optional live physics step-by-step visualization

---

## Summary

The simulation and animation pipeline is now fully wired up, modular, and task-driven.   
We've laid a solid foundation for object rendering, dynamic Plinko board runs,   
and eventual expansion into live physics or VFX-enhanced playback.   
Next steps focus on display entity setup and configurable rendering logic.




