package com.zenyte.game.content.achievementdiary;

public final class DiaryChunk {
	public DiaryChunk(final int varbit, final int size, final int greenVarbit) {
		this.varbit = varbit;
		this.size = size;
		this.greenVarbit = greenVarbit;
	}

	private final int varbit;
	private final int size;
	private final int greenVarbit;

	public int getVarbit() {
		return this.varbit;
	}

	public int getSize() {
		return this.size;
	}

	public int getGreenVarbit() {
		return this.greenVarbit;
	}
}
