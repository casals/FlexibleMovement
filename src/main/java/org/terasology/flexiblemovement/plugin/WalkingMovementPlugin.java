/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.flexiblemovement.plugin;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.flexiblemovement.FlexibleMovementComponent;
import org.terasology.flexiblepathfinding.plugins.StandardPlugin;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.events.SetMovementModeEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.WorldProvider;

public class WalkingMovementPlugin extends MovementPlugin {
    public WalkingMovementPlugin(WorldProvider world, Time time) {
        super(world, time);
    }

    @Override
    public StandardPlugin getPathfindingPlugin(EntityRef entity) {
        return new org.terasology.flexiblepathfinding.plugins.WalkingPlugin(world);
    }

    @Override
    public CharacterMoveInputEvent move(EntityRef entity, Vector3f dest, int sequence) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        if(!getPathfindingPlugin(entity).isWalkable(new Vector3i(dest))) {
            return null;
        }

        CharacterMovementComponent movement = entity.getComponent(CharacterMovementComponent.class);
        FlexibleMovementComponent flexibleMovementComponent = entity.getComponent(FlexibleMovementComponent.class);
        Vector3f velocity = new Vector3f(dest).sub(location.getWorldPosition());
        velocity.y = 0;
        if(velocity.lengthSquared() > 1.0f) {
            velocity.normalize();
        }

        float yaw = (float) Math.atan2(velocity.x, velocity.z);
//        boolean shouldJump = location.getWorldPosition().distance(lastPos) < 0.3;
//        lastPos.set(location.getWorldPosition());
        if(movement.mode != MovementMode.WALKING) {
            entity.send(new SetMovementModeEvent(MovementMode.WALKING));
        }

        return new CharacterMoveInputEvent(sequence, 0, yaw * TeraMath.RAD_TO_DEG + 180, velocity, false, true, time.getGameDeltaInMs());
    }
}