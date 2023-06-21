package com.zenyte.game.world.entity.player.collectionlog;

import com.google.common.eventbus.Subscribe;
import com.zenyte.game.item.Item;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.entity.player.container.Container;
import com.zenyte.game.world.entity.player.container.ContainerPolicy;
import com.zenyte.game.world.entity.player.container.impl.ContainerType;
import com.zenyte.plugins.events.InitializationEvent;
import com.zenyte.plugins.events.PostInitializationEvent;
import com.zenyte.utils.StaticInitializer;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import mgi.types.config.StructDefinitions;
import mgi.types.config.enums.EnumDefinitions;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

import static com.zenyte.game.world.entity.player.collectionlog.CollectionLogInterface.STRUCT_POINTER_ENUM_CAT;
import static com.zenyte.game.world.entity.player.collectionlog.CollectionLogInterface.STRUCT_POINTER_SUB_ENUM_CAT;

/**
 * @author Kris | 13/03/2019 21:10
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
@StaticInitializer
public class CollectionLog {
    private static final IntOpenHashSet COLLECTION_LOG_ITEMS = new IntOpenHashSet(500);
    private static final IntOpenHashSet UNTRADABLE_COLLECTION_LOG_ITEMS = new IntOpenHashSet(350);

    static {
        for (final com.zenyte.game.world.entity.player.collectionlog.CLCategoryType type : CLCategoryType.values()) {
            final mgi.types.config.StructDefinitions struct = Objects.requireNonNull(StructDefinitions.get(type.struct()));
            final java.util.Optional<?> optional = struct.getValue(STRUCT_POINTER_ENUM_CAT);
            final java.lang.Object enumId = optional.orElseThrow(RuntimeException::new);
            assert enumId instanceof Integer;
            final mgi.types.config.enums.IntEnum categoryEnum = EnumDefinitions.getIntEnum((Integer) enumId);
            final it.unimi.dsi.fastutil.objects.ObjectSet<it.unimi.dsi.fastutil.ints.Int2IntMap.Entry> entrySet = categoryEnum.getValues().int2IntEntrySet();
            for (final it.unimi.dsi.fastutil.ints.Int2IntMap.Entry entry : entrySet) {
                final int subCategoryStructId = entry.getIntValue();
                final mgi.types.config.StructDefinitions subCategoryStruct = Objects.requireNonNull(StructDefinitions.get(subCategoryStructId));
                final int subEnumId = Integer.parseInt(subCategoryStruct.getValue(STRUCT_POINTER_SUB_ENUM_CAT).orElseThrow(RuntimeException::new).toString());
                final mgi.types.config.enums.IntEnum subEnum = EnumDefinitions.getIntEnum(subEnumId);
                final it.unimi.dsi.fastutil.objects.ObjectSet<it.unimi.dsi.fastutil.ints.Int2IntMap.Entry> subEnumEntrySet = subEnum.getValues().int2IntEntrySet();
                for (final it.unimi.dsi.fastutil.ints.Int2IntMap.Entry e : subEnumEntrySet) {
                    COLLECTION_LOG_ITEMS.add(e.getIntValue());
                    if (!new Item(e.getIntValue()).isTradable()) {
                        UNTRADABLE_COLLECTION_LOG_ITEMS.add(e.getIntValue());
                    }
                }
            }
        }
    }

    private final Container container;

    public CollectionLog() {
        this.container = new Container(ContainerPolicy.ALWAYS_STACK, ContainerType.COLLECTION_LOG, Optional.empty());
    }

    @Subscribe
    public static final void onInitialization(final InitializationEvent event) {
        final com.zenyte.game.world.entity.player.Player player = event.getPlayer();
        final com.zenyte.game.world.entity.player.Player savedPlayer = event.getSavedPlayer();
        final com.zenyte.game.world.entity.player.collectionlog.CollectionLog otherCollectionLog = savedPlayer.getCollectionLog();
        if (otherCollectionLog == null || otherCollectionLog.container == null || otherCollectionLog.container.isEmpty()) {
            return;
        }
        player.getCollectionLog().container.setContainer(otherCollectionLog.container);
    }

    @Subscribe
    public static final void postInit(final PostInitializationEvent event) {
        final com.zenyte.game.world.entity.player.Player player = event.getPlayer();
        final it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap allItems = new Int2IntOpenHashMap(500);
        for (final it.unimi.dsi.fastutil.objects.ObjectCollection<com.zenyte.game.item.Item> itemCollection : Utils.concatenate(player.getInventory().getContainer().getItems().values(), player.getBank().getContainer().getItems().values(), player.getEquipment().getContainer().getItems().values(), player.getRetrievalService().getContainer().getItems().values())) {
            for (final com.zenyte.game.item.Item item : itemCollection) {
                if (!item.isTradable()) {
                    allItems.addTo(item.getId(), item.getAmount());
                }
            }
        }
        for (int itemId : UNTRADABLE_COLLECTION_LOG_ITEMS) {
            if (player.getCollectionLog().getContainer().contains(itemId, 1)) {
                continue;
            }
            if (allItems.containsKey(itemId)) {
                player.getCollectionLog().add(new Item(itemId, allItems.get(itemId)));
            }
        }
    }

    public void add(@NotNull final Item item) {
        final com.zenyte.game.item.Item unnotedItem = item.getDefinitions().isNoted() ? new Item(item.getDefinitions().getUnnotedOrDefault(), item.getAmount()) : item;
        if (!COLLECTION_LOG_ITEMS.contains(unnotedItem.getId())) {
            return;
        }
        container.add(unnotedItem);
    }

    public Container getContainer() {
        return this.container;
    }
}
