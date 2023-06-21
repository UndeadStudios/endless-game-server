package com.zenyte.game.content.magicstorageunit;

import com.google.common.eventbus.Subscribe;
import com.zenyte.game.util.Colour;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.plugins.events.InitializationEvent;
import com.zenyte.plugins.events.LoginEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import mgi.types.config.enums.Enums;
import mgi.types.config.items.ItemDefinitions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * @author Kris | 15/09/2020
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class MagicStorageUnit {
    public static final int STORAGE_UNIT_UIM_UNLOCK_COST = 1000000;
    public static final int STORAGE_UNIT_NORMAL_UNLOCK_COST = 2500000;
    private final Map<Integer, List<Integer>> storedSets;
    private int unlockPayment;

    public MagicStorageUnit() {
        this.storedSets = new Int2ObjectOpenHashMap<>();
    }

    @Subscribe
    public static final void onInitialization(final InitializationEvent event) {
        final com.zenyte.game.world.entity.player.Player player = event.getPlayer();
        final com.zenyte.game.world.entity.player.Player savedPlayer = event.getSavedPlayer();
        final com.zenyte.game.content.magicstorageunit.MagicStorageUnit storageUnit = savedPlayer.getMagicStorageUnit();
        if (storageUnit != null && storageUnit.storedSets != null) {
            final com.zenyte.game.content.magicstorageunit.MagicStorageUnit unit = player.getMagicStorageUnit();
            unit.storedSets.putAll(storageUnit.storedSets);
            unit.unlockPayment = storageUnit.unlockPayment;
        }
    }

    @Subscribe
    public static final void onLogin(final LoginEvent event) {
        event.getPlayer().getMagicStorageUnit().refeshVarbit(event.getPlayer());
    }

    public void refeshVarbit(@NotNull final Player player) {
        player.getVarManager().sendBit(16001, unlockPayment == 0 ? 0 : 1);
    }

    public List<Integer> getStoredItemIds(final int displayedItem) {
        return storedSets.get(displayedItem);
    }

    public void remove(final int displayedItem) {
        storedSets.remove(displayedItem);
    }

    public void store(@NotNull final Player player, final int itemId) {
        final java.util.Optional<java.util.List<com.zenyte.game.content.magicstorageunit.StorageUnitElement>> optionalSets = StorageUnitCollection.getSingleton().findElement(itemId);
        if (!optionalSets.isPresent()) {
            throw new IllegalStateException();
        }
        final java.util.List<com.zenyte.game.content.magicstorageunit.StorageUnitElement> sets = optionalSets.get();
        for (final com.zenyte.game.content.magicstorageunit.StorageUnitElement set : sets) {
            final it.unimi.dsi.fastutil.ints.IntList list = buildHeldItemsList(player, set);
            if (list == null) {
                continue;
            }
            if (storedSets.get(set.getDisplayItem()) != null) {
                player.sendMessage("You\'ve already stored that item set in your magic storage unit.");
                return;
            }
            storedSets.put(set.getDisplayItem(), list);
            final mgi.types.config.enums.StringEnum storageEnum = Enums.COSTUME_STORAGE_UNIT_ENUM;
            final java.lang.String name = storageEnum.getValue(set.getDisplayItem()).orElseThrow(RuntimeException::new);
            player.sendMessage("You add the " + name + " to the magic storage unit.");
            final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
            for (final java.lang.Integer id : list) {
                inventory.deleteItem(id, 1);
            }
            return;
        }
        notifyImpartialCollection(player, sets.get(0));
    }

    private void notifyImpartialCollection(@NotNull final Player player, final StorageUnitElement element) {
        final mgi.types.config.enums.StringEnum storageEnum = Enums.COSTUME_STORAGE_UNIT_ENUM;
        final java.lang.String name = storageEnum.getValue(element.getDisplayItem()).orElseThrow(RuntimeException::new);
        final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
        final boolean singular = element.singular();
        player.sendMessage("You need the following items to add the " + name + " in your magic storage unit:");
        for (final com.zenyte.game.content.magicstorageunit.StorableSetPiece piece : element.getPieces()) {
            final int[] ids = piece.getIds();
            if (ids.length == 1 || singular || ids.length > 2) {
                final int id = ids[0];
                final com.zenyte.game.util.Colour colourPrefix = inventory.containsItem(id, 1) ? Colour.GREEN : Colour.RED;
                player.sendMessage(colourPrefix.wrap("1 x " + ItemDefinitions.nameOf(id)));
            } else if (ids.length == 2) {
                final int firstId = ids[0];
                final int secondId = ids[1];
                final com.zenyte.game.util.Colour firstColourPrefix = inventory.containsItem(firstId, 1) ? Colour.GREEN : Colour.RED;
                final com.zenyte.game.util.Colour secondColourPrefix = inventory.containsItem(secondId, 1) ? Colour.GREEN : Colour.RED;
                player.sendMessage(firstColourPrefix.wrap("1 x " + ItemDefinitions.nameOf(firstId)) + " or " + secondColourPrefix.wrap("1 x " + ItemDefinitions.nameOf(secondId)));
            }
        }
    }

    private IntList buildHeldItemsList(@NotNull final Player player, @NotNull final StorageUnitElement element) {
        final it.unimi.dsi.fastutil.ints.IntArrayList list = new IntArrayList();
        final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
        for (final com.zenyte.game.content.magicstorageunit.StorableSetPiece piece : element.getPieces()) {
            final int[] ids = piece.getIds();
            int containedCount = 0;
            for (int id : ids) {
                if (inventory.containsItem(id, 1)) {
                    containedCount++;
                    list.add(id);
                }
            }
            if (containedCount == 0) {
                return null;
            }
        }
        return list;
    }

    public int getUnlockPayment() {
        return this.unlockPayment;
    }

    public void setUnlockPayment(final int unlockPayment) {
        this.unlockPayment = unlockPayment;
    }
}
