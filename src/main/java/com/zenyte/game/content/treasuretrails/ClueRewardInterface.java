package com.zenyte.game.content.treasuretrails;

import com.zenyte.game.constants.GameInterface;
import com.zenyte.game.item.Item;
import com.zenyte.game.ui.Interface;
import com.zenyte.game.util.AccessMask;
import com.zenyte.game.util.Examine;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.container.Container;
import com.zenyte.game.world.entity.player.container.ContainerPolicy;
import com.zenyte.game.world.entity.player.container.impl.ContainerType;

import java.util.List;
import java.util.Optional;

/**
 * @author Kris | 25/10/2019
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class ClueRewardInterface extends Interface {
    @Override
    protected void attach() {
        put(3, "Item list");
    }

    @Override
    public void open(Player player) {
        final java.lang.Object loot = player.getTemporaryAttributes().remove("treasure trails loot");
        if (!(loot instanceof List)) {
            throw new IllegalStateException("No loot list provided.");
        }
        @SuppressWarnings("unchecked cast")
        final java.util.List<com.zenyte.game.item.Item> rewards = (List<Item>) loot;
        final com.zenyte.game.world.entity.player.container.Container container = new Container(ContainerPolicy.ALWAYS_STACK, ContainerType.BARROWS_CHEST, Optional.of(player));
        container.add(rewards.toArray(new Item[0]));
        container.setFullUpdate(true);
        player.getPacketDispatcher().sendUpdateItemContainer(container);
        player.getInterfaceHandler().sendInterface(this);
        player.getTemporaryAttributes().put("treasure trails rewards container", container);
        player.getPacketDispatcher().sendComponentSettings(getInterface(), getComponent("Item list"), 0, 10, AccessMask.CLICK_OP10);
    }

    @Override
    public void close(final Player player, final Optional<GameInterface> replacement) {
        final java.lang.Object container = player.getTemporaryAttributes().remove("treasure trails rewards container");
        if (!(container instanceof Container)) {
            return;
        }
        final com.zenyte.game.world.entity.player.container.Container typedContainer = (Container) container;
        final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
        final com.zenyte.game.world.entity.player.collectionlog.CollectionLog log = player.getCollectionLog();
        for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<com.zenyte.game.item.Item> itemEntry : typedContainer.getItems().int2ObjectEntrySet()) {
            final com.zenyte.game.item.Item item = itemEntry.getValue();
            if (item == null) {
                continue;
            }
            inventory.addOrDrop(item);
            log.add(item);
        }
    }

    @Override
    protected void build() {
        bind("Item list", (player, slotId, itemId, option) -> Examine.sendItemExamine(player, itemId));
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.CLUE_SCROLL_REWARD;
    }
}
