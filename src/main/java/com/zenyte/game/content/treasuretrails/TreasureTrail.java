package com.zenyte.game.content.treasuretrails;

import com.google.common.base.Preconditions;
import com.zenyte.game.content.treasuretrails.challenges.*;
import com.zenyte.game.content.treasuretrails.clues.*;
import com.zenyte.game.content.treasuretrails.clues.emote.ItemRequirement;
import com.zenyte.game.content.treasuretrails.npcs.*;
import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemId;
import com.zenyte.game.util.Colour;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.Position;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.npc.NPC;
import com.zenyte.game.world.entity.pathfinding.events.player.EntityEvent;
import com.zenyte.game.world.entity.pathfinding.strategy.EntityStrategy;
import com.zenyte.game.world.entity.player.Emote;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.container.impl.ContainerType;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.game.world.entity.player.dialogue.EmptyDialogue;
import com.zenyte.game.world.entity.player.dialogue.impl.NPCChat;
import com.zenyte.game.world.object.WorldObject;
import com.zenyte.utils.ProjectileUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TreasureTrail {

    public static final class Constants {
        private static final String MIMIC_STATUS = "Clue scrolls Mimic";
        private static final String CLUE_SCROLL_NAME = "Clue scroll name";
        private static final String CLUE_SCROLL_STAGE = "Clue scroll stage";
        private static final String CLUE_SCROLL_TOTAL_STEPS = "Clue scroll total steps";
        public static final String CLUE_SCROLL_CURRENT_STEPS = "Clue scroll current steps";
        private static final String SHERLOCK_STAGE = "Sherlock Stage";
        private static final String CHARLIE_STAGE = "Charlie Stage";
        private static final String GUARDIAN_NPC_LIST = "TT Guardian NPC";
        private static final int ANCIENT_WIZARD_BASE = 7307;
        private static final int CECILIA_NPC_ID = 3267;
        private static final int NPC_SEEK_DISTANCE = 10;
        private static final int MASTER_HOT_COLD_RADIUS = 5;
        private static final int BEGINNER_HOT_COLD_RADIUS = 4;
        private static final int FALADOR_PARK_BRIDGE_MIN_X = 2986;
        private static final int FALADOR_PARK_BRIDGE_MAX_X = 2995;
        private static final int FALADOR_PARK_BRIDGE_MIN_Y = 3379;
        private static final int FALADOR_PARK_BRIDGE_MAX_Y = 3389;
        private static final int SPAWN_TILE_SEEK_RADIUS = 10;
        private static final int SPAWN_TILE_NPC_SIZE = 2;
        private static final int LIGHT_BOX_COMPLETED_STAGE = 2;
        private static final int PUZZLE_BOX_COMPLETED_STAGE = 1;
    }

    public static final boolean isMimicEnabled(@NotNull final Player player) {
        return player.getNumericAttribute(Constants.MIMIC_STATUS).intValue() == 0;
    }

    public static final void setMimicStatus(@NotNull final Player player, final boolean enabled) {
        player.addAttribute(Constants.MIMIC_STATUS, enabled ? 0 : 1);
    }

    private static final Optional<ClueLevel> getLevel(@NotNull final Item item) {
        final com.zenyte.game.content.treasuretrails.ClueItem clueItem = ClueItem.getMap().get(item.getId());
        return Optional.ofNullable(clueItem == null ? null : clueItem.getLevel());
    }

    public static final void setClue(@NotNull final Item item, final Clue clue) {
        final com.zenyte.game.content.treasuretrails.ClueLevel level = Objects.requireNonNull(getLevel(item).orElseThrow(RuntimeException::new));
        final int range = Utils.random(1, 1);
        item.resetAttributes();
        item.setAttribute(Constants.CLUE_SCROLL_NAME, level == ClueLevel.MASTER && clue instanceof CrypticClue ? (((CrypticClue) clue).selectMasterCrypticClues()) : Collections.singletonList(clue.getEnumName()));
        item.setAttribute(Constants.CLUE_SCROLL_TOTAL_STEPS, range);
    }

    public static final Clue setRandomClue(@NotNull final Item item) {
        final com.zenyte.game.content.treasuretrails.ClueLevel level = Objects.requireNonNull(getLevel(item).orElseThrow(RuntimeException::new));
        final com.zenyte.game.content.treasuretrails.clues.Clue clue = TreasureTrailType.random(level);
        final int range = Utils.random(level.getStepsRange().getFirst(), level.getStepsRange().getLast());
        item.resetAttributes();
        item.setAttribute(Constants.CLUE_SCROLL_NAME, level == ClueLevel.MASTER && clue instanceof CrypticClue ? (((CrypticClue) clue).selectMasterCrypticClues()) : Collections.singletonList(clue.getEnumName()));
        item.setAttribute(Constants.CLUE_SCROLL_TOTAL_STEPS, range);
        return clue;
    }

    public static final List<CrypticClue> getCrypticClues(@NotNull final Item item) {
        final com.zenyte.game.content.treasuretrails.ClueLevel level = Objects.requireNonNull(getLevel(item).orElseThrow(RuntimeException::new));
        assert level == ClueLevel.MASTER;
        final java.lang.Object attr = item.getAttribute(Constants.CLUE_SCROLL_NAME);
        assert attr instanceof List;
        @SuppressWarnings("unchecked")
        final java.util.List<java.lang.String> list = (List<String>) attr;
        final it.unimi.dsi.fastutil.objects.ObjectArrayList<com.zenyte.game.content.treasuretrails.clues.CrypticClue> typedList = new ObjectArrayList<CrypticClue>();
        for (final java.lang.String element : list) {
            final com.zenyte.game.content.treasuretrails.clues.Clue typedClue = getClues().get(element);
            assert typedClue instanceof CrypticClue;
            typedList.add((CrypticClue) typedClue);
        }
        return typedList;
    }

    public static final int getCrypticCluesStage(@NotNull final Item item) {
        final com.zenyte.game.content.treasuretrails.ClueLevel level = Objects.requireNonNull(getLevel(item).orElseThrow(RuntimeException::new));
        assert level == ClueLevel.MASTER;
        return item.getNumericAttribute(Constants.CLUE_SCROLL_STAGE).intValue();
    }

    private static final boolean advance(@NotNull final Item item) {
        final int currentSteps = item.getNumericAttribute(Constants.CLUE_SCROLL_CURRENT_STEPS).intValue();
        final int totalSteps = item.getNumericAttribute(Constants.CLUE_SCROLL_TOTAL_STEPS).intValue();
        if (currentSteps >= (totalSteps - 1)) {
            return false;
        }
        final com.zenyte.game.content.treasuretrails.ClueLevel level = Objects.requireNonNull(getLevel(item).orElseThrow(RuntimeException::new));
        final com.zenyte.game.content.treasuretrails.clues.Clue clue = TreasureTrailType.random(level);
        item.resetAttributes();
        item.setAttribute(Constants.CLUE_SCROLL_CURRENT_STEPS, currentSteps + 1);
        item.setAttribute(Constants.CLUE_SCROLL_NAME, level == ClueLevel.MASTER && clue instanceof CrypticClue ? (((CrypticClue) clue).selectMasterCrypticClues()) : Collections.singletonList(clue.getEnumName()));
        item.setAttribute(Constants.CLUE_SCROLL_TOTAL_STEPS, totalSteps);
        return true;
    }

    private static final <T> boolean findAndProgress(@NotNull final Player player, @NotNull final Class<T> clazz, @NotNull final Predicate<T> predicate, Consumer<ClueEntry> progressConsumer) {
        final java.util.Optional<com.zenyte.game.content.treasuretrails.ClueEntry> entry = findClueScroll(player, clazz, predicate);
        entry.ifPresent(progressConsumer);
        return entry.isPresent();
    }

    public static final void progressSherlockTask(@NotNull final Player player, @Nullable final Predicate<Player> predicate, @NotNull final SherlockTask task) {
        if (predicate != null && !predicate.test(player)) {
            return;
        }
        findClueScroll(player, SherlockRequest.class, clue -> clue == task.getChallenge()).ifPresent(clueEntry -> {
            if (clueEntry.getItem().getNumericAttribute(Constants.SHERLOCK_STAGE).intValue() == 1) {
                clueEntry.getItem().setAttribute(Constants.SHERLOCK_STAGE, 2);
                player.sendMessage(Colour.RED.wrap("Skill challenge completed."));
            }
        });
    }

    public static final Optional<ClueEntry> findSherlockClue(@NotNull final Player player) {
        return findClueScroll(player, SherlockRequest.class, __ -> true);
    }

    public static final void progressCharlieTask(@NotNull final Player player, @NotNull final CharlieTask task) {
        findClueScroll(player, CharlieRequest.class, clue -> clue.getTask() == task).ifPresent(clueEntry -> {
            if (clueEntry.getItem().getNumericAttribute(Constants.CHARLIE_STAGE).intValue() == 1) {
                clueEntry.getItem().setAttribute(Constants.CHARLIE_STAGE, 2);
                player.sendMessage("You\'ve completed Charlie\'s challenge. Perhaps you should show it to Charlie again.");
            }
        });
    }

    public static final Optional<ClueEntry> findFaloClue(@NotNull final Player player) {
        return findClueScroll(player, FaloTheBardChallenge.class, predicate -> true);
    }

    private static final <T> Optional<ClueEntry> findClueScroll(@NotNull final Player player, @NotNull final Class<T> clazz, @NotNull final Predicate<T> predicate) {
        final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
        for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<com.zenyte.game.item.Item> entry : inventory.getContainer().getItems().int2ObjectEntrySet()) {
            final com.zenyte.game.item.Item item = entry.getValue();
            if (!isClueScroll(item)) {
                continue;
            }
            final java.util.List<java.lang.String> list = getCluesList(item);
            if (list == null) {
                continue;
            }
            for (int i = 0; i < list.size(); i++) {
                final java.lang.String constant = list.get(i);
                final com.zenyte.game.content.treasuretrails.clues.Clue clueScroll = getClues().get(constant);
                final com.zenyte.game.content.treasuretrails.challenges.ClueChallenge challenge = clueScroll.getChallenge();
                if (challenge == null) continue;
                if (!clazz.isAssignableFrom(challenge.getClass())) {
                    continue;
                }
                if (!predicate.test(clazz.cast(challenge))) {
                    continue;
                }
                if (clueScroll.getClass().isAssignableFrom(CrypticClue.class) && clueScroll.level() == ClueLevel.MASTER) {
                    if ((item.getNumericAttribute(Constants.CLUE_SCROLL_STAGE).intValue() & (1 << i)) != 0) {
                        continue;
                    }
                }
                return Optional.of(new ClueEntry(entry.getIntKey(), item, clueScroll, challenge));
            }
        }
        return Optional.empty();
    }

    public static final void progress(@NotNull final Player player, @NotNull final ClueEntry entry, @NotNull final Optional<Dialogue> optionalDialogue) {
        final java.util.List<java.lang.String> list = getCluesList(entry.getItem());
        assert list != null;
        int index = -1;
        if (list.size() > 1) {
            index = list.indexOf(entry.getClueScroll().getEnumName());
            if (index != -1) {
                if ((entry.getItem().getNumericAttribute(Constants.CLUE_SCROLL_STAGE).intValue() & (1 << index)) != 0) {
                    return;
                }
                entry.getItem().setAttribute(Constants.CLUE_SCROLL_STAGE, entry.getItem().getNumericAttribute(Constants.CLUE_SCROLL_STAGE).intValue() | (1 << index));
            }
        }
        progress(player, entry.getItem(), entry.getSlot(), optionalDialogue, index);
        if (entry.getChallenge() instanceof HotColdChallenge) {
            player.sendMessage("The strange device cools as you find your treasure.");
        }
    }

    public static final void progress(@NotNull final Player player, @NotNull final Item item, final int slot, @NotNull final Optional<Dialogue> optionalDialogue) {
        progress(player, item, slot, optionalDialogue, -1);
    }

    public static final void progressTripleCryptic(@NotNull final Player player, @NotNull final Item item) {
        final java.util.Optional<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<com.zenyte.game.item.Item>> entry = player.getInventory().getContainer().getItems().int2ObjectEntrySet().stream().filter(e -> e.getValue().equals(item)).findFirst();
        if (!entry.isPresent()) {
            return;
        }
        progress(player, entry.get().getValue(), entry.get().getIntKey(), Optional.empty(), -1);
    }

    private static final void progress(@NotNull final Player player, @NotNull final Item item, final int slot, @NotNull final Optional<Dialogue> optionalDialogue, final int index) {
        if (index != -1) {
            final com.zenyte.game.world.entity.player.dialogue.Dialogue dialogue = optionalDialogue.orElseGet(() -> new EmptyDialogue(player));
            player.getDialogueManager().start(dialogue);
            if (item.getNumericAttribute(Constants.CLUE_SCROLL_STAGE).intValue() == (1 | 2 | 4)) {
                player.getInventory().deleteItem(slot, item);
            }
            if (dialogue.getOnBuild() == null) {
                final com.zenyte.game.item.Item tornPiece = new Item(19837 + index);
                tornPiece.setAttributes(item.getAttributes());
                player.getInventory().addOrDrop(tornPiece);
                dialogue.setOnBuild(() -> dialogue.item(tornPiece, "You\'ve found a torn clue scroll piece!"));
            }
            return;
        }
        final boolean advanceStep = TreasureTrail.advance(item);
        final com.zenyte.game.item.Item dialogueItem = advanceStep ? item : new Item(Objects.requireNonNull(ClueItem.getMap().get(item.getId())).getCasket());
        if (!advanceStep) {
            final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
            inventory.deleteItem(slot, item);
            inventory.addItem(dialogueItem);
        }
        final com.zenyte.game.world.entity.player.dialogue.Dialogue dialogue = optionalDialogue.orElseGet(() -> new EmptyDialogue(player));
        if (dialogue.getOnBuild() == null) {
            dialogue.setOnBuild(() -> dialogue.item(dialogueItem, advanceStep ? "You\'ve found another clue scroll!" : "You\'ve found a casket!"));
        }
        player.getDialogueManager().start(dialogue);
    }

    private static final boolean onFaladorParkBridge(@NotNull final Location location) {
        final int x = location.getX();
        final int y = location.getY();
        return x >= Constants.FALADOR_PARK_BRIDGE_MIN_X && x <= Constants.FALADOR_PARK_BRIDGE_MAX_X && y >= Constants.FALADOR_PARK_BRIDGE_MIN_Y && y <= Constants.FALADOR_PARK_BRIDGE_MAX_Y;
    }

    public static final void playSong(@NotNull final Player player, @NotNull final String songName) {
        if (!onFaladorParkBridge(player.getLocation())) {
            return;
        }
        findClueScroll(player, SongRequest.class, clue -> clue.getSong().equalsIgnoreCase(songName)).ifPresent(entry -> {
            final java.util.Optional<com.zenyte.game.world.entity.npc.NPC> optionalNPC = World.findNPC(Constants.CECILIA_NPC_ID, player.getLocation(), Constants.NPC_SEEK_DISTANCE);
            optionalNPC.ifPresent(npc -> player.setRouteEvent(new EntityEvent(player, new EntityStrategy(npc), true)));
            progress(player, entry, Optional.of(new NPCChat(player, Constants.CECILIA_NPC_ID, optionalNPC, "Thank you, I love that song!")));
        });
    }

    public static final boolean containsMusicClue(@NotNull final Player player) {
        return findClueScroll(player, SongRequest.class, __ -> true).isPresent();
    }

    public static final boolean dig(@NotNull final Player player) {
        final com.zenyte.game.content.treasuretrails.ClueEntry entry = findClueScroll(player, DigRequest.class, clue -> clue.getLocation().getTileDistance(player.getLocation()) <= 1).orElseGet(() -> findClueScroll(player, HotColdChallenge.class, clue -> Math.max(Math.abs(clue.getCenter().getX() - player.getX()), Math.abs(clue.getCenter().getY() - player.getY())) < Constants.MASTER_HOT_COLD_RADIUS).orElse(null));
        if (entry == null || (entry.getClueScroll() instanceof HotColdClue && entry.getClueScroll().level() == ClueLevel.BEGINNER && Math.max(Math.abs(((HotColdChallenge) entry.getChallenge()).getCenter().getX() - player.getX()), Math.abs(((HotColdChallenge) entry.getChallenge()).getCenter().getY() - player.getY())) >= Constants.BEGINNER_HOT_COLD_RADIUS)) {
            return false;
        }
        final org.apache.commons.lang3.mutable.MutableObject<com.zenyte.game.content.treasuretrails.TreasureGuardianNPC> guardian = new MutableObject<TreasureGuardianNPC>();
        final com.zenyte.game.content.treasuretrails.challenges.ClueChallenge challenge = entry.getChallenge();
        if (challenge instanceof DigRequest) {
            guardian.setValue(((DigRequest) challenge).getGuardianNPC());
        } else if (entry.getClueScroll().level() == ClueLevel.MASTER) {
            guardian.setValue(TreasureGuardianNPC.ANCIENT_WIZARDS);//Will use either brassican mage or ancient wizard based on multi settings, regardless of which I pass.
        }
        if (interruptedByGuardians(player, guardian.getValue())) {
            return true;
        }
        progress(player, entry, Optional.empty());
        return true;
    }

    private static final boolean interruptedByGuardians(@NotNull final Player player, @Nullable final TreasureGuardianNPC type) {
        if (type == null) {
            return false;
        }
        final java.lang.Object guardianAttr = player.getTemporaryAttributes().get(Constants.GUARDIAN_NPC_LIST);
        if (guardianAttr instanceof Collection) {
            player.sendMessage("You\'ll have to kill the guardian to dig without interruptions.");
            return true;
        }
        if (guardianAttr == null) {
            final com.zenyte.game.world.entity.Location tile = getSpawnTile(player.getLocation());
            final java.util.ArrayList<com.zenyte.game.content.treasuretrails.npcs.TreasureGuardian> list = new ArrayList<TreasureGuardian>();
            if (type == TreasureGuardianNPC.GUARDIAN) {
                list.add(Utils.random(1) == 0 ? new BandosianGuard(player, tile) : new ArmadylianGuard(player, tile));
            } else if (type == TreasureGuardianNPC.ZAMORAK_WIZARD) {
                list.add(new ZamorakWizard(player, tile));
            } else if (type == TreasureGuardianNPC.SARADOMIN_WIZARD) {
                list.add(new SaradominWizard(player, tile));
            } else if (type == TreasureGuardianNPC.ANCIENT_WIZARDS || type == TreasureGuardianNPC.BRASSICAN_MAGE) {
                if (player.isMultiArea()) {
                    final it.unimi.dsi.fastutil.ints.IntOpenHashSet usedHashSet = new IntOpenHashSet();
                    for (int a = 0; a < 3; a++) {
                        final com.zenyte.game.world.entity.Location t = a == 0 ? tile : getSpawnTile(player.getLocation(), position -> !usedHashSet.contains(position.getPosition().getPositionHash()));
                        list.add(new AncientWizard(player, t, Constants.ANCIENT_WIZARD_BASE + a));
                        usedHashSet.add(t.getPositionHash());
                    }
                } else {
                    list.add(new BrassicanMage(player, tile));
                }
            }
            player.getTemporaryAttributes().put(Constants.GUARDIAN_NPC_LIST, list);
            boolean force = false;
            for (final com.zenyte.game.content.treasuretrails.npcs.TreasureGuardian guard : list) {
                guard.spawn();
                if (!force) {
                    guard.getCombat().forceTarget(player);
                    force = true;
                } else {
                    guard.getCombat().setTarget(player);
                }
            }
            return true;
        }
        player.getTemporaryAttributes().remove(Constants.GUARDIAN_NPC_LIST);
        return false;
    }

    public static final Optional<HotColdResult> getHotColdResult(@NotNull final Player player, final boolean beginner) {
        final java.util.OptionalInt slot = TreasureTrail.findHotColdClue(player, beginner);
        if (!slot.isPresent()) {
            return Optional.empty();
        }
        final com.zenyte.game.item.Item clueItem = Objects.requireNonNull(player.getInventory().getItem(slot.getAsInt()));
        final java.util.List<java.lang.String> list = getCluesList(clueItem);
        if (list == null) {
            throw new IllegalStateException();
        }
        for (final java.lang.String constantName : list) {
            final com.zenyte.game.content.treasuretrails.clues.Clue clueScroll = TreasureTrail.getClues().get(constantName);
            final com.zenyte.game.content.treasuretrails.challenges.ClueChallenge challenge = clueScroll.getChallenge();
            Preconditions.checkArgument(challenge instanceof HotColdChallenge);
            final com.zenyte.game.content.treasuretrails.challenges.HotColdChallenge hc = (HotColdChallenge) challenge;
            final com.zenyte.game.world.entity.Location center = hc.getCenter();
            final com.zenyte.game.world.entity.Location tile = player.getLocation();
            final int distance = Math.max(Math.abs(center.getX() - tile.getX()), Math.abs(center.getY() - tile.getY()));
            final java.util.Optional<com.zenyte.game.content.treasuretrails.DeviceTemperature> temperature = DeviceTemperature.get(clueScroll.level() == ClueLevel.BEGINNER, distance);
            return Optional.of(new HotColdResult(slot.getAsInt(), clueItem, hc, distance, temperature.orElse(null)));
        }
        throw new IllegalStateException();
    }

    private static final OptionalInt findHotColdClue(@NotNull final Player player, final boolean beginner) {
        return findClueScroll(player, HotColdChallenge.class, clue -> true).map(clueEntry -> {
            if (clueEntry.getClueScroll().level() != (beginner ? ClueLevel.BEGINNER : ClueLevel.MASTER)) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(clueEntry.getSlot());
        }).orElseGet(OptionalInt::empty);
    }

    @NotNull
    private static final Location getSpawnTile(@NotNull final Location tile) {
        return getSpawnTile(tile, null);
    }

    @NotNull
    private static final Location getSpawnTile(@NotNull final Location tile, @Nullable final Predicate<Position> predicate) {
        return Utils.findEmptySquare(tile, Constants.SPAWN_TILE_SEEK_RADIUS, Constants.SPAWN_TILE_NPC_SIZE, Optional.of(t -> !ProjectileUtils.isProjectileClipped(null, null, tile, t, true) && !tile.matches(t) && (predicate == null || predicate.test(t)))).orElse(tile);
    }

    /**
     * Validates whether or not the item passed in the arguments is one of the five clue scrolls.
     * @param item the item validated.
     * @return whether or not the item is a clue scroll item.
     */
    private static final boolean isClueScroll(@Nullable final Item item) {
        if (item == null) {
            return false;
        }
        final com.zenyte.game.content.treasuretrails.ClueItem clueItem = ClueItem.getMap().get(item.getId());
        return clueItem != null && clueItem.getClue() == item.getId();
    }

    @NotNull
    private static final Item[] buildEquipmentArray(@NotNull final Player player) {
        final com.zenyte.game.item.Item[] equipment = new Item[ContainerType.EQUIPMENT.getSize()];
        final it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap<com.zenyte.game.item.Item> entries = player.getEquipment().getContainer().getItems();
        for (int a = ContainerType.EQUIPMENT.getSize() - 1; a >= 0; a--) {
            final com.zenyte.game.item.Item entry = entries.get(a);
            equipment[a] = entry == null ? new Item(-1) : entry;
        }
        return equipment;
    }

    public static final void emoteChallenge(@NotNull final Player player, @NotNull final Emote emote) {
        final org.apache.commons.lang3.mutable.MutableObject<com.zenyte.game.item.Item[]> mutableEquipment = new MutableObject<Item[]>();
        findClueScroll(player, EmoteRequest.class, clue -> {
            if (!clue.getEmotes().contains(emote) || !clue.getPolygon().contains(player.getLocation())) {
                return false;
            }
            Item[] equipment = mutableEquipment.getValue();
            if (equipment == null) {
                mutableEquipment.setValue(equipment = buildEquipmentArray(player));
            }
            for (final com.zenyte.game.content.treasuretrails.clues.emote.ItemRequirement requirement : clue.getRequirements()) {
                if (!requirement.fulfilledBy(equipment)) {
                    return false;
                }
            }
            final java.util.List<com.zenyte.game.world.entity.player.Emote> emotes = clue.getEmotes();
            if (emotes.size() == 2 && emotes.get(1) == emote) {
                final java.lang.Object uri = player.getTemporaryAttributes().get("spawned uri npc");
                if (uri instanceof UriNPC) {
                    ((UriNPC) uri).setTalkative(true);
                    return true;
                }
                return false;
            }
            final boolean invokeUri = emotes.get(0) == emote;
            if (invokeUri) {
                if (clue.isAgents()) {
                    final java.lang.Object existingAgent = player.getTemporaryAttributes().get("double agent clue");
                    final boolean containsAgent = existingAgent instanceof DoubleAgent;
                    final boolean defeatedAgent = containsAgent && (((DoubleAgent) existingAgent).isDead() || ((DoubleAgent) existingAgent).isFinished());
                    if (!containsAgent) {
                        final com.zenyte.game.content.treasuretrails.npcs.DoubleAgent agent = new DoubleAgent(clue.getLevel(), getSpawnTile(player.getLocation()), player, clue);
                        agent.spawn();
                        player.getTemporaryAttributes().put("double agent clue", agent);
                    }
                    if (!defeatedAgent) {
                        return true;
                    }
                }
                if (!UriNPC.findUri(player).isPresent()) {
                    UriNPC.spawnUri(player, clue);
                }
            }
            return true;
        });
    }

    /* TODO: Handle double agents and double-emotes. */
    public static final boolean speakWithUri(@NotNull final Player player, @NotNull final UriNPC npc) {
        final org.apache.commons.lang3.mutable.MutableObject<com.zenyte.game.item.Item[]> mutableEquipment = new MutableObject<Item[]>();
        final java.util.Optional<com.zenyte.game.content.treasuretrails.ClueEntry> entry = findClueScroll(player, EmoteRequest.class, clue -> {
            if (!npc.isTalkative() || !clue.getPolygon().contains(player.getLocation()) || !npc.getClue().equals(clue)) {
                return false;
            }
            Item[] equipment = mutableEquipment.getValue();
            if (equipment == null) {
                mutableEquipment.setValue(equipment = buildEquipmentArray(player));
            }
            for (final ItemRequirement requirement : clue.getRequirements()) {
                if (!requirement.fulfilledBy(equipment)) {
                    return false;
                }
            }
            return true;
        });
        entry.ifPresent(clueEntry -> player.getDialogueManager().start(new Dialogue(player, npc) {
            @Override
            public void buildDialogue() {
                npc(UriNPC.randomQuote());
                player("What?").executeAction(() -> {
                    npc.finish();
                    progress(player, clueEntry, Optional.empty());
                });
            }
        }));
        return entry.isPresent();
    }

    public static final boolean searchKeyObject(@NotNull final Player player, @NotNull final WorldObject object, @NotNull final String option) {
        return findAndProgress(player, KeyRequest.class, clue -> Utils.findMatching(clue.getValidObjects(), s -> s.getTile().matches(object) && option.equalsIgnoreCase(s.getOption())) != null, entry -> progress(player, entry, Optional.empty()));
    }

    public static final boolean search(@NotNull final Player player, @NotNull final WorldObject object, @NotNull final String option) {
        return findAndProgress(player, SearchRequest.class, clue -> Utils.findMatching(clue.getValidObjects(), s -> s.getTile().matches(object) && option.equalsIgnoreCase(s.getOption())) != null, entry -> progress(player, entry, Optional.empty()));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static final boolean searchWithkey(@NotNull final Player player, @NotNull final WorldObject object, @NotNull final String option) {
        return searchKeyObject(player, object, option);
    }

    private static final boolean talkChallenge(@NotNull final Player player, @NotNull final NPC npc) {
        final int transmogrifiedId = player.getTransmogrifiedId(npc.getDefinitions(), npc.getId());
        final java.util.Optional<com.zenyte.game.content.treasuretrails.ClueEntry> clueEntry = findClueScroll(player, TalkChallengeRequest.class, clue -> ArrayUtils.contains(clue.getValidNPCs(), transmogrifiedId));
        clueEntry.ifPresent(clue -> {
            Preconditions.checkArgument(clue.getChallenge() instanceof TalkChallengeRequest);
            final com.zenyte.game.content.treasuretrails.challenges.TalkChallengeRequest talkChallenge = (TalkChallengeRequest) clue.getChallenge();
            player.getDialogueManager().start(new Dialogue(player, npc) {
                @Override
                public void buildDialogue() {
                    npc("Well done, you\'ve found me!<br>Answer me this to continue your trailing:");
                    npc(talkChallenge.getChallengeScroll().getQuestion()).executeAction(() -> {
                        player.getDialogueManager().finish();
                        player.sendInputInt("Answer:", value -> {
                            //Prevent progressing any further if the inventory positioning of the clue scroll has changed.
                            if (player.getInventory().getItem(clue.getSlot()) != clue.getItem()) {
                                return;
                            }
                            if (value != talkChallenge.getChallengeScroll().getAnswer() && !(value == 20 && talkChallenge.getChallengeScroll() == ChallengeScroll.FATHER_AERECK)) {
                                player.getDialogueManager().start(new NPCChat(player, transmogrifiedId, "That\'s incorrect!"));
                                return;
                            }
                            progress(player, clue, Optional.of(new NPCChat(player, transmogrifiedId, "That\'s correct!")));
                        });
                    });
                }
            });
        });
        return clueEntry.isPresent();
    }

    private static final boolean talkPuzzleBox(@NotNull final Player player, @NotNull final NPC npc) {
        final int transmogrifiedId = player.getTransmogrifiedId(npc.getDefinitions(), npc.getId());
        final java.util.Optional<com.zenyte.game.content.treasuretrails.ClueEntry> clueEntry = findClueScroll(player, PuzzleRequest.class, clue -> ArrayUtils.contains(clue.getValidNPCs(), transmogrifiedId));
        clueEntry.ifPresent(clue -> {
            final java.util.OptionalInt puzzleboxSlot = findPuzzleBox(player, clue.getClueScroll().getEnumName());
            if (puzzleboxSlot.isPresent()) {
                final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
                final com.zenyte.game.item.Item puzzleboxItem = inventory.getItem(puzzleboxSlot.getAsInt());
                if (puzzleboxItem.getCharges() != Constants.PUZZLE_BOX_COMPLETED_STAGE) {
                    player.getDialogueManager().start(new NPCChat(player, transmogrifiedId, "You need to solve the puzzle box!"));
                    return;
                }
                inventory.deleteItem(puzzleboxSlot.getAsInt(), puzzleboxItem);
                progress(player, clue, Optional.of(new Dialogue(player, npc) {
                    @Override
                    public void buildDialogue() {
                        npc("You\'ve done it!");
                    }
                }));
                return;
            }
            player.getDialogueManager().start(new Dialogue(player, npc) {
                @Override
                public void buildDialogue() {
                    if (!player.getInventory().hasFreeSlots()) {
                        npc("Well done, you\'ve found me!<br>Talk to me again when you have enough space to claim the puzzle.");
                        return;
                    }
                    player.getPuzzleBox().reset();
                    final com.zenyte.game.item.Item puzzle = new Item(Puzzle.random(clue.getClueScroll().level()).getPuzzleBox());
                    puzzle.setAttribute(Constants.CLUE_SCROLL_NAME, Collections.singletonList(clue.getClueScroll().getEnumName()));
                    final int currentSteps = clue.getItem().getNumericAttribute(Constants.CLUE_SCROLL_CURRENT_STEPS).intValue();
                    final int totalSteps = clue.getItem().getNumericAttribute(Constants.CLUE_SCROLL_TOTAL_STEPS).intValue();
                    puzzle.setAttribute(Constants.CLUE_SCROLL_CURRENT_STEPS, currentSteps);
                    puzzle.setAttribute(Constants.CLUE_SCROLL_TOTAL_STEPS, totalSteps);
                    npc("Well done, you\'ve found me!<br>Solve the puzzle to continue trailing:").executeAction(() -> player.getInventory().addOrDrop(puzzle));
                    item(puzzle, npc.getName(player) + " hands you a puzzle.");
                }
            });
        });
        return clueEntry.isPresent();
    }

    private static final boolean talkLightBox(@NotNull final Player player, @NotNull final NPC npc) {
        final int transmogrifiedId = player.getTransmogrifiedId(npc.getDefinitions(), npc.getId());
        final java.util.Optional<com.zenyte.game.content.treasuretrails.ClueEntry> clueEntry = findClueScroll(player, LightBoxRequest.class, clue -> ArrayUtils.contains(clue.getValidNPCs(), transmogrifiedId));
        clueEntry.ifPresent(clue -> {
            final java.util.OptionalInt lightboxSlot = findLightBox(player, clue.getClueScroll().getEnumName());
            if (lightboxSlot.isPresent()) {
                final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
                final com.zenyte.game.item.Item lightboxItem = inventory.getItem(lightboxSlot.getAsInt());
                if (lightboxItem.getCharges() != Constants.LIGHT_BOX_COMPLETED_STAGE) {
                    player.getDialogueManager().start(new NPCChat(player, transmogrifiedId, "You need to solve the light box!"));
                    return;
                }
                inventory.deleteItem(lightboxSlot.getAsInt(), lightboxItem);
                progress(player, clue, Optional.of(new Dialogue(player, npc) {
                    @Override
                    public void buildDialogue() {
                        npc("You\'ve done it!");
                    }
                }));
                return;
            }
            player.getDialogueManager().start(new Dialogue(player, npc) {
                @Override
                public void buildDialogue() {
                    if (!player.getInventory().hasFreeSlots()) {
                        npc("Well done, you\'ve found me!<br>Talk to me again when you have enough space to claim the light box.");
                        return;
                    }
                    final com.zenyte.game.item.Item puzzle = new Item(ItemId.LIGHT_BOX);
                    puzzle.setAttribute(Constants.CLUE_SCROLL_NAME, Collections.singletonList(clue.getClueScroll().getEnumName()));
                    npc("Well done, you\'ve found me!<br>Solve the puzzle to continue trailing:").executeAction(() -> player.getInventory().addOrDrop(puzzle));
                    item(puzzle, npc.getName(player) + " hands you a light box puzzle.");
                }
            });
        });
        return clueEntry.isPresent();
    }

    private static final OptionalInt findPuzzleBox(@NotNull final Player player, @NotNull final String clueName) {
        final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
        for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<com.zenyte.game.item.Item> entry : inventory.getContainer().getItems().int2ObjectEntrySet()) {
            final com.zenyte.game.item.Item item = entry.getValue();
            if (item == null || !ArrayUtils.contains(Puzzle.getPuzzleBoxArray(), item.getId())) {
                continue;
            }
            final java.util.List<java.lang.String> list = getCluesList(item);
            if (list == null) {
                continue;
            }
            for (final java.lang.String constantName : list) {
                if (clueName.equalsIgnoreCase(constantName)) {
                    return OptionalInt.of(entry.getIntKey());
                }
            }
        }
        return OptionalInt.empty();
    }

    private static final OptionalInt findLightBox(@NotNull final Player player, @NotNull final String clueName) {
        final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
        for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<com.zenyte.game.item.Item> entry : inventory.getContainer().getItems().int2ObjectEntrySet()) {
            final com.zenyte.game.item.Item item = entry.getValue();
            if (item == null || item.getId() != ItemId.LIGHT_BOX) {
                continue;
            }
            final java.util.List<java.lang.String> list = getCluesList(item);
            if (list == null) {
                continue;
            }
            for (final java.lang.String constantName : list) {
                if (clueName.equalsIgnoreCase(constantName)) {
                    return OptionalInt.of(entry.getIntKey());
                }
            }
        }
        return OptionalInt.empty();
    }

    public static final boolean withoutHotColdClue(@NotNull final Player player, final boolean beginner) {
        final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
        for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<com.zenyte.game.item.Item> entry : inventory.getContainer().getItems().int2ObjectEntrySet()) {
            final com.zenyte.game.item.Item item = entry.getValue();
            if (item == null) {
                continue;
            }
            final java.util.List<java.lang.String> list = getCluesList(item);
            if (list == null) {
                continue;
            }
            for (final java.lang.String constantName : list) {
                final com.zenyte.game.content.treasuretrails.clues.Clue constant = getClues().get(constantName);
                if (constant instanceof HotColdClue && (constant.level() == (beginner ? ClueLevel.BEGINNER : ClueLevel.MASTER))) {
                    return false;
                }
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static final List<String> getCluesList(@NotNull final Item item) {
        final java.lang.Object attr = item.getAttribute(Constants.CLUE_SCROLL_NAME);
        if (!(attr instanceof List)) return null;
        return (List<String>) attr;
    }

    public static final OptionalInt getKey(@NotNull final Player player, @NotNull final NPC npc) {
        final java.util.Optional<com.zenyte.game.content.treasuretrails.ClueEntry> clue = findClueScroll(player, KeyRequest.class, predicate -> ArrayUtils.contains(predicate.getValidNPCs(), player.getTransmogrifiedId(npc.getDefinitions(), npc.getId())));
        return clue.map(clueEntry -> OptionalInt.of(((KeyRequest) clueEntry.getChallenge()).getKeyId())).orElseGet(OptionalInt::empty);
    }

    public static final boolean talk(@NotNull final Player player, @NotNull final NPC npc) {
        if (talkChallenge(player, npc) || talkLightBox(player, npc) || talkPuzzleBox(player, npc)) {
            return true;
        }
        final int transmogrifiedId = player.getTransmogrifiedId(npc.getDefinitions(), npc.getId());
        final java.util.Optional<com.zenyte.game.content.treasuretrails.ClueEntry> clueEntry = findClueScroll(player, TalkRequest.class, clue -> ArrayUtils.contains(clue.getValidNPCs(), transmogrifiedId) && (clue.getPredicate() == null || clue.getPredicate().test(player)));
        clueEntry.ifPresent(entry -> progress(player, entry, Optional.of(new NPCChat(player, transmogrifiedId, "Well done, you found me!"))));
        return clueEntry.isPresent();
    }

    public static final void kill(@NotNull final Player player, @NotNull final NPC npc) {
        findAndProgress(player, KillRequest.class, clue -> ArrayUtils.contains(clue.getValidNPCs(), player.getTransmogrifiedId(npc.getDefinitions(), npc.getId())), entry -> progress(player, entry, Optional.empty()));
    }

    public static final void continueSherlockTask(@NotNull final Player player, @NotNull final Item item, final int slot) {
        final java.util.List<java.lang.String> list = getCluesList(item);
        if (list == null) {
            return;
        }
        for (final java.lang.String constantName : list) {
            final com.zenyte.game.content.treasuretrails.clues.Clue constant = getClues().get(constantName);
            if (constant instanceof SherlockTask) {
                progress(player, item, slot, Optional.empty());
            }
        }
    }

    public static final void continueCharlieTask(@NotNull final Player player, @NotNull final Item item, final int slot) {
        if (player.getInventory().getItem(slot) != item) {
            return;
        }
        final java.util.List<java.lang.String> list = getCluesList(item);
        if (list == null) {
            return;
        }
        for (final java.lang.String constantName : list) {
            final com.zenyte.game.content.treasuretrails.clues.Clue constant = getClues().get(constantName);
            if (constant instanceof CharlieTask) {
                progress(player, item, slot, Optional.empty());
            }
        }
    }

    public static final Object2ObjectMap<String, Clue> getClues() {
        return TreasureTrailType.getNamedClues();
    }
}
