package com.zenyte.game.ui.testinterfaces;

import com.google.common.eventbus.Subscribe;
import com.zenyte.GameEngine;
import com.zenyte.game.BonusXpManager;
import com.zenyte.game.GameClock;
import com.zenyte.game.constants.GameInterface;
import com.zenyte.game.content.achievementdiary.Diary;
import com.zenyte.game.ui.Interface;
import com.zenyte.game.util.AccessMask;
import com.zenyte.game.util.Colour;
import com.zenyte.game.util.TimeUnit;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.player.*;
import com.zenyte.game.world.region.area.wilderness.WildernessArea;
import com.zenyte.plugins.events.LoginEvent;
import com.zenyte.plugins.events.LogoutEvent;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tommeh | 2-12-2018 | 16:05
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class GameNoticeboardInterface extends Interface {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final Privilege[] STAFF = {Privilege.OWNER,Privilege.CO_OWNER,Privilege.COMMUNITY_MANAGER, Privilege.ADMINISTRATOR, Privilege.GLOBAL_MODERATOR, Privilege.MODERATOR, Privilege.SUPPORT, Privilege.FORUM_MODERATOR};

    @Subscribe
    public static final void onLogin(final LoginEvent event) {
        final com.zenyte.game.world.entity.player.Player p = event.getPlayer();
        p.getPacketDispatcher().sendClientScript(3501, 162 << 16 | 2, 701 << 16 | 11, 701 << 16 | 16, 701 << 16 | 31, 701 << 16 | 32, 701 << 16 | 33, 701 << 16 | 43);
        p.getVarManager().sendVar(3500, (int) TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - GameEngine.SERVER_START_TIME));
        p.getVarManager().sendVar(3501, (int) (p.getVariables().getPlayTime() * 0.6));
        p.getVarManager().sendVar(3506, Math.max(0, (int) TimeUnit.MILLISECONDS.toSeconds(BonusXpManager.expirationDate - System.currentTimeMillis())));
        p.getVarManager().sendVar(3507, (int) (p.getVariables().getRaidsBoost() * 0.6F));
        p.getVarManager().sendVar(3801, (int) (p.getVariables().getBonusXP() * 0.6F));
        refreshCounters(true);
        final java.util.Optional<com.zenyte.game.ui.Interface> optionalPlugin = GameInterface.GAME_NOTICEBOARD.getPlugin();
        if (optionalPlugin.isPresent()) {
            final com.zenyte.game.ui.Interface plugin = optionalPlugin.get();
            p.getPacketDispatcher().sendComponentText(plugin.getInterface(), plugin.getComponent("Time"), "Time: <col=ffffff>" + GameClock.gameTime());
        }
    }

    public static final void refreshXericsWisdom(@NotNull final Player player) {
        player.getVarManager().sendVar(3507, (int) (player.getVariables().getRaidsBoost() * 0.6F));
    }

    public static final void refreshBonusXP() {
        for (final com.zenyte.game.world.entity.player.Player player : World.getPlayers()) {
            player.getVarManager().sendVar(3506, Math.max(0, (int) TimeUnit.MILLISECONDS.toSeconds(BonusXpManager.expirationDate - System.currentTimeMillis())));
        }
    }

    public static final void refreshCounters(final boolean all) {
        final com.zenyte.game.world.EntityList<com.zenyte.game.world.entity.player.Player> players = World.getPlayers();
        final int total = players.size();
        final org.apache.commons.lang3.mutable.MutableInt staff = new MutableInt();
        final org.apache.commons.lang3.mutable.MutableInt mobile = new MutableInt();
        final org.apache.commons.lang3.mutable.MutableInt wilderness = new MutableInt();
        for (final com.zenyte.game.world.entity.player.Player player : players) {
            if (all) {
                if (player.getPrivilege().eligibleTo(Privilege.FORUM_MODERATOR) && player.getSocialManager().getStatus().equals(SocialManager.PrivateStatus.ALL)) {
                    staff.increment();
                }
                if (player.getPlayerInformation().getDevice() == Device.MOBILE) {
                    mobile.increment();
                }
            }
            if (player.getVarManager().getBitValue(WildernessArea.IN_WILDERNESS_VARBIT_SPECIAL_UNCLICKABLE) == 1) {
                wilderness.increment();
            }
        }
        for (final com.zenyte.game.world.entity.player.Player player : players) {
            if (all) {
                player.getVarManager().sendVar(3502, total);
                player.getVarManager().sendVar(3503, staff.intValue());
                player.getVarManager().sendVar(3508, mobile.intValue());
            }
            player.getVarManager().sendVar(3509, wilderness.intValue());
        }
    }

    @Subscribe
    public static final void onLogout(final LogoutEvent event) {
        refreshCounters(true);
    }

    private static List<Player> getStaff(final Player requester, final Privilege privilege) {
        return World.getPlayers().stream().filter(p -> p.getPrivilege().equals(privilege) && (!isHidden(p) || requester.getPrivilege().eligibleTo(Privilege.FORUM_MODERATOR))).collect(Collectors.toList());
    }


    public enum StaffStatus {
        NOT_STAFF, PUBLIC, NOT_PUBLIC;
    }

    public static final StaffStatus getStaffStatus(final Player player) {
        if (!player.getPrivilege().eligibleTo(Privilege.SUPPORT)) {
            return StaffStatus.NOT_STAFF;
        }
        return player.getSocialManager().getStatus().equals(SocialManager.PrivateStatus.ALL) ? StaffStatus.PUBLIC : StaffStatus.NOT_PUBLIC;
    }

    public static void showStaffOnline(final Player player) {
        final java.util.ArrayList<java.lang.String> lines = new ArrayList<String>();
        int count = 0;
        for (final com.zenyte.game.world.entity.player.Privilege privilege : STAFF) {
            final java.util.List<com.zenyte.game.world.entity.player.Player> members = getStaff(player, privilege);
            count += members.size();
            lines.add(privilege.getCrown() + " <col=00080>" + privilege + (privilege == Privilege.OWNER ? "" : "s") + "</col>");
            if (members.isEmpty()) {
                lines.add("- Nobody");
            } else {
                members.forEach(p -> lines.add(p.getName() + (isHidden(p) ? " (" + Colour.MAROON.wrap("Hidden") + ")" : "")));
            }
            lines.add("\n");
        }
        Diary.sendJournal(player, "Staff online: " + count, lines);
    }

    private static boolean isHidden(final Player player) {
        return !player.getSocialManager().getStatus().equals(SocialManager.PrivateStatus.ALL);
    }

    @Override
    protected void attach() {
        put(8, "Players online");
        put(9, "Staff online");
        put(10, "Wilderness players");
        put(11, "Up-time");
        put(12, "Time");
        put(14, "2FA");
        put(15, "XP rate");
        put(16, "Time played");
        put(17, "Register date");
        put(18, "Privilege");
        put(19, "Game Mode");
        put(20, "Member Rank");
        put(21, "Loyalty points");
        put(22, "Total donated");
        put(23, "Vote credits");
        //put(24, "Store credits");
        put(25, "Game Settings");
        put(27, "Drop Viewer");
        put(29, "Daily Challenges");
        put(31, "Bonus XP");
        put(32, "CoX Boost");
        put(35, "Website");
        put(37, "Forums");
        put(39, "Discord");
        put(41, "Store");
    }

    @Override
    public void open(Player player) {
        final long time = player.getNumericAttribute("forum registration date").longValue();
        final java.lang.String date = FORMATTER.format(Instant.ofEpochMilli(time == 0 ? System.currentTimeMillis() : time).atZone(ZoneId.systemDefault()).toLocalDate());
        final int yearIndex = !date.contains("201") ? date.indexOf("202") : date.indexOf("201");
        final java.lang.String formatted = date.substring(3, yearIndex - 1) + " " + Utils.suffixOrdinal(Integer.parseInt(date.substring(0, 2))) + " " + date.substring(yearIndex, yearIndex + 4);
        player.getInterfaceHandler().sendInterface(getInterface());
        player.getPacketDispatcher().sendComponentText(getInterface(), getComponent("Time"), "Time: <col=ffffff>" + GameClock.gameTime());
        player.getPacketDispatcher().sendComponentText(getInterface(), getComponent("2FA"), "Two-Factor Authentication");
        player.getPacketDispatcher().sendComponentText(getInterface(), getComponent("XP rate"), "XP: <col=ffffff>" + ((player.getSkillingXPRate() == 1) ? "-" : (player.getCombatXPRate() + "x Combat & " + player.getSkillingXPRate() + "x Skilling</col>")));
        player.getPacketDispatcher().sendComponentText(getInterface(), getComponent("Register date"), "Registered on: <col=ffffff>" + player.getPlayerInformation().getRegistryDate() + "</col>");
        player.getPacketDispatcher().sendComponentText(getInterface(), getComponent("Privilege"), "Privilege: <col=ffffff>" + player.getPrivilege().getCrown() + player.getPrivilege().toString() + "</col>");
        player.getPacketDispatcher().sendComponentText(getInterface(), getComponent("Game Mode"), "Mode: <col=ffffff>" + player.getGameMode().getCrown() + player.getGameMode().toString() + "</col>");
        player.getPacketDispatcher().sendComponentText(getInterface(), getComponent("Member Rank"), "Member: <col=ffffff>" + player.getMemberRank().getCrown() + player.getMemberRank().toString().replace(" Member", "") + "</col>");
        player.getPacketDispatcher().sendComponentText(getInterface(), getComponent("Loyalty points"), "Loyalty points: <col=ffffff>" + player.getLoyaltyManager().getLoyaltyPoints() + "</col>");
        final java.lang.String totalDonated = "Total donated: <col=ffffff>$" + player.getNumericAttribute("total donated online") + "</col>";
        player.log(LogLevel.INFO, totalDonated);
        player.getPacketDispatcher().sendComponentText(getInterface(), getComponent("Total donated"), totalDonated);
        player.getPacketDispatcher().sendComponentText(getInterface(), getComponent("Vote credits"), "Vote credits: <col=ffffff>" + player.getNumericAttribute("vote_points").intValue() + "</col>");
        player.getPacketDispatcher().sendComponentSettings(getInterface(), getComponent("2FA"), -1, 0, AccessMask.CLICK_OP1);
    }

    @Override
    protected void build() {
        bind("Staff online", GameNoticeboardInterface::showStaffOnline);
        bind("Game Settings", GameInterface.GAME_SETTINGS::open);
        bind("Drop Viewer", GameInterface.DROP_VIEWER::open);
        bind("Daily Challenges", GameInterface.DAILY_CHALLENGES_OVERVIEW::open);
        //bind("2FA", player -> player.getPacketDispatcher().sendURL("https://vigilancegaming.net/topic/362-two-factor-authentication-guide/"));
        bind("Website", player -> player.getPacketDispatcher().sendURL("https://vigilancegaming.net/"));
        bind("Forums", player -> player.getPacketDispatcher().sendURL("https://vigilancegaming.net/community/"));
        bind("Discord", player -> player.getPacketDispatcher().sendURL("https://vigilancegaming.net/discord/"));
        bind("Store", player -> player.getPacketDispatcher().sendURL("https://vigilancegaming.net/store/"));
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.GAME_NOTICEBOARD;
    }
}
