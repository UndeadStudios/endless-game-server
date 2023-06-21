package com.zenyte.game.content.treasuretrails.plugins;

import com.zenyte.game.content.treasuretrails.ClueItem;
import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemId;
import com.zenyte.game.item.pluginextensions.ItemPlugin;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.Objects;

/**
 * @author Kris | 05/01/2020
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class SkillingClueItem extends ItemPlugin {
    @Override
    public void handle() {
        bind("Open", (player, item, container, slotId) -> {
            final java.lang.String name = item.getName().toLowerCase();
            final com.zenyte.game.content.treasuretrails.ClueItem clueItem = Objects.requireNonNull(ClueItem.getMap().get(item.getId()));
            final boolean nest = name.contains("nest");
            if (nest) {
                if (player.getInventory().getFreeSlots() < 1 && !player.getInventory().containsItem(clueItem.getScrollBox())) {
                    player.sendMessage("Not enough free space in your inventory.");
                    return;
                }
            }
            player.getInventory().deleteItem(item);
            player.getInventory().addOrDrop(new Item(clueItem.getScrollBox()));
            if (nest) {
                player.getInventory().addOrDrop(new Item(ItemId.BIRD_NEST_5075));
            }
        });
    }

    @Override
    public int[] getItems() {
        final it.unimi.dsi.fastutil.ints.IntOpenHashSet set = new IntOpenHashSet();
        for (final com.zenyte.game.content.treasuretrails.ClueItem item : ClueItem.values()) {
            set.add(item.getClueGeode());
            set.add(item.getClueBottle());
            set.add(item.getClueNest());
        }
        set.remove(-1);
        return set.toIntArray();
    }
}
