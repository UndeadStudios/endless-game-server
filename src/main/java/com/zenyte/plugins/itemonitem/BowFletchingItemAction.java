package com.zenyte.plugins.itemonitem;

import com.zenyte.game.content.skills.fletching.FletchingDefinitions.BowFletchingData;
import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemOnItemAction;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.plugins.dialogue.skills.BowStringingD;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * @author Kris | 11. nov 2017 : 0:32.30
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class BowFletchingItemAction implements ItemOnItemAction {
	@Override
	public void handleItemOnItemAction(final Player player, final Item from, final Item to, final int fromSlot, final int toSlot) {
		final com.zenyte.game.content.skills.fletching.FletchingDefinitions.BowFletchingData bow = BowFletchingData.getDataByMaterial(from, to);
		if (bow != null && BowFletchingData.hasRequirements(player, bow)) {
			player.getDialogueManager().start(new BowStringingD(player, bow));
			return;
		} else {
			player.sendMessage("Nothing interesting happens");
		}
	}

	@Override
	public int[] getItems() {
		final it.unimi.dsi.fastutil.ints.IntArrayList list = new IntArrayList();
		for (final com.zenyte.game.content.skills.fletching.FletchingDefinitions.BowFletchingData data : BowFletchingData.VALUES_ARR) {
			for (final com.zenyte.game.item.Item item : data.getMaterials()) {
				list.add(item.getId());
			}
		}
		return list.toArray(new int[list.size()]);
	}
}
