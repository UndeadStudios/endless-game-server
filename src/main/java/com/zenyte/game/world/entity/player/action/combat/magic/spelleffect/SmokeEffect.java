package com.zenyte.game.world.entity.player.action.combat.magic.spelleffect;

import com.zenyte.game.util.Utils;
import com.zenyte.game.world.entity.Entity;
import com.zenyte.game.world.entity.Toxins.ToxinType;

public class SmokeEffect implements SpellEffect {

	public SmokeEffect(final int damage) {
		this.damage = damage;
	}

	private final int damage;

	@Override
	public void spellEffect(final Entity player, final Entity target, final int damage) {
		if (Utils.random(3) != 0) {
			return;
		}
		target.getToxins().applyToxin(ToxinType.POISON, this.damage);
	}

}
