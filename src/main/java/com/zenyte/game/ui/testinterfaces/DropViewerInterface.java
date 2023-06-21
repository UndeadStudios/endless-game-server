package com.zenyte.game.ui.testinterfaces;

import com.zenyte.game.constants.GameInterface;
import com.zenyte.game.item.ItemId;
import com.zenyte.game.ui.Interface;
import com.zenyte.game.util.AccessMask;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.entity.npc.drop.matrix.DropProcessorLoader;
import com.zenyte.game.world.entity.npc.drop.matrix.ItemDrop;
import com.zenyte.game.world.entity.npc.drop.matrix.NPCDrops;
import com.zenyte.game.world.entity.npc.drop.viewerentry.DropViewerEntry;
import com.zenyte.game.world.entity.npc.drop.viewerentry.ItemDropViewerEntry;
import com.zenyte.game.world.entity.npc.drop.viewerentry.NPCDropViewerEntry;
import com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.var.VarCollection;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mgi.types.Definitions;
import mgi.types.config.items.ItemDefinitions;
import mgi.types.config.npcs.NPCDefinitions;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingDouble;

/**
 * @author Tommeh | 16-4-2019 | 14:19
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class DropViewerInterface extends Interface {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DropViewerInterface.class);

    @Override
    protected void attach() {
        put(11, "Select NPC");
        put(13, "Select Item");
        put(19, "View Result");
        put(31, "Rarity Display");
        put(10, "Search button");//TODO for tom: Make it so clicking anywhere in the search box opens this.
    }

    @Override
    public void open(Player player) {
        reset(player);
        player.getInterfaceHandler().sendInterface(this);
    }

    @Override
    public void close(final Player player, final Optional<GameInterface> replacement) {
        player.getPacketDispatcher().sendClientScript(2158);
    }

    @Override
    protected void build() {
        bind("Select NPC", player -> {
            player.addTemporaryAttribute("drop_viewer_search_type", 0);
            final java.lang.Object attr = player.getTemporaryAttributes().get("drop_viewer_input");
            if (!(attr instanceof String)) {
                return;
            }
            final java.lang.String input = (String) attr;
            search(player, input);
        });
        bind("Select Item", player -> {
            player.addTemporaryAttribute("drop_viewer_search_type", 1);
            final java.lang.Object attr = player.getTemporaryAttributes().get("drop_viewer_input");
            if (!(attr instanceof String)) {
                return;
            }
            final java.lang.String input = (String) attr;
            search(player, input);
        });
        bind("View Result", (player, slotId, itemId, option) -> {
            final java.lang.Object attr = player.getTemporaryAttributes().get("drop_viewer_results");
            if (!(attr instanceof List)) {
                return;
            }
            final java.util.List<mgi.types.Definitions> results = (List<Definitions>) attr;
            final mgi.types.Definitions result = results.get(slotId);
            if (result == null) {
                return;
            }
            final java.util.List<com.zenyte.game.world.entity.npc.drop.viewerentry.DropViewerEntry> entries = getEntries(player, result);
            populateRows(player, true, result, entries);
            player.getPacketDispatcher().sendClientScript(2239);
        });
        bind("Rarity Display", player -> {
            player.toggleBooleanAttribute("drop_viewer_fractions");
            VarCollection.DROP_VIEWER_FRACTIONS.update(player);
            final java.lang.Object rowsAttr = player.getTemporaryAttributes().get("drop_viewer_rows");
            if (!(rowsAttr instanceof List)) {
                return;
            }
            final java.util.List<com.zenyte.game.world.entity.npc.drop.viewerentry.DropViewerEntry> rows = (List<DropViewerEntry>) rowsAttr;
            final java.lang.Object resultAttr = player.getTemporaryAttributes().get("drop_viewer_search_result");
            if (!(resultAttr instanceof Definitions)) {
                return;
            }
            final mgi.types.Definitions searchResult = (Definitions) resultAttr;
            populateRows(player, false, searchResult, rows);
        });
    }

    private static final Int2IntMap transformedIds = new Int2IntOpenHashMap();

    static {
        //Re-point all the vorkath versions.
        transformedIds.put(8026, 8061);
        transformedIds.put(8058, 8061);
        transformedIds.put(8059, 8061);
        transformedIds.put(8060, 8061);
    }

    public static final void open(@NotNull final Player player, final int npcId) {
        final int id = transformedIds.getOrDefault(npcId, npcId);
        final mgi.types.config.npcs.NPCDefinitions result = NPCDefinitions.get(id);
        final java.util.List<com.zenyte.game.world.entity.npc.drop.viewerentry.DropViewerEntry> entries = getEntries(player, result);
        if (entries.isEmpty()) {
            player.sendMessage("No drops found for " + NPCDefinitions.getOrThrow(id).getName().toLowerCase() + ".");
            return;
        }
        GameInterface.DROP_VIEWER.open(player);
        populateRows(player, true, result, entries);
        player.getPacketDispatcher().sendClientScript(2239);
    }

    public static List<DropViewerEntry> getEntries(final Player player, final Definitions r) {
        final java.util.LinkedList<com.zenyte.game.world.entity.npc.drop.viewerentry.DropViewerEntry> list = new LinkedList<DropViewerEntry>();
        if (r instanceof NPCDefinitions) {
            final mgi.types.config.npcs.NPCDefinitions result = (NPCDefinitions) r;
            final int id = result.getId();
            final com.zenyte.game.world.entity.npc.drop.matrix.NPCDrops.DropTable table = NPCDrops.getTable(id);
            final java.util.List<com.zenyte.game.world.entity.npc.drop.matrix.DropProcessor> processors = DropProcessorLoader.get(id);
            final java.util.HashMap<java.lang.Integer, java.util.List<com.zenyte.game.world.entity.npc.drop.viewerentry.ItemDropViewerEntry>> rows = new HashMap<Integer, List<ItemDropViewerEntry>>();
            if (table != null) {
                for (final com.zenyte.game.world.entity.npc.drop.matrix.Drop drop : table.getDrops()) {
                    if (drop.getItemId() == ItemId.TOOLKIT) {
                        continue;
                    }
                    rows.computeIfAbsent(drop.getItemId(), l -> new LinkedList<>()).add(new ItemDropViewerEntry(drop.getItemId(), drop.getMinAmount(), drop.getMaxAmount(), drop.getRate() == 100000 ? 100 : drop.getRate() / (float) table.getWeight() * 100, ""));
                }
            }
            if (processors != null) {
                for (final com.zenyte.game.world.entity.npc.drop.matrix.DropProcessor processor : processors) {
                    for (final com.zenyte.game.world.entity.npc.drop.matrix.DropProcessor.DisplayedDrop drop : processor.getBasicDrops()) {
                        if (drop.getPredicate() != null && !drop.getPredicate().test(player, id)) {
                            continue;
                        }
                        double rate = drop.getRate(player, id);
                        rows.computeIfAbsent(drop.getId(), l -> new LinkedList<>()).add(new ItemDropViewerEntry(drop.getId(), drop.getMinAmount(), drop.getMaxAmount(), 1.0 / rate * 100, ""));
                    }
                    for (final it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<com.zenyte.game.world.entity.npc.drop.matrix.DropProcessor.PredicatedDrop> entry : processor.getInfoMap().long2ObjectEntrySet()) {
                        try {
                            final long packed = entry.getLongKey();
                            final int item = (int) (packed);
                            final int npc = (int) (packed >> 32);
                            if (id != npc) {
                                continue;
                            }
                            final java.util.List<com.zenyte.game.world.entity.npc.drop.viewerentry.ItemDropViewerEntry> tableDropList = rows.get(item);
                            final com.zenyte.game.world.entity.npc.drop.matrix.DropProcessor.PredicatedDrop drop = entry.getValue();
                            if (tableDropList == null) {
                                continue;
                            }
                            rows.remove(item);
                            for (final com.zenyte.game.world.entity.npc.drop.viewerentry.ItemDropViewerEntry tableDrop : tableDropList) {
                                rows.computeIfAbsent(item, l -> new LinkedList<>()).add(new ItemDropViewerEntry(tableDrop.getItem(), tableDrop.getMinAmount(), tableDrop.getMaxAmount(), tableDrop.getRate(), drop.getInformation()));
                            }
                        } catch (Exception e) {
                            log.error(Strings.EMPTY, e);
                        }
                    }
                }
            }
            rows.forEach((key, value) -> list.addAll(value));
        } else {
            final java.util.HashMap<java.lang.Integer, it.unimi.dsi.fastutil.objects.Object2ObjectMap<java.lang.String, com.zenyte.game.world.entity.npc.drop.viewerentry.NPCDropViewerEntry>> rows = new HashMap<Integer, Object2ObjectMap<String, NPCDropViewerEntry>>();
            final java.util.LinkedList<mgi.types.config.items.ItemDefinitions> definitionsList = new LinkedList<ItemDefinitions>();
            definitionsList.add((ItemDefinitions) r);
            if (((ItemDefinitions) r).getNotedOrDefault() != ((ItemDefinitions) r).getId()) {
                definitionsList.add(ItemDefinitions.getOrThrow(((ItemDefinitions) r).getNotedOrDefault()));
            }
            for (final mgi.types.config.items.ItemDefinitions def : definitionsList) {
                final java.util.List<com.zenyte.game.world.entity.npc.drop.matrix.ItemDrop> drops = NPCDrops.getTableForItem(def.getId());
                if (drops != null) {
                    for (final com.zenyte.game.world.entity.npc.drop.matrix.ItemDrop d : drops) {
                        final com.zenyte.game.world.entity.npc.drop.matrix.NPCDrops.DisplayedNPCDrop drop = d.getDrop();
                        if (!NPCSpawnLoader.dropViewerNPCs.contains(d.getNpcId()) || (drop.getPredicate() != null && !drop.getPredicate().test(player, d.getNpcId()))) {
                            continue;
                        }
                        rows.computeIfAbsent(drop.getItemId(), l -> new Object2ObjectOpenHashMap<>()).put(NPCDefinitions.getOrThrow(d.getNpcId()).getName(), new NPCDropViewerEntry(drop.getItemId(), d.getNpcId(), drop.getMinAmount(), drop.getMaxAmount(), drop.getFunction().apply(player, d.getNpcId()), ""));
                    }
                }
            }
            rows.forEach((key, value) -> list.addAll(value.values()));
        }
        return list.stream().sorted(Collections.reverseOrder(comparingDouble(DropViewerEntry::getRate))).collect(Collectors.toList());
    }

    public static void populateRows(final Player player, final boolean completeRefresh, final Definitions searchResult, final List<DropViewerEntry> rows) {
        int offsetY = 0;
        int index = 0;
        player.addTemporaryAttribute("drop_viewer_rows", rows);
        player.addTemporaryAttribute("drop_viewer_search_result", searchResult);
        if (searchResult != null) {
            if (searchResult instanceof NPCDefinitions) {
                player.getPacketDispatcher().sendClientScript(10103, ((NPCDefinitions) searchResult).getName(), completeRefresh ? 1 : 0); //set layout title
            } else {
                player.getPacketDispatcher().sendClientScript(10103, ((ItemDefinitions) searchResult).getName(), completeRefresh ? 1 : 0); //set layout title
            }
        }
        for (final com.zenyte.game.world.entity.npc.drop.viewerentry.DropViewerEntry r : rows) {
            final java.lang.String formatted = Utils.format((int) Math.round(1 / r.getRate() * 100));
            String rarity;
            if (player.getBooleanAttribute("drop_viewer_fractions")) {
                rarity = "1 / " + (formatted.length() > 8 ? formatted.substring(0, 1) + " million" : formatted);
            } else {
                rarity = r.getRate() == 100 ? "Always" : String.format(r.getRate() < 0.001 ? "%.4f" : r.getRate() < 0.01 ? "%.3f" : "%.2f", r.getRate()) + "%";
            }
            if (r instanceof ItemDropViewerEntry) {
                final com.zenyte.game.world.entity.npc.drop.viewerentry.ItemDropViewerEntry row = (ItemDropViewerEntry) r;
                player.getPacketDispatcher().sendClientScript(10114, index, offsetY, row.getItem(), row.getInfo(), row.getMinAmount(), row.getMaxAmount(), rarity); //append item row
            } else {
                final com.zenyte.game.world.entity.npc.drop.viewerentry.NPCDropViewerEntry row = (NPCDropViewerEntry) r;
                final java.lang.String name = NPCDefinitions.get(row.getNpc()).getName();
                player.getPacketDispatcher().sendClientScript(10121, index, offsetY, row.getItemId(), name, row.getInfo(), row.getMinAmount(), row.getMaxAmount(), rarity); //append npc row
            }
            offsetY += r.isPredicated() ? 70 : 40;
            index++;
        }
        if (searchResult != null) {
            player.getPacketDispatcher().sendClientScript(10115, offsetY, completeRefresh ? 1 : 0); //rebuild scrolllayer/bar
        }
    }

    public static void search(final Player player, final String rawInput) {
        final java.lang.String input = rawInput.toLowerCase();
        player.addTemporaryAttribute("drop_viewer_input", input);
        final int type = player.getNumericTemporaryAttribute("drop_viewer_search_type").intValue();
        if (type == 0) {
            final it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<mgi.types.config.npcs.NPCDefinitions, java.util.Set<mgi.types.config.npcs.NPCDefinitions>> map = new Object2ObjectOpenHashMap<NPCDefinitions, Set<NPCDefinitions>>();
            final java.util.LinkedList<mgi.types.config.npcs.NPCDefinitions> results = new LinkedList<NPCDefinitions>();
            loop:
            for (final mgi.types.config.npcs.NPCDefinitions def : NPCDefinitions.definitions) {
                if (def == null || !def.containsOption("Attack") || NPCSpawnLoader.ignoredMonsters.contains(def.getId()) || def.getCombatLevel() == 0 || NPCDrops.getTable(def.getId()) == null && DropProcessorLoader.get(def.getId()) == null || !NPCSpawnLoader.dropViewerNPCs.contains(def.getId())) {
                    continue;
                }
                if (def.getLowercaseName().contains(input)) {
                    for (final mgi.types.config.npcs.NPCDefinitions result : results) {
                        if (result.getName().equalsIgnoreCase(def.getName()) && result.getCombatLevel() == def.getCombatLevel()) {
                            if (NPCDrops.equalsIgnoreRates(result.getId(), def.getId())) {
                                map.computeIfAbsent(result, r -> new ObjectOpenHashSet<>(Collections.singleton(result))).add(def);
                                continue loop;
                            }
                        }
                    }
                    map.computeIfAbsent(def, r -> new ObjectOpenHashSet<>(Collections.singleton(def)));
                    results.add(def);
                }
            }
            results.sort((o1, o2) -> {
                String x1 = o1.getName();
                String x2 = o2.getName();
                int sComp = x1.compareToIgnoreCase(x2);
                if (sComp != 0) {
                    return sComp;
                }
                return Integer.compare(o1.getCombatLevel(), o2.getCombatLevel());
            });
            int offsetY = 0;
            player.getPacketDispatcher().sendClientScript(10104, type); //set searchtype (item/npc)
            for (int index = 0; index < results.size(); index++) {
                final mgi.types.config.npcs.NPCDefinitions def = results.get(index);
                final java.lang.String name = def.getName();
                final java.lang.String cb = Utils.getPreciseLevelColour(player.getCombatLevel(), def.getCombatLevel()) + " (lvl-" + def.getCombatLevel() + ")";
                final java.util.ArrayList<java.lang.String> allAreas = new ArrayList<String>();
                map.get(def).forEach(definition -> {
                    final java.util.Set<java.lang.String> areas = NPCSpawnLoader.getFoundLocations(definition.getId());
                    if (areas == null) {
                        return;
                    }
                    areas.forEach(string -> {
                        if (!allAreas.contains(string)) {
                            allAreas.add(string);
                        }
                    });
                });
                //Sorting alphabetically w/ the exception of the 'Undefined' string which will always appear in the bottom.
                allAreas.sort((s1, s2) -> {
                    if (s1.equals("Undefined area")) {
                        return 1;
                    } else if (s2.equals("Undefined area")) {
                        return -1;
                    }
                    return s1.compareTo(s2);
                });
                final java.lang.StringBuilder builder = new StringBuilder();
                for (final java.lang.String area : allAreas) {
                    builder.append("- ").append(area).append("<br>");
                }
                final int width = Utils.getTextWidth(494, name + cb);
                final int height = width > 114 ? 22 : 13;
                player.getPacketDispatcher().sendClientScript(10105, index, offsetY, height, width, name, cb, builder.toString()); //append npc search result
                offsetY += height;
            }
            player.getPacketDispatcher().sendClientScript(2239); //disable keyboard input on chatbox
            player.getPacketDispatcher().sendComponentSettings(GameInterface.DROP_VIEWER.getId(), GameInterface.DROP_VIEWER.getPlugin().get().getComponent("View Result"), 0, results.size(), AccessMask.CLICK_OP1);
            player.getPacketDispatcher().sendClientScript(10108, offsetY); //rebuild scrollllayer/bar
            player.addTemporaryAttribute("drop_viewer_results", results);
            if (results.size() == 0) {
                player.getPacketDispatcher().sendClientScript(10109, "No results were found with your search."); //set response msg
            } else {
                final mgi.types.config.npcs.NPCDefinitions result = results.get(0);
                if (result == null) {
                    return;
                }
                final java.util.List<com.zenyte.game.world.entity.npc.drop.viewerentry.DropViewerEntry> entries = getEntries(player, result);
                populateRows(player, true, results.get(0), entries); //populate rows of first result
                player.getPacketDispatcher().sendClientScript(10107, 0); //highlight first result
            }
        } else {
            final java.util.LinkedList<mgi.types.config.items.ItemDefinitions> results = new LinkedList<ItemDefinitions>();
            for (final mgi.types.config.items.ItemDefinitions def : searchableItemDefinitions) {
                if (def.getLowercaseName().contains(input)) {
                    results.add(def);
                }
            }
            int offsetY = 0;
            player.getPacketDispatcher().sendClientScript(10104, type); //set search type (item/npc)
            results.removeIf(def -> {
                final java.util.List<com.zenyte.game.world.entity.npc.drop.viewerentry.DropViewerEntry> entries = getEntries(player, def);
                return entries == null || entries.isEmpty();
            });
            results.removeIf(result -> {
                if (result.isNoted()) {
                    final int unnotedId = result.getUnnotedOrDefault();
                    return results.contains(ItemDefinitions.getOrThrow(unnotedId));
                }
                return false;
            });
            results.sort(Comparator.comparing(ItemDefinitions::getName));
            for (int index = 0; index < results.size(); index++) {
                final mgi.types.config.items.ItemDefinitions def = results.get(index);
                final java.lang.String name = def.getName();
                final int width = Utils.getTextWidth(494, name);
                final int height = width > 114 ? 22 : 13;
                player.getPacketDispatcher().sendClientScript(10120, index, offsetY, height, width, name); //append item search result
                offsetY += height;
            }
            player.getPacketDispatcher().sendClientScript(2239); //disable keyboard input on chatbox
            player.getPacketDispatcher().sendComponentSettings(GameInterface.DROP_VIEWER.getId(), GameInterface.DROP_VIEWER.getPlugin().get().getComponent("View Result"), 0, results.size(), AccessMask.CLICK_OP1);
            player.getPacketDispatcher().sendClientScript(10108, offsetY); //rebuild scrolllayer/bar
            player.addTemporaryAttribute("drop_viewer_results", results);
            if (results.size() == 0) {
                player.getPacketDispatcher().sendClientScript(10109, "No results were found with your search."); //set response msg
            } else {
                final mgi.types.config.items.ItemDefinitions result = results.get(0);
                if (result == null) {
                    return;
                }
                final java.util.List<com.zenyte.game.world.entity.npc.drop.viewerentry.DropViewerEntry> entries = getEntries(player, result);
                populateRows(player, true, results.get(0), entries); //populate rows of first result
                player.getPacketDispatcher().sendClientScript(10107, 0); //highlight first result
            }
        }
    }

    private static final List<ItemDefinitions> searchableItemDefinitions = new LinkedList<>();

    public static final void populateDropViewerData() {
        for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<java.util.List<com.zenyte.game.world.entity.npc.drop.matrix.DropProcessor>> dropProcessorEntry : DropProcessorLoader.getProcessors().int2ObjectEntrySet()) {
            final int npcId = dropProcessorEntry.getIntKey();
            final java.util.List<com.zenyte.game.world.entity.npc.drop.matrix.DropProcessor> processorsList = dropProcessorEntry.getValue();
            for (final com.zenyte.game.world.entity.npc.drop.matrix.DropProcessor processor : processorsList) {
                for (final com.zenyte.game.world.entity.npc.drop.matrix.DropProcessor.DisplayedDrop displayedDrop : processor.getBasicDrops()) {
                    final com.zenyte.game.world.entity.npc.drop.matrix.NPCDrops.DisplayedDropTable table = NPCDrops.displayedDrops.computeIfAbsent(npcId, __ -> new NPCDrops.DisplayedDropTable(npcId, new ObjectArrayList<>()));
                    final com.zenyte.game.world.entity.npc.drop.matrix.NPCDrops.DisplayedNPCDrop drop = new NPCDrops.DisplayedNPCDrop(displayedDrop.getId(), displayedDrop.getMinAmount(), displayedDrop.getMaxAmount(), (player, id) -> 1.0 / displayedDrop.getRate(player, id) * 100.0);
                    table.getDrops().add(drop);
                    if (displayedDrop.getPredicate() != null) {
                        drop.setPredicate(displayedDrop.getPredicate());
                    }
                }
            }
        }
        for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<com.zenyte.game.world.entity.npc.drop.matrix.NPCDrops.DisplayedDropTable> table : NPCDrops.displayedDrops.int2ObjectEntrySet()) {
            for (final com.zenyte.game.world.entity.npc.drop.matrix.NPCDrops.DisplayedNPCDrop drop : table.getValue().getDrops()) {
                NPCDrops.dropsByItem.computeIfAbsent(drop.getItemId(), __ -> new ObjectArrayList<>()).add(new ItemDrop(table.getIntKey(), drop));
            }
        }
        for (final mgi.types.config.items.ItemDefinitions def : ItemDefinitions.definitions) {
            if (def == null || !NPCDrops.dropsByItem.containsKey(def.getId())) {
                continue;
            }
            searchableItemDefinitions.add(def);
        }
    }

    private void reset(final Player player) {
        player.getTemporaryAttributes().remove("drop_viewer_results");
        player.getTemporaryAttributes().remove("drop_viewer_search_type");
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.DROP_VIEWER;
    }
}
