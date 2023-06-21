package com.zenyte.game.world.object;

import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.SoundEffect;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

public class Door {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Door.class);
	public static final SoundEffect doorOpenSound = new SoundEffect(81, 5, 0);
	public static final SoundEffect doorCloseSound = new SoundEffect(58, 5, 0);
	private static final Int2IntOpenHashMap MAP = new Int2IntOpenHashMap(500);
	private static final int X = 0;
	private static final int Y = 1;
	private static final int[][] COORD_OFFSETS = new int[][] {new int[] {-1, 0}, new int[] {0, 1}, new int[] {1, 0}, new int[] {0, -1}};
	private static final Map<WorldObject, WorldObject> interactedDoors = new Object2ObjectOpenHashMap<>();

	static final WorldObject handleDoor(final WorldObject door, final WorldObject interacted, final boolean open) {
		final int doorId = door.getId();
		final int type = door.getType();
		final int rotation = door.getRotation();
		final int nextRotation = open ? ((rotation + 1) & 3) : rotation;
		final int[] offsets = COORD_OFFSETS[type == 9 ? ((nextRotation + 1) & 3) : nextRotation];
		final com.zenyte.game.world.entity.Location location = new Location(door.getX() + offsets[X], door.getY() + offsets[Y], door.getPlane());
		final int id = interacted == null ? MAP.getOrDefault(doorId, doorId) : interacted.getId();
		final com.zenyte.game.world.object.WorldObject object = new WorldObject(id, type, open ? ((rotation - 1) & 3) : ((rotation + 1) & 3), location);
		World.removeObject(door);
		World.spawnObject(object);
		World.sendSoundEffect(door, open ? doorOpenSound : doorCloseSound);
		if (interacted == null) {
			interactedDoors.put(object, door);
		}
		return object;
	}

	public static final WorldObject handleDoor(final WorldObject door, final boolean isOpen) {
		final com.zenyte.game.world.object.WorldObject interacted = interactedDoors.remove(door);
		return handleDoor(door, interacted, isOpen);
	}

	static final WorldObject handleDoor(final WorldObject door) {
		final com.zenyte.game.world.object.WorldObject interacted = interactedDoors.remove(door);
		return handleDoor(door, interacted, interacted == null ? door.getDefinitions().containsOption("Close") : interacted.getDefinitions().containsOption("Open"));
	}

	public static final WorldObject handleGraphicalDoor(final WorldObject door, final WorldObject original) {
		final int doorId = door.getId();
		final boolean open = original != null ? original.getDefinitions().containsOption("Open") : door.getDefinitions().containsOption("Close");
		final int type = door.getType();
		final int rotation = door.getRotation();
		final int nextRotation = open ? ((rotation + 1) & 3) : rotation;
		final int[] offsets = COORD_OFFSETS[type == 9 ? ((nextRotation + 1) & 3) : nextRotation];
		final com.zenyte.game.world.entity.Location location = new Location(door.getX() + offsets[X], door.getY() + offsets[Y], door.getPlane());
		final int id = MAP.getOrDefault(doorId, doorId);
		final com.zenyte.game.world.object.WorldObject object = new WorldObject(id, type, open ? ((rotation - 1) & 3) : ((rotation + 1) & 3), location);
		World.sendSoundEffect(door, open ? doorOpenSound : doorCloseSound);
		if (original == null) {
			World.spawnGraphicalDoor(object);
			World.removeGraphicalDoor(door);
		} else {
			World.spawnGraphicalDoor(original);
			World.removeGraphicalDoor(door);
		}
		return object;
	}

	public static final void load() {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("data/objects/Door definitions.json"));
			final Int2IntOpenHashMap map = World.getGson().fromJson(br, Int2IntOpenHashMap.class);
			MAP.putAll(map);
		} catch (final FileNotFoundException e) {
			log.error(Strings.EMPTY, e);
		}
	}
}
