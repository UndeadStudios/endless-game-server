package com.zenyte.game.world.region;

public class XTEA {
	private int region;
	private int[] keys;

	public XTEA(final int region, final int[] keys) {
		this.region = region;
		this.keys = keys;
	}

	public int getRegion() {
		return this.region;
	}

	public int[] getKeys() {
		return this.keys;
	}
}
