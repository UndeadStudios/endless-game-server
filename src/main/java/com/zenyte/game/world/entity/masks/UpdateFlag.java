package com.zenyte.game.world.entity.masks;

/**
 * @author Kris | 6. nov 2017 : 14:34.26
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public enum UpdateFlag {
	/**
	 * Appearance update.
	 */
	APPEARANCE(1, -1), /**
	 * Graphics update.
	 */
	GRAPHICS(512, 8), /**
	 * Animation update.
	 */
	ANIMATION(128, 32), /**
	 * Forced chat update.
	 */
	FORCED_CHAT(32, 2), /**
	 * Interacting entity update.
	 */
	FACE_ENTITY(2, 1), /**
	 * Face coordinate entity update.
	 */
	FACE_COORDINATE(4, 64), /**
	 * Hit update.
	 */
	HIT(64, 16), /**
	 * Update flag used to define player's current movement type (walk or run)
	 */
	MOVEMENT_TYPE(2048, -1), /**
	 * Update flag used to force move player.
	 */
	FORCE_MOVEMENT(1024, -1), /**
	 * Update flag used to set player's movement type for one tick (teleport or walk - supports run as well but never used)
	 */
	TEMPORARY_MOVEMENT_TYPE(4096, -1), /**
	 * Update flag used to set player's right-click strings (before name, after name and after combat)
	 */
	NAMETAG(256, -1), /**
	 * Update flag used for chat messages.
	 */
	CHAT(16, -1), /**
	 * Update flag used to transform a npc to a different one.
	 */
	TRANSFORMATION(-1, 4), /**
	 * Update flag used to set an option on an npc.
	 */
	OPTION(-1, 128);
	private final int playerMask;
	private final int npcMask;

	UpdateFlag(final int playerMask, final int npcMask) {
		this.playerMask = playerMask;
		this.npcMask = npcMask;
	}

	public static final UpdateFlag[] VALUES = values();

	public int getPlayerMask() {
		return this.playerMask;
	}

	public int getNpcMask() {
		return this.npcMask;
	}
}
