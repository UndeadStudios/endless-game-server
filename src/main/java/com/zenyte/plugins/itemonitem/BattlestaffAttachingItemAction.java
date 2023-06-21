package com.zenyte.plugins.itemonitem;

import com.zenyte.game.content.skills.crafting.CraftingDefinitions.BattlestaffAttachingData;
import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemOnItemAction;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.plugins.dialogue.skills.BattlestaffAttachingD;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * @author Kris | 11. nov 2017 : 0:36.08
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class BattlestaffAttachingItemAction implements ItemOnItemAction {
	@Override
	public void handleItemOnItemAction(final Player player, final Item from, final Item to, final int fromSlot, final int toSlot) {
		final com.zenyte.game.content.skills.crafting.CraftingDefinitions.BattlestaffAttachingData staff = BattlestaffAttachingData.getDataByMaterial(from, to);
		if (staff != null && BattlestaffAttachingData.hasRequirements(player, staff)) {
			player.getDialogueManager().start(new BattlestaffAttachingD(player, staff));
			return;
		} else {
			player.sendMessage("Nothing interesting happens");
		}
	}

	@Override
	public int[] getItems() {
		final it.unimi.dsi.fastutil.ints.IntArrayList list = new IntArrayList();
		for (final com.zenyte.game.content.skills.crafting.CraftingDefinitions.BattlestaffAttachingData data : BattlestaffAttachingData.VALUES_ARR) {
			for (final com.zenyte.game.item.Item item : data.getMaterials()) {
				list.add(item.getId());
			}
		}
		return list.toArray(new int[list.size()]);
	}
}
