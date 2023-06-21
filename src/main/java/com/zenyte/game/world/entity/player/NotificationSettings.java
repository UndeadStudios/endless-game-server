package com.zenyte.game.world.entity.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.zenyte.game.item.Item;
import com.zenyte.game.ui.InterfacePosition;
import com.zenyte.game.util.AccessMask;
import com.zenyte.game.util.Utils;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Tommeh | 19 feb. 2018 : 20:18:54
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server
 * profile</a>}
 */
public class NotificationSettings {
    public static final int INTERFACE = 492;
    public static final ImmutableList<String> BOSS_NPC_NAMES = ImmutableList.of("general graardor", "kree\'arra", "commander zilyana", "k\'ril tsutsaroth", "callisto", "venenatis", "scorpia", "vet\'ion", "chaos elemental", "chaos fanatic", "crazy archaeologist", "cerberus", "vorkath", "zulrah", "giant mole", "thermonuclear smoke devil", "king black dragon", "dagannoth rex", "dagannoth supreme", "dagannoth prime", "skotizo", "barrows", "kraken", "grotesque guardians", "hespori", "obor", "bryophyta", "corporeal beast", "tztok-jad", "alchemical hydra", "tzkal-zuk", "mimic");
    public static final ImmutableList<String> SLAYER_NPC_NAMES = ImmutableList.of("crawling hand", "cave bug", "cave crawler", "banshee", "cave slime", "rockslug", "desert lizard", "cockatrice", "pyrefiend", "mogre", "harpie bug swarm", "wall beast", "killerwatt", "molanisk", "basilisk", "sea snake", "terror dog", "fever spider", "infernal mage", "brine rat", "bloodveld", "jellie", "turoth", "mutated zygomite", "cave horror", "aberrant spectre", "spiritual ranger", "dust devil", "spiritual warrior", "kurask", "skeletal wyvern", "gargoyle", "nechryael", "spiritual mage", "abyssal demon", "cave kraken", "dark beast", "smoke devil", "tortured gorilla", "demonic gorilla", "adamant dragon", "rune dragon", "superior creature", "brutal black dragon", "fossil island wyvern", "revenant", "hydra", "wyrm", "drake");
    public static final ImmutableList<String> EXTRA_TRACKED_NPC_NAMES = ImmutableList.of("lizardman", "lizardman shaman", "lizardman brute");
    private final transient Player player;
    private static final ImmutableMap<String, String> slayerRedirections = ImmutableMap.<String, String>builder().put("crushing hand", "crawling hand").put("chasm crawler", "cave crawler").put("screaming banshee", "banshee").put("screaming twisted banshee", "banshee").put("giant rockslug", "rock slug").put("cockathrice", "cockatrice").put("flaming pyrelord", "pyrefiend").put("infernal pyrelord", "pyrelord").put("monstrous basilisk", "basilisk").put("malevolent mage", "infernal mage").put("insatiable bloodveld", "bloodveld").put("insatiable mutated bloodveld", "bloodveld").put("vitreous jelly", "jelly").put("vitreous warped jelly", "warped jelly").put("cave abomination", "cave horror").put("abhorrent spectre", "aberrant spectre").put("repugnant spectre", "aberrant spectre").put("basilisk sentinel", "basilisk knight").put("choke devil", "dust devil").put("king kurask", "kurask").put("marble gargoyle", "gargoyle").put("nechryarch", "nechryael").put("greater nechryael", "nechryael").put("greater abyssal demon", "abyssal demon").put("night beast", "dark beast").put("nuclear smoke devil", "smoke devil").put("deviant spectre", "aberrant spectre").put("mutated bloodveld", "bloodveld").put("twisted banshee", "banshee").build();
    /**
     * A list of pairings which will show up in the slayer killcount when you slaughtered the given boss.
     * So for example, if someone killed 100 kraken bosses, the slayer log should show cave krakens as quantity 100, plus whatever cave krakens they killed..
     */
    private static final List<Pair<String, String>> bossToSlayerInclusions = Collections.unmodifiableList(Arrays.asList(new Pair<>("kraken", "cave kraken"), new Pair<>("thermonuclear smoke devil", "smoke devil"), new Pair<>("grotesque guardians", "gargoyle"), new Pair<>("alchemical hydra", "hydra")));

