package com.zenyte.game.content.chambersofxeric;

import com.google.common.eventbus.Subscribe;
import com.zenyte.game.constants.GameInterface;
import com.zenyte.game.content.chambersofxeric.greatolm.GreatOlm;
import com.zenyte.game.content.chambersofxeric.map.BossPattern;
import com.zenyte.game.content.chambersofxeric.map.RaidArea;
import com.zenyte.game.content.chambersofxeric.map.RaidMap;
import com.zenyte.game.content.chambersofxeric.map.RoomGeneration;
import com.zenyte.game.content.chambersofxeric.party.RaidParty;
import com.zenyte.game.content.chambersofxeric.rewards.RaidRewards;
import com.zenyte.game.content.chambersofxeric.room.ScavengerRoom;
import com.zenyte.game.content.chambersofxeric.score.Scoreboard;
import com.zenyte.game.content.chambersofxeric.skills.RaidFarming;
import com.zenyte.game.content.chambersofxeric.storageunit.SharedStorage;
import com.zenyte.game.content.clans.ClanManager;
import com.zenyte.game.content.clans.ClanRank;
import com.zenyte.game.content.consumables.drinks.GourdPotion;
import com.zenyte.game.item.Item;
import com.zenyte.game.tasks.TickTask;
import com.zenyte.game.tasks.WorldTask;
import com.zenyte.game.tasks.WorldTasksManager;
import com.zenyte.game.ui.GameTab;
import com.zenyte.game.ui.InterfacePosition;
import com.zenyte.game.util.Colour;
import com.zenyte.game.util.TimeUnit;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.Position;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.ImmutableLocation;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.SoundEffect;
import com.zenyte.game.world.entity.masks.Animation;
import com.zenyte.game.world.entity.npc.NPC;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.Skills;
import com.zenyte.game.world.object.WorldObject;
import com.zenyte.game.world.region.dynamicregion.OutOfSpaceException;
import com.zenyte.plugins.dialogue.PlainChat;
import com.zenyte.plugins.events.ClanLeaveEvent;
import com.zenyte.plugins.events.ItemDefinitionsLoadedEvent;
import com.zenyte.plugins.events.LoginEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mgi.types.config.items.ItemDefinitions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static com.zenyte.game.constants.GameInterface.RAID_OVERLAY;

