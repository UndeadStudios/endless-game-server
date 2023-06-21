package com.zenyte.game.item;

import com.zenyte.game.world.entity.player.Player;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.apache.logging.log4j.util.Strings;

/**
 * @author Kris | 10. nov 2017 : 23:59.43
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class ItemOnItemHandler {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ItemOnItemHandler.class);
	private static final Long2ObjectOpenHashMap<ItemOnItemAction> INT_ACTIONS = new Long2ObjectOpenHashMap<>();
	private static final Int2ObjectOpenHashMap<ItemOnItemAction> useOnAny = new Int2ObjectOpenHashMap<>();

	public static final void add(final Class<?> c) {
		try {
			if (c.isAnonymousClass() || c.isInterface()) {
				return;
			}
			final Object o = c.newInstance();
			if (!(o instanceof ItemOnItemAction)) {
				return;
			}
			final ItemOnItemAction action = (ItemOnItemAction) o;
			final boolean include = action.includeEquivalentItems();
			final com.zenyte.game.item.ItemOnItemAction.ItemPair[] pairs = action.getMatchingPairs();
			if (pairs != null) {
				for (final com.zenyte.game.item.ItemOnItemAction.ItemPair pair : pairs) {
					final int left = pair.getLeft();
					final int right = pair.getRight();
					final int first = Math.max(left, right);
					final int second = Math.min(left, right);
					INT_ACTIONS.put((((long) (first)) << 32) | (second & 4294967295L), action);
				}
				return;
			}
			final int[] items = action.getItems();
			final int length = items.length;
			if (action.allItems()) {
				for (final int item : items) {
					useOnAny.put(item, action);
				}
			} else {
				for (int i = length - 1; i >= 0; i--) {
					final int itemUsed = items[i];
					for (int a = length - 1; a >= 0; a--) {
						final int usedWith = items[a];
						if (itemUsed == usedWith && !include) {
							continue;
						}
						final int first = Math.max(itemUsed, usedWith);
						final int second = Math.min(itemUsed, usedWith);
						INT_ACTIONS.put((((long) (first)) << 32) | (second & 4294967295L), action);
					}
				}
			}
		} catch (final Exception e) {
			log.error(Strings.EMPTY, e);
		}
	}

	public static final void handleItemOnItem(final Player player, final Item used, final Item usedWith, final int fromSlot, final int toSlot) {
		//player.stopAll();
		player.stop(Player.StopType.INTERFACES);
		final int itemId = used.getId();
		final int usedWithId = usedWith.getId();
		final int first = Math.max(itemId, usedWithId);
		final int second = Math.min(itemId, usedWithId);
		final long hash = (((long) first) << 32) | (second & 4294967295L);
		ItemOnItemAction action = useOnAny.get(itemId);
		if (action == null) {
			action = useOnAny.get(usedWithId);
			if (action == null) {
				action = INT_ACTIONS.get(hash);
			}
		}
		if (action != null) {
			log.info("[" + action.getClass().getSimpleName() + "] " + used.getName() + "(" + (used.getId() + " x " + used.getAmount()) + ") -> " + usedWith.getName() + "(" + (usedWith.getId() + " x " + usedWith.getAmount()) + ") | Slots: " + fromSlot + " -> " + toSlot);
			action.handleItemOnItemAction(player, used, usedWith, fromSlot, toSlot);
			return;
		}
		player.sendMessage("Nothing interesting happens.");
	}
}