    public NotificationSettings(final Player player) {
        this.player = player;
    }

    public static final boolean isKillcountTracked(@NotNull final String source) {
        final java.lang.String lowercase = source.toLowerCase();
        final java.lang.String name = slayerRedirections.getOrDefault(lowercase, lowercase);
        return BOSS_NPC_NAMES.contains(name) || SLAYER_NPC_NAMES.contains(name) || EXTRA_TRACKED_NPC_NAMES.contains(name);
    }

    public void setThresholdValue(final int option) {
        final int threshold = player.getNumericAttribute(Setting.THRESHOLD_DROP_VALUE.toString()).intValue();
        if (threshold > 0 && option == 1) {
            player.getSettings().toggleSetting(Setting.LOOT_DROP_NOTIFICATIONS);
            return;
        }
        player.sendInputInt(option == 2 ? "Change threshold value: (" + Utils.format(threshold) + ")" : "Set threshold value:", amount -> {
            final int value = Math.min(amount, 536870911);
            player.getSettings().setSetting(Setting.THRESHOLD_DROP_VALUE, value);
            player.getSettings().setSetting(Setting.LOOT_DROP_NOTIFICATIONS, 1);
        });
    }

    public void setDropWarningTresholdValue(final int option) {
        final int threshold = player.getNumericAttribute(Setting.TRESHOLD_DROP_WARNING_VALUE.toString()).intValue();
        if (threshold > 0 && option == 1) {
            player.getSettings().toggleSetting(Setting.LOOT_DROP_WARNING_NOTIFICATIONS);
            return;
        }
        player.sendInputInt(option == 2 ? "Change threshold value: (" + Utils.format(threshold) + ")" : "Set threshold value:", amount -> {
            final int value = Math.min(amount, 536870911);
            player.getSettings().setSetting(Setting.TRESHOLD_DROP_WARNING_VALUE, value);
            player.getSettings().setSetting(Setting.LOOT_DROP_WARNING_NOTIFICATIONS, value > 0 ? 1 : 0);
        });
    }

    public void increaseKill(final String requestedName) {
        final java.lang.String lowercaseName = requestedName.toLowerCase();
        final java.lang.String name = slayerRedirections.getOrDefault(lowercaseName, lowercaseName);
        int kills = (player.getSettings().getKillsLog().getOrDefault(name, 0) & 65535) + 1;
        int streak = ((player.getSettings().getKillsLog().getOrDefault(name, 0) >> 16) & 65535) + 1;
        if (kills > 65535) {
            kills = 65535;
        }
        if (streak > 65535) {
            streak = 65535;
        }
        final int packed = kills & 65535 | (streak & 65535) << 16;
        player.getSettings().getKillsLog().put(name, packed);
    }

    public boolean shouldNotifyRareDrop(@NotNull final Item item) {
        if (!item.isTradable()) {
            return false;
        }
        final int value = item.getSellPrice();
        final int threshold = player.getNumericAttribute(Setting.THRESHOLD_DROP_VALUE.toString()).intValue();
        return player.getBooleanSetting(Setting.LOOT_DROP_NOTIFICATIONS) && threshold != 0 && value >= threshold;
    }