/**
 * The engine of raids. Keeps everything running and cleans up, if necessary.
 *
 * @author Kris | 15. nov 2017 : 20:59.10
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public final class Raid {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Raid.class);
    public static final Location outsideTile = new ImmutableLocation(1246, 3558, 0);
    static final Animation crystalDisappearAnimation = new Animation(7506);
    private static final SoundEffect crystalExplosionSound = new SoundEffect(1021, 10, 0);
    private static final SoundEffect crystalShatterSound = new SoundEffect(3821, 10, 0);
    public static final Int2ObjectMap<Raid> existingRaidsMap = new Int2ObjectOpenHashMap<>();
    private static final SoundEffect enterSound = new SoundEffect(1952);
    private static final SoundEffect leaveSound = new SoundEffect(1956);
    private static int currentIndex;
    private final RaidParty party;
    private final Set<Player> players = new HashSet<>();
    private final Set<String> originalPlayers = new ObjectOpenHashSet<>();
    private final Object2IntMap<String> pointsMap = new Object2IntOpenHashMap<>();
    private final RaidFarming farming = new RaidFarming();
    private final List<String> deaths = new ObjectArrayList<>();
    private final Int2ObjectMap<Pair<String, Long>> levelCompletionMessages = new Int2ObjectOpenHashMap<>();
    private int combatLevel;
    private RaidMap map;
    private long startTime;
    private int index;
    private RaidRewards rewards;
    private SharedStorage storage;
    private int totalPoints;
    private int stage;
    private boolean constructingStorage;
    private boolean destroyed;
    private Location respawnTile;
    private int duration;
    private boolean recorded;
    private int farmingLevel = 1;
    private GreatOlm olm;
    private int potionsCap;
    private int psykkCap;
    private RaidStatus status;
    private boolean pendingDestroy;
    private boolean completed;
    private String onyxDropMessage;

    public Raid(final RaidParty party) {
        this.party = party;
    }

    @Subscribe
    public static final void onLogin(final LoginEvent event) {
        final com.zenyte.game.world.entity.player.Player player = event.getPlayer();
        final int index = player.getAttributes().containsKey("lastRaidIndex") ? player.getNumericAttribute("lastRaidIndex").intValue() : -1;
        player.getAttributes().remove("lastRaidIndex");
        player.getAttributes().remove("raidPotionSips");
        final com.zenyte.game.content.chambersofxeric.Raid raid = Raid.existingRaidsMap.get(index);
        //If raid is null or has already been completed(aka olm has fallen), we do not place the player back in the raid for reward purposes; They're not eligible for kc or rewards.
        if (raid == null || raid.isCompleted()) {
            return;
        }
        for (final java.lang.String p : raid.getOriginalPlayers()) {
            final java.util.Optional<com.zenyte.game.world.entity.player.Player> originalPlayer = World.getPlayer(p);
            if (originalPlayer.isPresent() && originalPlayer.get().getUsername().equals(player.getUsername())) {
                raid.addPlayer(player);
                return;
            }
        }
        player.sendMessage(Colour.RS_PINK.wrap("Your party could not be re-found."));
    }

    /**
     * Shatters the crystal(s) that block the exits to the next room when the room is deemed complete.
     *
     * @param crystal the crystal object.
     */
    public static final void shatterCrystal(@NotNull final WorldObject crystal) {
        World.sendObjectAnimation(crystal, crystalDisappearAnimation);
        World.sendSoundEffect(crystal, crystalExplosionSound);
        World.sendSoundEffect(crystal, crystalShatterSound);
        WorldTasksManager.schedule(() -> World.removeObject(crystal), 2);
    }

    public static final boolean isInternalItem(final int id) {
        //Dark journal
        if (id == 20899) {
            return false;
        }
        return id >= 20799 && id <= 20801 || id >= 20853 && id <= 20996 || id >= 21036 && id <= 21042;
    }

    @Subscribe
    public static final void onItemDefinitionsLoaded(final ItemDefinitionsLoadedEvent event) {
        for (int i = 20799; i <= 20801; i++) {
            setItemTradeable(i);
        }
        for (int i = 20853; i <= 20996; i++) {
            if (i == 20899) {
                continue;
            }
            setItemTradeable(i);
        }
        for (int i = 21036; i <= 21042; i++) {
            setItemTradeable(i);
        }
    }

    private static final void setItemTradeable(final int id) {
        ItemDefinitions.getOrThrow(id).setGrandExchange(true);
    }

    @Subscribe
    public static final void onClanLeave(final ClanLeaveEvent event) {
        final com.zenyte.game.world.entity.player.Player player = event.getPlayer();
        if (player == null || player.isNulled() || player.isFinished()) {
            return;
        }
        player.getRaid().ifPresent(raid -> WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (raid.isDestroyed() || player.isNulled()) {
                    stop();
                    return;
                }
                if (!player.isLocked()) {
                    player.reset();
                    raid.leaveRaid(player, false, false);
                    stop();
                }
            }
        }, 0, 0));
        MountQuidamortemArea.onLeave(player);
    }

    public static final void applyPotionEffect(@NotNull final Player player, @NotNull final GourdPotion potion, @NotNull final Item item) {
        player.getRaid().ifPresent(raid -> {
            if (raid.potionsCap <= 0) {
                return;
            }
            final java.lang.String finder = Objects.toString(item.getAttribute("brewer"));
            final com.zenyte.game.content.consumables.drinks.GourdPotion.GourdType type = potion.getType();
            final java.lang.String potionName = potion.name();
            final int potionPoints = potionName.contains("OVERLOAD") ? 400 : potionName.contains("XERIC") ? 200 : 100;
            final float strengthMultiplier = type.getMultiplier();
            final int points = (int) (potionPoints * strengthMultiplier * (!finder.equalsIgnoreCase(player.getUsername()) && !finder.equals("null") ? 1.5F : 1.0F) * (raid.potionsCap <= 5 ? 0.5F : 1.0F));
            raid.addPoints(player, points);
            raid.potionsCap--;
        });
    }

    public static final void applyPsykkEffect(@NotNull final Player player, @NotNull final Item item) {
        player.getRaid().ifPresent(raid -> {
            if (raid.psykkCap <= 0) {
                return;
            }
            raid.addPoints(player, Objects.toString(item.getAttribute("cooker")).equals(player.getUsername()) ? 60 : 100);
            raid.psykkCap--;
        });
    }

    public static final void incrementPoints(final Player player, final int amount) {
        player.getRaid().ifPresent(raid -> raid.addPoints(player, amount));
    }

    private void addRaid(final Raid raid) {
        existingRaidsMap.put(index = currentIndex++, raid);
    }

    public boolean isMetamorphicDustEligible() {
        if (duration == 0) {
            return false;
        }
        final int bracket = Scoreboard.getTargetTime(originalPlayers.size());
        return duration <= bracket;
    }

    @SuppressWarnings("unchecked cast")
    public <T extends RaidArea> void ifInRoom(@NotNull final Position position, @NotNull final Class<T> clazz, @NotNull Consumer<T> consumer) {
        final com.zenyte.game.content.chambersofxeric.map.RaidArea room = getRoom(position.getPosition());
        if (room == null || !Objects.equals(room.getClass(), clazz)) {
            return;
        }
        consumer.accept((T) room);
    }

    public int getCombatLevel() {
        if (combatLevel == 0) {
            throw new IllegalStateException("Combat level has not been set yet.");
        }
        return combatLevel;
    }

    private void setIncomplete() {
        if (this.status != null) {
            return;
        }
        this.status = this.startTime == 0 ? RaidStatus.NOT_STARTED : RaidStatus.INCOMPLETE;
        try {
            ChambersStatisticsLogger.record(this);
        } catch (Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    public void setComplete() {
        this.status = RaidStatus.COMPLETE;
        try {
            ChambersStatisticsLogger.record(this);
        } catch (Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    public void setStage(final int stage) {
        if (this.stage >= stage) {
            return;
        }
        this.stage = stage;
        if (stage < 2 || stage > 4) {
            return;
        }
        final long millis = Utils.currentTimeMillis() - getStartTime();
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        final long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        final java.lang.String duration = Colour.RED.wrap(hours == 0 ? String.format("%02d:%02d", minutes, seconds) : String.format("%02d:%02d:%02d", hours, minutes, seconds));
        final java.lang.String prefix = stage == 2 ? "Upper" : stage == 4 || !isChallengeMode() ? "Lower" : "Middle";
        levelCompletionMessages.put(prefix.equals("Upper") ? 3 : prefix.equals("Middle") ? 2 : 1, Pair.of(prefix + " level: ", millis));
        sendGlobalMessage(prefix + " level complete! Duration: " + duration);
    }

    public void recordDeath(@NotNull final String string) {
        deaths.add(string);
    }

    public boolean isChallengeMode() {
        return party != null && party.isChallengeMode();
    }

    @NotNull
    public SharedStorage constructOrGetSharedStorage() {
        if (storage == null) {
            constructStorage();
        }
        return storage;
    }

    public void sendGlobalMessage(final String string) {
        for (final com.zenyte.game.world.entity.player.Player p : players) {
            p.sendMessage(Colour.RS_PINK.wrap(string));
        }
    }

    public int getPoints(@NotNull final Player player) {
        return Math.min(pointsMap.getInt(player.getUsername()), 131071);
    }

    private void constructStorage() {
        storage = new SharedStorage();
        storage.getContainer().setContainerSize(1000);
    }

    public void addCombatPoints(@NotNull final Player player, final int amount) {
        final com.zenyte.game.content.chambersofxeric.map.RaidArea room = getRoom(player.getLocation());
        if (room == null) {
            return;
        }
        final int remainingPoints = room.getRemainingCombatPoints();
        final int appendableAmount = Math.min(amount, remainingPoints);
        if (appendableAmount <= 0) {
            return;
        }
        room.setRemainingCombatPoints(remainingPoints - appendableAmount);
        addPoints(player, appendableAmount);
    }

    public void addPoints(@NotNull final Player player, final int amount) {
        addPoints(player, amount, true);
    }

    public void addPoints(@NotNull final Player player, final int amount, final boolean total) {
        if (!originalPlayers.contains(player.getUsername())) {
            return;
        }
        final int current = getPoints(player);
        if (current >= 131071) {
            return;
        }
        int amt = (int) Math.floor((int) (amount * (player.getVariables().getRaidsBoost() > 0 ? 1.25F : 1.0F)) * (player.getSkills().getCombatLevel() < 115 ? 0.8F : 1));
        if (amt + current > 131071) {
            amt = 131071 - current;
        }
        if (total) {
            totalPoints += amt;
        }
        pointsMap.put(player.getUsername(), current + amt);
        for (final com.zenyte.game.world.entity.player.Player p : players) {
            refreshPoints(p);
        }
    }

    public void decrementPoints(@NotNull final Player player, final int amount) {
        if (!originalPlayers.contains(player.getUsername())) {
            return;
        }
        final int current = getPoints(player);
        if (current <= 0) {
            return;
        }
        final int amt = Math.max(0, Math.min(amount, current));
        totalPoints -= amt;
        pointsMap.put(player.getUsername(), current - amt);
        for (final com.zenyte.game.world.entity.player.Player p : players) {
            refreshPoints(p);
        }
    }

    public void decrementGroupPoints() {
        totalPoints = (int) (totalPoints * 0.9F);
        for (final com.zenyte.game.world.entity.player.Player p : players) {
            refreshPoints(p);
        }
    }

    private void refreshPoints(@NotNull final Player player) {
        final com.zenyte.game.world.entity.player.VarManager varManager = player.getVarManager();
        varManager.sendBit(5431, totalPoints);
        varManager.sendBit(5422, getPoints(player));
    }

    private void constructMap() {
        try {
            map = new RaidMap(this);
            map.construct();
        } catch (OutOfSpaceException e) {
            map.erase();
            throw new RuntimeException("Failure constructing map.", e);
        }
        respawnTile = getMap().getRaidChunks().get(0).getBoundTile();
    }

    public void destroy(final boolean message, final boolean force) {
        if (destroyed || pendingDestroy) {
            return;
        }
        log.info("Pending raid destroying.");
        pendingDestroy = true;
        WorldTasksManager.schedule(() -> {
            if ((!force && containsEligiblePlayers()) || (stage >= 4 && !players.isEmpty())) {
                log.info("Prevented raid from being destroyed.");
                pendingDestroy = false;
                return;
            }
            setIncomplete();
            log.info("Destroying raid.");
            existingRaidsMap.remove(index);
            destroyed = true;
            WorldTasksManager.schedule(() -> {
                if (map != null) {
                    map.erase();
                }
            }, 3);
            if (party.getChannel().getRaidParty() != null) {
                RaidParty.advertisedParties.remove(party.getChannel().getRaidParty());
                party.getChannel().setRaidParty(null);
            }
            for (final Player player : players) {
                if (player.isNulled()) {
                    continue;
                }
                if (message) {
                    player.sendMessage("As there is no one else eligible to kick players from this channel, the raid has been destroyed.");
                }
                fadeOut(player, () -> {
                    player.setLocation(outsideTile);
                    player.getVarManager().sendBit(5432, 0);
                    player.getReceivedHits().clear();
                    player.getReceivedDamage().clear();
                    player.reset();
                    player.setAnimation(null);
                    player.setGraphics(null);
                    player.lock(3);
                    player.blockIncomingHits();
                });
            }
        }, 3);
    }

    private void fadeOut(@NotNull final Player player, final Runnable runnable) {
        RAID_OVERLAY.open(player);
        final com.zenyte.game.packet.PacketDispatcher dispatcher = player.getPacketDispatcher();
        dispatcher.sendClientScript(1512);
        RaidOverlay.setVisibility(player, false);
        WorldTasksManager.schedule(new TickTask() {
            @Override
            public void run() {
                switch (ticks++) {
                case 0: 
                    runnable.run();
                    RAID_OVERLAY.open(player);
                    RaidOverlay.setVisibility(player, true);
                    dispatcher.sendClientScript(948, 0, 255, 0, 0, 15);//Fade
                    dispatcher.sendClientScript(1513);//fade in
                    break;
                case 3: 
                    player.getInterfaceHandler().closeInterface(InterfacePosition.OVERLAY);
                    stop();
                    break;
                }
            }
        }, 0, 0);
    }

    private void addPlayer(final Player player) {
        player.lock();
        players.add(player);
        player.sendSound(enterSound);
        refreshParty();
        launch(player);
        refreshPoints(player);
    }

    private final void launch(@NotNull final Player player) {
        RAID_OVERLAY.open(player);
        final com.zenyte.game.world.entity.player.VarManager varManager = player.getVarManager();
        varManager.sendBit(5456, 0);
        final com.zenyte.game.packet.PacketDispatcher dispatcher = player.getPacketDispatcher();
        dispatcher.sendClientScript(1512);
        RaidOverlay.setVisibility(player, true);
        WorldTasksManager.schedule(() -> {
            player.setLocation(respawnTile);
            player.getInterfaceHandler().openGameTab(GameTab.QUEST_TAB);
            if (player.isOnMobile() && !isChallengeMode()) {
                informLayout(player);
            }
        });
        WorldTasksManager.schedule(() -> {
            RaidOverlay.setVisibility(player, false);
            dispatcher.sendClientScript(948, 0, 255, 0, 0, 15);//Fade
            dispatcher.sendClientScript(1513);//fade in
            readd(player);
            player.unlock();
        }, 1);
    }

    private final void informLayout(@NotNull final Player player) {
        final int chunkX = respawnTile.getChunkX();
        final int chunkY = respawnTile.getChunkY();
        final java.util.List<com.zenyte.game.content.chambersofxeric.map.RaidArea> raidChunks = this.map.getRaidChunks();
        final com.zenyte.game.content.chambersofxeric.map.RaidRoom[] bossRooms = BossPattern.getValues()[0].getRooms();
        //If room is within 6 chunks distance(in any direction), the client knows of it.
        final java.lang.StringBuilder builder = new StringBuilder();
        int knownCombatRoomsCount = 0;
        for (int i = raidChunks.size() - 1; i >= 0; i--) {
            final com.zenyte.game.content.chambersofxeric.map.RaidArea chunk = raidChunks.get(i);
            final com.zenyte.game.content.chambersofxeric.map.RaidRoom type = chunk.getType();
            final boolean isCombatRoom = ArrayUtils.contains(bossRooms, type);
            if (!isCombatRoom) {
                continue;
            }
            final int roomChunkX = chunk.getChunkX();
            final int roomChunkY = chunk.getChunkY();
            final int deltaX = Math.abs(roomChunkX - chunkX);
            final int deltaY = Math.abs(roomChunkY - chunkY);
            final int inverseDeltaX = Math.abs(chunkX - (roomChunkX + 4));
            final int inverseDeltaY = Math.abs(chunkY - (roomChunkY + 4));
            if (!((deltaX > 6 && inverseDeltaX > 6) || (deltaY > 6 && inverseDeltaY > 6))) {
                knownCombatRoomsCount++;
            }
        }
        for (final com.zenyte.game.content.chambersofxeric.map.RaidArea chunk : raidChunks) {
            final com.zenyte.game.content.chambersofxeric.map.RaidRoom type = chunk.getType();
            final boolean isCombatRoom = ArrayUtils.contains(bossRooms, type);
            final boolean isPuzzleRoom = RoomGeneration.PUZZLE_ROOMS.contains(type);
            if (!isCombatRoom && !isPuzzleRoom) {
                continue;
            }
            final int roomChunkX = chunk.getChunkX();
            final int roomChunkY = chunk.getChunkY();
            final int deltaX = Math.abs(roomChunkX - chunkX);
            final int deltaY = Math.abs(roomChunkY - chunkY);
            final int inverseDeltaX = Math.abs(chunkX - (roomChunkX + 4));
            final int inverseDeltaY = Math.abs(chunkY - (roomChunkY + 4));
            if (((deltaX > 6 && inverseDeltaX > 6) || (deltaY > 6 && inverseDeltaY > 6)) && (isPuzzleRoom || knownCombatRoomsCount <= 1)) {
                builder.append("Unknown (").append(isPuzzleRoom ? "puzzle" : "combat").append("), ");
                continue;
            }
            builder.append(type.getRuneliteNaming()).append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        final java.lang.String layout = map.getAlgorithm().getGeneration().getRaidPattern().getLayout();
        final java.lang.String filteredLayout = layout.split(" - ")[0].replaceAll("[^FSCP]", Strings.EMPTY);
        player.sendMessage(Colour.RED.wrap("Layout: ") + "[" + filteredLayout + "]: " + builder.toString());
    }

    public void refreshParty() {
        for (final com.zenyte.game.world.entity.player.Player p : getPlayers()) {
            party.refreshTab(p);
            final long milliseconds = Utils.currentTimeMillis() - getStartTime();
            final long ticks = com.zenyte.game.util.TimeUnit.MILLISECONDS.toTicks(milliseconds);
            p.getVarManager().sendBit(6386, duration != 0 ? duration : stage == 0 ? 0 : ((int) ticks + 1));
        }
    }

    public void refreshTimer() {
        for (final com.zenyte.game.world.entity.player.Player p : getPlayers()) {
            final long milliseconds = Utils.currentTimeMillis() - getStartTime();
            final long ticks = com.zenyte.game.util.TimeUnit.MILLISECONDS.toTicks(milliseconds);
            p.getVarManager().sendBit(6386, duration != 0 ? duration : stage == 0 ? 0 : ((int) ticks + 1));
        }
    }

    public void readd(@NotNull final Player player) {
        players.add(player);
        final com.zenyte.game.world.entity.player.VarManager varManager = player.getVarManager();
        GameInterface.RAID_PARTY_TAB.open(player);
        varManager.sendBit(5432, 1);//Sets tab icon
        varManager.sendBit(8168, 2);//Also tab icon.
        refreshPoints(player);
        if (stage >= 1) {
            player.setViewDistance(Player.SCENE_DIAMETER);
        }
        RAID_OVERLAY.open(player);
    }

    public void enterRaid(final Player player) {
        if (World.isUpdating()) {
            player.getDialogueManager().start(new PlainChat(player, "The game is currently in the process of being updated. You cannot enter a raid until the update is finished."));
            return;
        }
        final java.util.Optional<com.zenyte.game.world.entity.player.Player> owner = World.getPlayer(party.getPlayer());
        if (!owner.isPresent() || (!players.contains(owner.get()) && player != owner.get())) {
            final com.zenyte.game.content.clans.ClanRank rank = ClanManager.getRank(player, party.getChannel());
            if (rank.ordinal() < ClanRank.GENERAL.ordinal()) {
                player.getDialogueManager().start(new PlainChat(player, "Only Generals and above may enter the raid before the party leader."));
                return;
            }
        }
        if (players.size() >= 100) {
            player.sendMessage("The raid is full!");
            return;
        }
        if (map == null) {
            constructMap();
        }
        addPlayer(player);
        party.getChannel().getMembers().forEach(member -> {
            if (!member.inArea(MountQuidamortemArea.class)) {
                return;
            }
            member.sendMessage(Colour.RS_PINK.wrap(member == player ? ("Inviting party...") : (player.getName() + " has invited you to their raid party...")));
        });
    }

    private transient int timer;

    public final void load() {
        if (stage != 0) {
            return;
        }
        stage = 1;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (destroyed) {
                    stop();
                    return;
                }
                farming.process();
                if (++timer % 100 == 0) {
                    refreshTimer();
                }
            }
        }, 0, 0);
        addRaid(this);
        startTime = Utils.currentTimeMillis();
        for (final Player p : getPlayers()) {
            originalPlayers.add(p.getUsername());
            party.refreshTab(p);
            final com.zenyte.game.world.entity.player.VarManager varManager = p.getVarManager();
            varManager.sendBit(5425, stage);
            varManager.sendBit(6386, 1);
        }
        for (final com.zenyte.game.world.entity.player.Player p : party.getChannel().getMembers()) {
            p.sendMessage("<col=fc02e7>The raid has begun!</col>");
        }
        if (players.size() > 0) {
            int level = 0;
            for (final com.zenyte.game.world.entity.player.Player player : players) {
                level += player.getSkills().getLevelForXp(Skills.FARMING);
                if (player.getCombatLevel() > this.combatLevel) {
                    this.combatLevel = player.getCombatLevel();
                }
            }
            this.farmingLevel = level / players.size();
        }
        for (final com.zenyte.game.content.chambersofxeric.map.RaidArea raidChunk : map.getRaidChunks()) {
            try {
                raidChunk.loadRoom();
                raidChunk.calculateRemainingCombatPoints();
            } catch (Exception e) {
                log.error(Strings.EMPTY, e);
            }
        }
        this.potionsCap = psykkCap = (originalPlayers.size() * 5) + 5;
        try {
            map.getBoss().loadRoom();
        } catch (Exception e) {
            log.error(Strings.EMPTY, e);
        }
        map.getBoss().setTotalPhases();
        WorldTasksManager.schedule(() -> {
            for (final com.zenyte.game.world.entity.player.Player player : getPlayers()) {
                player.setViewDistance(Player.SCENE_DIAMETER);
            }
        });
    }

    public void complete() {
        if (this.completed) {
            return;
        }
        this.completed = true;
        this.map.getRaidChunks().forEach(area -> {
            if (area instanceof ScavengerRoom) {
                final java.util.List<com.zenyte.game.content.chambersofxeric.npc.ScavengerBeast> scavengers = ((ScavengerRoom) area).getScavengers();
                scavengers.forEach(NPC::finish);
            }
        });
    }

    private void destroyIfIneligible() {
        if (pendingDestroy || destroyed || containsEligiblePlayers()) {
            return;
        }
        destroy(true, true);
    }

    private boolean containsEligiblePlayers() {
        for (final com.zenyte.game.world.entity.player.Player p : players) {
            if (ClanManager.canKick(p, party.getChannel())) {
                return true;
            }
            if (party.getPlayer().equals(p.getUsername())) {
                return true;
            }
        }
        return false;
    }

    public void stashRewards(@NotNull final Player player) {
        final com.zenyte.game.content.chambersofxeric.rewards.RaidRewards rewards = getRewards();
        if (rewards == null) {
            return;
        }
        final com.zenyte.game.content.chambersofxeric.storageunit.PrivateStorage privateStorage = player.getPrivateStorage();
        privateStorage.resetInaccessibleItems();
        final com.zenyte.game.world.entity.player.container.Container playerRewards = rewards.getRewardMap().get(player);
        if (playerRewards != null && playerRewards.getSize() > 0) {
            for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<com.zenyte.game.item.Item> entry : playerRewards.getItems().int2ObjectEntrySet()) {
                privateStorage.getContainer().deposit(null, playerRewards, entry.getIntKey(), entry.getValue().getAmount());
            }
        }
    }

    private void leaveThroughLogout(@NotNull final Player player) {
        players.remove(player);
        refreshTab();
        dropInternalItems(player, new Location(player.getLocation()));
        player.forceLocation(outsideTile);
        MountQuidamortemArea.appiontAnotherPartyLeader(player);
        destroyIfIneligible();
    }

    public void leaveRaid(@NotNull final Player player, final boolean logout, final boolean destroyIfEmptyOnly) {
        if (logout) {
            leaveThroughLogout(player);
            return;
        }
        player.lock(10);
        players.remove(player);
        player.getVarManager().sendBit(5425, 0);
        refreshTab();
        fadeOut(player, () -> {
            player.lock(1);
            dropInternalItems(player, new Location(player.getLocation()));
            player.setLocation(outsideTile);
            player.sendSound(leaveSound);
            player.getVarManager().sendBit(5432, 0);
            if (!player.getPrivateStorage().resetInaccessibleItems().getContainer().isEmpty()) {
                player.sendMessage(Colour.RED.wrap("You left something in a storage unit. You can retrieve your stuff by attempting to enter the Chambers of Xeric."));
            }
            if (!destroyIfEmptyOnly) {
                MountQuidamortemArea.appiontAnotherPartyLeader(player);
                destroyIfIneligible();
            } else {
                if (players.size() == 0) {
                    destroy(false, true);
                }
            }
        });
    }

    public void refreshTab() {
        for (final com.zenyte.game.world.entity.player.Player p : getPlayers()) {
            party.refreshTab(p);
        }
    }

    public void dropInternalItems(@NotNull final Player player, @NotNull final Location tile) {
        final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
        for (int i = 0; i < 28; i++) {
            final com.zenyte.game.item.Item item = inventory.getItem(i);
            if (item == null) {
                continue;
            }
            final int id = item.getId();
            if (isInternalItem(id)) {
                World.spawnFloorItem(item, player, tile);
                inventory.set(i, null);
            }
        }
        inventory.refreshAll();
    }

    public RaidArea getRoom(final Location player) {
        if (map == null) {
            return null;
        }
        if (player.withinDistance(map.getBoss().getCenter(), 30)) {
            return map.getBoss();
        }
        final int x = player.getX();
        final int y = player.getY();
        for (final com.zenyte.game.content.chambersofxeric.map.RaidArea chunk : map.getRaidChunks()) {
            if (chunk.getToPlane() != player.getPlane()) {
                continue;
            }
            final int minX = chunk.getChunkX() * 8;
            final int maxX = (chunk.getChunkX() + 4) * 8;
            final int minY = chunk.getChunkY() * 8;
            final int maxY = (chunk.getChunkY() + 4) * 8;
            if (x >= minX && x < maxX && y >= minY && y < maxY) {
                return chunk;
            }
        }
        return null;
    }


    enum RaidStatus {
        NOT_STARTED, INCOMPLETE, COMPLETE;
    }

    public RaidParty getParty() {
        return this.party;
    }

    public Set<Player> getPlayers() {
        return this.players;
    }

    public Set<String> getOriginalPlayers() {
        return this.originalPlayers;
    }

    public Object2IntMap<String> getPointsMap() {
        return this.pointsMap;
    }

    public RaidFarming getFarming() {
        return this.farming;
    }

    public List<String> getDeaths() {
        return this.deaths;
    }

    public Int2ObjectMap<Pair<String, Long>> getLevelCompletionMessages() {
        return this.levelCompletionMessages;
    }

    public RaidMap getMap() {
        return this.map;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public int getIndex() {
        return this.index;
    }

    public RaidRewards getRewards() {
        return this.rewards;
    }

    public void setRewards(final RaidRewards rewards) {
        this.rewards = rewards;
    }

    public SharedStorage getStorage() {
        return this.storage;
    }

    public int getTotalPoints() {
        return this.totalPoints;
    }

    public int getStage() {
        return this.stage;
    }

    public boolean isConstructingStorage() {
        return this.constructingStorage;
    }

    public void setConstructingStorage(final boolean constructingStorage) {
        this.constructingStorage = constructingStorage;
    }

    public boolean isDestroyed() {
        return this.destroyed;
    }

    public Location getRespawnTile() {
        return this.respawnTile;
    }

    public void setRespawnTile(final Location respawnTile) {
        this.respawnTile = respawnTile;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(final int duration) {
        this.duration = duration;
    }

    public boolean isRecorded() {
        return this.recorded;
    }

    public void setRecorded(final boolean recorded) {
        this.recorded = recorded;
    }

    public int getFarmingLevel() {
        return this.farmingLevel;
    }

    public GreatOlm getOlm() {
        return this.olm;
    }

    public void setOlm(final GreatOlm olm) {
        this.olm = olm;
    }

    public int getPotionsCap() {
        return this.potionsCap;
    }

    public void setPotionsCap(final int potionsCap) {
        this.potionsCap = potionsCap;
    }

    public int getPsykkCap() {
        return this.psykkCap;
    }

    public void setPsykkCap(final int psykkCap) {
        this.psykkCap = psykkCap;
    }

    public RaidStatus getStatus() {
        return this.status;
    }

    public void setStatus(final RaidStatus status) {
        this.status = status;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public String getOnyxDropMessage() {
        return this.onyxDropMessage;
    }

    public void setOnyxDropMessage(final String onyxDropMessage) {
        this.onyxDropMessage = onyxDropMessage;
    }
}
