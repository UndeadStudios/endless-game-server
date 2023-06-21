package com.zenyte.game.world.object;

import com.zenyte.game.tasks.WorldTask;
import com.zenyte.game.tasks.WorldTasksManager;
import com.zenyte.game.world.World;

import java.util.HashMap;
import java.util.Map;

public class DoorHandler {
	private static final Map<WorldObject, WorldTask> RUNNING_DOORS = new HashMap<WorldObject, WorldTask>();

	public static final void handleDoor(final WorldObject object) {
		handleDoor(object, 500);
	}

	public static final void handleDoor(final WorldObject object, final int delay) {
		final boolean spawned = World.isSpawnedObject(object);
		final com.zenyte.game.world.object.WorldObject door = Door.handleDoor(object);
		final com.zenyte.game.tasks.WorldTask runningTask = RUNNING_DOORS.remove(object);
		if (runningTask != null) {
			runningTask.stop();
		}
		if (!spawned) {
			WorldTask task = new WorldTask() {
				@Override
				public void run() {
					if (RUNNING_DOORS.get(door) != this) {
						return;
					}
					Door.handleDoor(door);
					RUNNING_DOORS.remove(door);
				}
			};
			WorldTasksManager.schedule(task, delay);
			RUNNING_DOORS.put(door, task);
		}
	}

	public static final void handleDoor(final WorldObject object, final int delay, final boolean isOpen) {
		final boolean spawned = World.isSpawnedObject(object);
		final com.zenyte.game.world.object.WorldObject door = Door.handleDoor(object, isOpen);
		final com.zenyte.game.tasks.WorldTask runningTask = RUNNING_DOORS.remove(object);
		if (runningTask != null) {
			runningTask.stop();
		}
		if (!spawned) {
			WorldTask task = new WorldTask() {
				@Override
				public void run() {
					if (RUNNING_DOORS.get(door) != this) {
						return;
					}
					Door.handleDoor(door);
					RUNNING_DOORS.remove(door);
				}
			};
			WorldTasksManager.schedule(task, delay);
			RUNNING_DOORS.put(door, task);
		}
	}
}
