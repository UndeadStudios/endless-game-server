package com.zenyte.game.world.entity.npc;

import com.zenyte.game.world.entity.Entity;
import com.zenyte.game.world.entity.npc.combat.CombatScript;
import com.zenyte.game.world.entity.npc.combat.Default;

public class CombatScriptsHandler {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CombatScriptsHandler.class);
	public static final Default DEFAULT_SCRIPT = new Default();

	public static int specialAttack(final NPC npc, final Entity target) {
		npc.renewFlinch();
		if (npc instanceof CombatScript) {
			return ((CombatScript) npc).attack(target);
		}
		return DEFAULT_SCRIPT.attack(npc, target);
	}
}
