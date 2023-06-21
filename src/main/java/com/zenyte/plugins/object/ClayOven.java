package com.zenyte.plugins.object;

import com.zenyte.game.content.skills.cooking.CookingDefinitions;
import com.zenyte.game.item.Item;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.object.ObjectAction;
import com.zenyte.game.world.object.WorldObject;
import com.zenyte.plugins.dialogue.skills.CookingD;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import java.util.ArrayList;

/**
 * @author Kris | 21/03/2020
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class ClayOven implements ObjectAction {
    @Override
    public void handleObjectAction(Player player, WorldObject object, String name, int optionId, String option) {
        if (option.equals("Cook")) {
            final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
            final it.unimi.dsi.fastutil.ints.IntArrayList list = new IntArrayList();
            final boolean fire = object.getName().toLowerCase().contains("fire");
            final it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet<com.zenyte.game.content.skills.cooking.CookingDefinitions.CookingData> cookableSet = new ObjectLinkedOpenHashSet<CookingDefinitions.CookingData>();
            loop:
            for (int i = 0; i < 28; i++) {
                final com.zenyte.game.item.Item item = inventory.getItem(i);
                if (item == null) continue;
                final com.zenyte.game.content.skills.cooking.CookingDefinitions.CookingData[] food = CookingDefinitions.CookingData.isCooking(player, item, fire);
                if (food.length > 0) {
                    for (final com.zenyte.game.content.skills.cooking.CookingDefinitions.CookingData f : food) {
                        if (!list.contains(f.getCooked())) {
                            list.add(f.getCooked());
                            cookableSet.add(f);
                        }
                        if (list.size() >= 10) break loop;
                    }
                }
            }
            if (!list.isEmpty()) {
                final java.util.ArrayList<com.zenyte.game.item.Item> itemList = new ArrayList<Item>(list.size());
                for (int i : list) {
                    itemList.add(new Item(i));
                }
                final java.util.ArrayList<com.zenyte.game.content.skills.cooking.CookingDefinitions.CookingData> cookableList = new ArrayList<>(cookableSet);
                player.getDialogueManager().start(new CookingD(player, object, true, cookableList, itemList.toArray(new Item[0])));
            } else {
                player.sendMessage("You have nothing to cook at the moment.");
            }
        }
    }

    @Override
    public Object[] getObjects() {
        return new Object[] {10377};
    }
}
