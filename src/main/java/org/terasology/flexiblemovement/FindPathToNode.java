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
package org.terasology.flexiblemovement;

import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.flexiblepathfinding.JPSConfig;
import org.terasology.flexiblepathfinding.LineOfSight3d;
import org.terasology.flexiblepathfinding.PathfinderCallback;
import org.terasology.flexiblepathfinding.PathfinderSystem;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

import java.util.List;

public class FindPathToNode extends Node {
    @Override
    public FindPathToTask createTask() {
        return new FindPathToTask(this);
    }

    private class FindPathToTask extends Task{
        Status pathStatus = null;
        List<Vector3i> pathResult = null;
        @In
        PathfinderSystem system;

        @In
        private WorldProvider world;
        private Time time;

        protected FindPathToTask(Node node) {
            super(node);
        }

        @Override
        public Status update(float dt) {
            if(pathStatus == null) {
                pathStatus = Status.RUNNING;
                FlexibleMovementComponent flexibleMovementComponent = actor().getComponent(FlexibleMovementComponent.class);
                Vector3i start = new Vector3i(actor().getComponent(LocationComponent.class).getWorldPosition());
                Vector3i goal = actor().getComponent(FlexibleMovementComponent.class).pathTarget;

                JPSConfig config = new JPSConfig(start, goal);
                config.useLineOfSight = true;
                config.maxTime = 0.5f;
                config.plugin = flexibleMovementComponent.getMovementPlugin(world, time).getPathfindingPlugin(actor().getEntity());
                system.requestPath(config, new PathfinderCallback() {
                    @Override
                    public void pathReady(List<Vector3i> path, Vector3i target) {
                        if(path == null || path.size() == 0) {
                            pathStatus = Status.FAILURE;
                            return;
                        }
                        pathStatus = Status.SUCCESS;
                        pathResult = path;
                    }
                });
            }

            if(pathStatus == Status.SUCCESS) {
                FlexibleMovementComponent movement = actor().getComponent(FlexibleMovementComponent.class);
                movement.path = pathResult;
                actor().save(movement);
            }

            return pathStatus;
        }

        @Override
        public void handle(Status result) {

        }
    }
}