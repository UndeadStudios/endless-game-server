package com.zenyte.game.content.chambersofxeric.storageunit;

import com.zenyte.game.constants.GameInterface;
import com.zenyte.game.ui.SwitchPlugin;
import com.zenyte.game.util.AccessMask;
import com.zenyte.game.world.entity.player.GameMode;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.container.impl.bank.Bank;

/**
 * @author Kris | 21/07/2019 02:41
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class PrivateStorageInterface extends StorageInterface implements SwitchPlugin {
    @Override
    protected void attach() {
        put(4, "Size");
        put(5, "Shared storage button");
        put(6, "Interact with item");
        put(9, "Withdraw all");
        put(10, "Deposit/Bank all");
    }

    @Override
    public void open(final Player player) {
        final com.zenyte.game.content.chambersofxeric.storageunit.PrivateStorage storage = player.getPrivateStorage();
        final com.zenyte.game.world.entity.player.container.Container container = storage.getContainer();
        final java.lang.Object attribute = player.getTemporaryAttributes().remove("private storage size");
        if (!(attribute instanceof Number)) {
            throw new IllegalStateException("Size undefined");
        }
        final int size = ((Number) attribute).intValue();
        container.setContainerSize(size == -1 ? 90 : size);
        if (size == -1) {
            storage.resetInaccessibleItems();
        }
        final com.zenyte.game.packet.PacketDispatcher dispatcher = player.getPacketDispatcher();
        dispatcher.sendComponentText(getInterface(), getComponent("Size"), size == -1 ? "N/A" : size);
        dispatcher.sendComponentVisibility(getInterface(), getComponent("Shared storage button"), size == -1);
        dispatcher.sendComponentSettings(getInterface(), getComponent("Interact with item"), 0, container.getContainerSize(), AccessMask.CLICK_OP1, AccessMask.CLICK_OP2, AccessMask.CLICK_OP3, AccessMask.CLICK_OP4, AccessMask.CLICK_OP5, AccessMask.CLICK_OP10, AccessMask.DRAG_DEPTH1, AccessMask.DRAG_TARGETABLE);
        container.shift();
        container.setFullUpdate(true);
        storage.refresh();
        player.getInterfaceHandler().sendInterface(this);
        player.getVarManager().sendBit(3459, 1);
        GameInterface.RAIDS_STORAGE_INVENTORY_INTERFACE.open(player);
    }

    @Override
    protected void build() {
        bind("Shared storage button", player -> player.getRaid().ifPresent(raid -> GameInterface.RAIDS_SHARED_STORAGE.open(player)));
        bind("Interact with item", (player, slotId, itemId, option) -> {
            final com.zenyte.game.content.chambersofxeric.storageunit.PrivateStorage storage = player.getPrivateStorage();
            final com.zenyte.game.world.entity.player.container.Container container = storage.getContainer();
            final int slot = container.getSlotOf(itemId);
            final com.zenyte.game.item.Item item = container.get(slot);
            if (item == null) {
                return;
            }
            if (player.getGameMode().equals(GameMode.ULTIMATE_IRON_MAN) && player.getRaid().isPresent()) {
                player.sendMessage("You cannot use the storage units as an ultimate ironman.");
                return;
            }
            handleInteraction(player, storage, option, slot, item);
        });
        bind("Withdraw all", player -> {
            if (player.getGameMode().equals(GameMode.ULTIMATE_IRON_MAN) && player.getRaid().isPresent()) {
                player.sendMessage("You cannot use the storage units as an ultimate ironman.");
                return;
            }
            final com.zenyte.game.content.chambersofxeric.storageunit.PrivateStorage storage = player.getPrivateStorage();
            final com.zenyte.game.world.entity.player.container.Container container = storage.getContainer();
            if (container.getSize() == 0) {
                player.sendMessage("There\'s nothing to withdraw.");
                return;
            }
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                final com.zenyte.game.item.Item item = container.get(slot);
                if (item == null) {
                    continue;
                }
                player.getInventory().getContainer().deposit(null, container, slot, item.getAmount());
            }
            player.getInventory().refreshAll();
            container.refresh(player);
            if (container.getSize() != 0) {
                player.sendMessage("Not enough space in your inventory.");
            }
        });
        bind("Deposit/Bank all", player -> {
            if (player.getGameMode().equals(GameMode.ULTIMATE_IRON_MAN) && player.getRaid().isPresent()) {
                player.sendMessage("You cannot use the storage units as an ultimate ironman.");
                return;
            }
            final java.util.Optional<com.zenyte.game.content.chambersofxeric.Raid> raid = player.getRaid();
            final com.zenyte.game.content.chambersofxeric.storageunit.PrivateStorage storage = player.getPrivateStorage();
            if (raid.isPresent()) {
                final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
                final com.zenyte.game.world.entity.player.container.Container container = inventory.getContainer();
                if (container.getSize() == 0) {
                    player.sendMessage("There\'s nothing to deposit..");
                    return;
                }
                for (int slot = 0; slot < container.getContainerSize(); slot++) {
                    final com.zenyte.game.item.Item item = container.get(slot);
                    if (item == null || Bank.unbankableItems.contains(item.getId())) {
                        continue;
                    }
                    final java.util.function.Predicate<com.zenyte.game.world.entity.player.Player> predicate = Bank.predicateMap.get(item.getId());
                    if (predicate != null && !predicate.test(player)) {
                        continue;
                    }
                    storage.getContainer().deposit(null, container, slot, item.getAmount());
                }
                storage.refresh();
                container.refresh(player);
                if (container.getSize() != 0) {
                    player.sendMessage("Not enough space in your private storage.");
                }
                return;
            }
            final com.zenyte.game.world.entity.player.container.Container container = storage.getContainer();
            if (container.getSize() == 0) {
                player.sendMessage("There\'s nothing to bank.");
                return;
            }
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                final com.zenyte.game.item.Item item = container.get(slot);
                if (item == null) {
                    continue;
                }
                player.getBank().getContainer().deposit(null, container, slot, item.getAmount());
            }
            player.getBank().refreshContainer();
            container.refresh(player);
            if (container.getSize() != 0) {
                player.sendMessage("Not enough space in your bank.");
            }
        });
        bind("Interact with item", "Interact with item", (player, fromSlot, toSlot) -> player.getPrivateStorage().switchItem(fromSlot, toSlot));
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.RAIDS_PRIVATE_STORAGE;
    }
}
