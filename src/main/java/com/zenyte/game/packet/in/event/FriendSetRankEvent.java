package com.zenyte.game.packet.in.event;

import com.zenyte.game.content.clans.ClanManager;
import com.zenyte.game.content.clans.ClanRank;
import com.zenyte.game.packet.in.ClientProtEvent;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Tommeh | 25-1-2019 | 20:17
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class FriendSetRankEvent implements ClientProtEvent {
    private final String name;
    private final int rank;

    @Override
    public void log(@NotNull final Player player) {
        log(player, "Name: " + name + ", rank: " + rank);
    }

    @Override
    public void handle(Player player) {
        final com.zenyte.game.content.clans.ClanChannel channel = ClanManager.CLAN_CHANNELS.get(player.getPlayerInformation().getUsername());
        if (channel == null) {
            return;
        }
        final com.zenyte.game.content.clans.ClanRank clanRank = ClanRank.getRank(rank);
        if (clanRank == null) {
            return;
        }
        final java.util.Optional<com.zenyte.game.world.entity.player.Player> loggedInPlayer = World.getPlayer(name);
        if (!loggedInPlayer.isPresent()) {
            channel.getRankedMembers().put(Utils.formatUsername(name), clanRank);
            player.getPacketDispatcher().initFriendsList();
        } else {
            channel.getRankedMembers().put(Utils.formatUsername(name), clanRank);
            if (channel.getMembers().contains(loggedInPlayer.get())) {
                ClanManager.refreshPartial(channel, loggedInPlayer.get(), true, false);
            }
            player.getPacketDispatcher().initFriendsList();
        }
    }

    @Override
    public LogLevel level() {
        return LogLevel.HIGH_PACKET;
    }

    public FriendSetRankEvent(final String name, final int rank) {
        this.name = name;
        this.rank = rank;
    }
}
