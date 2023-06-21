package com.zenyte.game.content.chambersofxeric.dialogue;

import com.zenyte.game.content.chambersofxeric.Raid;
import com.zenyte.game.content.chambersofxeric.greatolm.OlmRoom;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import org.apache.commons.lang3.mutable.MutableBoolean;

/**
 * @author Kris | 16. nov 2017 : 3:28.30
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public final class LeaveRaidD extends Dialogue {
	public LeaveRaidD(Player player, final Raid raid) {
		super(player);
		this.raid = raid;
	}

	private final Raid raid;

	@Override
	public void buildDialogue() {
		final org.apache.commons.lang3.mutable.MutableBoolean bool = new MutableBoolean();
		final org.apache.commons.lang3.mutable.MutableBoolean fullChest = new MutableBoolean();
		raid.ifInRoom(player.getLocation(), OlmRoom.class, room -> {
			bool.setTrue();
			final com.zenyte.game.content.chambersofxeric.rewards.RaidRewards rewards = raid.getRewards();
			if (rewards != null) {
				final java.util.Map<com.zenyte.game.world.entity.player.Player, com.zenyte.game.world.entity.player.container.Container> map = rewards.getRewardMap();
				if (map != null) {
					final com.zenyte.game.world.entity.player.container.Container container = map.get(player);
					if (container != null) {
						if (!container.isEmpty()) {
							fullChest.setTrue();
						}
					}
				}
			}
		});
		options(fullChest.isTrue() ? "You have stuff in the chest.<br>Are you sure you want to abandon the raid?" : "You will not be able to rejoin this raid again.", new DialogueOption("Leave the raid.", () -> raid.leaveRaid(player, false, bool.isTrue())), new DialogueOption("Stay."));
	}
}
