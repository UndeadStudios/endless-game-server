package com.zenyte.game.world.entity.npc.drop.matrix;

import com.zenyte.game.world.entity.player.MemberRank;
import com.zenyte.game.world.entity.player.Player;

public class Drop {

	private int itemId, minAmount, maxAmount;
	private Player player;
	private int rate;

	//private boolean rare;

	public Drop(final Drop drop, Player player) {
		if (player.getMemberRank().eligibleTo(MemberRank.ZENYTE_MEMBER)) {
			this.itemId = drop.itemId;
			this.minAmount = drop.minAmount;
			this.maxAmount = drop.maxAmount;
			this.rate *= 1.5;
		}
		else if (!player.isMember()) {
			this.itemId = drop.itemId;
			this.minAmount = drop.minAmount;
			this.maxAmount = drop.maxAmount;
			this.rate = drop.rate;
			this.player = player;
		}
	}

	public Drop(int itemId, int rate, int minAmount, int maxAmount/*, boolean rare*/) {
		if (player.isMember()) {
			this.itemId = itemId;
			this.rate += 3;
			this.minAmount = minAmount;
			this.maxAmount = maxAmount;
		}
		else if (!player.isMember()) {
			this.itemId = itemId;
			this.rate = rate;
			this.minAmount = minAmount;
			this.maxAmount = maxAmount;
			//this.rare = rare;
		}
	}

	public Drop(int itemId, int rate, int minAmount) {
		if (player.getMemberRank().eligibleTo(MemberRank.ZENYTE_MEMBER)) {
			this.rate -= 0.5;
		}
		else if (!player.getMemberRank().eligibleTo(MemberRank.ZENYTE_MEMBER)) {
		this.itemId = itemId;
		this.rate = rate;
		this.minAmount = minAmount;
		this.maxAmount = minAmount;
	}
	//	this.rare = false;
	}


	public int getExtraAmount() {
		return maxAmount - minAmount;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(short itemId) {
		this.itemId = itemId;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(int amount) {
		this.maxAmount = amount;
	}

	public int getMinAmount() {
		return minAmount;
	}

	public void setMinAmount(int amount) {
		this.minAmount = amount;
	}

	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}

	public boolean isAlways() {
	    return rate == 100000;
    }

	/*public boolean isFromRareTable() {
		return rare;
	}*/
}