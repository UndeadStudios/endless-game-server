package com.zenyte.game.world.entity.npc;

import com.zenyte.game.util.Direction;
import com.zenyte.game.world.World;
import org.apache.logging.log4j.util.Strings;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kris | 16. juuni 2018 : 20:20:31
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>}
 */
public class AbstractNPCManager {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractNPCManager.class);
	private static final List<NPC> ABSTRACT_NPCS = new ArrayList<>();

	public static final void add(final Class<? extends Spawnable> c) {
		try {
			if (Modifier.isAbstract(c.getModifiers())) return;
			final java.lang.reflect.Constructor<? extends com.zenyte.game.world.entity.npc.Spawnable> constructor = c.getDeclaredConstructor(World.NPC_INVOCATION_ARGUMENTS);
			constructor.setAccessible(true);
			final com.zenyte.game.world.entity.npc.Spawnable npc = constructor.newInstance(-1, null, Direction.SOUTH, 0);
			ABSTRACT_NPCS.add((NPC) npc);
		} catch (final Exception e) {
			log.error(Strings.EMPTY, e);
		}
	}

	public static final Class<? extends NPC> get(final int id, final String name) {
		for (final NPC n : ABSTRACT_NPCS) {
			if (((Spawnable) n).validate(id, name)) {
				return n.getClass();
			}
		}
		return NPC.class;
	}
}