    public void sendDropNotification(final Item item) {
        final int value = item.getSellPrice();
        final int threshold = player.getNumericAttribute(Setting.THRESHOLD_DROP_VALUE.toString()).intValue();
        final mgi.types.config.items.ItemDefinitions defs = item.getDefinitions();
        if (!item.isTradable()) {
            if (player.getBooleanSetting(Setting.LOOT_DROP_NOTIFICATIONS) && player.getBooleanSetting(Setting.UNTRADEABLE_DROP_NOTIFICATIONS)) {
                player.sendMessage("<col=ef1020>Untradeable drop: " + defs.getName() + "</col>");
                return;
            }
        }
        if (!player.getBooleanSetting(Setting.LOOT_DROP_NOTIFICATIONS) || threshold == 0 || value < threshold) {
            return;
        }
        final int amount = item.getAmount();
        if (amount == 1) {
            player.sendMessage("<col=ef1020>Valuable drop: " + defs.getName() + " (" + Utils.format(value) + " coins)</col>");
        } else {
            player.sendMessage("<col=ef1020>Valuable drop: " + amount + " x " + defs.getName() + " (" + Utils.format(value * amount) + " coins)</col>");
        }
    }

    public void sendBossKillCountNotification(final String name) {
        final int kills = player.getSettings().getKillsLog().getOrDefault(name.toLowerCase(), 0) & 65535;
        player.sendMessage("Your " + Utils.formatString(name) + " kill count is: <col=FF0000>" + kills + "</col>.");
    }

    public int getKillcount(final String name) {
        final java.lang.String lowercase = name.toLowerCase();
        final java.lang.String lowercaseName = slayerRedirections.getOrDefault(lowercase, lowercase);
        final int defaultKills = player.getSettings().getKillsLog().getOrDefault(lowercaseName, 0) & 65535;
        int extraKills = 0;
        for (final kotlin.Pair<java.lang.String, java.lang.String> pair : bossToSlayerInclusions) {
            if (pair.getSecond().equals(lowercaseName)) {
                extraKills = player.getSettings().getKillsLog().getOrDefault(pair.getFirst(), 0) & 65535;
                break;
            }
        }
        return Math.min(65535, defaultKills + extraKills);
    }

    public int getKillstreak(final String name) {
        final java.lang.String lowercaseName = name.toLowerCase();
        final int defaultKills = (player.getSettings().getKillsLog().getOrDefault(lowercaseName, 0) >> 16) & 65535;
        int extraKills = 0;
        for (final kotlin.Pair<java.lang.String, java.lang.String> pair : bossToSlayerInclusions) {
            if (pair.getSecond().equals(lowercaseName)) {
                extraKills = (player.getSettings().getKillsLog().getOrDefault(pair.getFirst(), 0) >> 16) & 65535;
                break;
            }
        }
        return Math.min(65535, defaultKills + extraKills);
    }

    public void sendKillLog(final ImmutableList<String> names, final boolean sendInterface) {
        if (sendInterface) {
            player.getInterfaceHandler().sendInterface(InterfacePosition.CENTRAL, 549);
        }
        final java.lang.StringBuilder[] builders = new StringBuilder[] {new StringBuilder(), new StringBuilder(), new StringBuilder()};
        final java.lang.String type = names.size() >= 42 ? "Slayer" : "Boss";
        for (final java.lang.String name : names) {
            final java.lang.String lowerCase = name.toLowerCase();
            final int kills = getKillcount(lowerCase);
            final int streaks = getKillstreak(lowerCase);
            final java.lang.String n = name.equals("tztok-jad") ? "TzTok-Jad" : name.equals("tzkal-zuk") ? "TzKal-Zuk" : Utils.formatString(name);
            builders[0].append(n).append(type.equals("Slayer") ? "s|" : "|");
            builders[1].append(kills).append("|");
            builders[2].append(streaks).append("|");
        }
        player.getPacketDispatcher().sendComponentSettings(549, 16, 0, names.size(), AccessMask.CLICK_OP1);
        player.getPacketDispatcher().sendClientScript(1584, builders[0].toString(), builders[1].toString(), builders[2].toString(), names.size(), type + " Kill Log");
        player.getTemporaryAttributes().put("KillLogType", type);
    }
}
