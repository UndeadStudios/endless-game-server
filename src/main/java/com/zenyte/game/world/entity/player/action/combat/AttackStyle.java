package com.zenyte.game.world.entity.player.action.combat;

import com.zenyte.game.world.entity.npc.combatdefs.AttackType;

public final class AttackStyle {
	public AttackStyle(final AttackType type, final AttackExperienceType experienceType) {
		this.type = type;
		this.experienceType = experienceType;
	}

	private final AttackType type;
	private final AttackExperienceType experienceType;


	public enum AttackExperienceType {
		ATTACK_XP, STRENGTH_XP, DEFENCE_XP, RANGED_XP, MAGIC_XP, RANGED_DEFENCE_XP, MAGIC_DEFENCE_XP, SHARED_XP, NOT_AVAILABLE;
	}

	public AttackType getType() {
		return this.type;
	}

	public AttackExperienceType getExperienceType() {
		return this.experienceType;
	}
}
