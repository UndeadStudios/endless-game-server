package com.zenyte.game.packet.out;

import com.zenyte.Constants;
import com.zenyte.game.content.clans.ClanManager;
import com.zenyte.game.packet.GamePacketEncoder;
import com.zenyte.game.packet.ServerProt;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.game.packet.GamePacketOut;
import com.zenyte.network.io.RSBuffer;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Tommeh | 28 jul. 2018 | 18:55:03
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class UpdateFriendList implements GamePacketEncoder {
    private final Player player;
    private final List<FriendEntry> friends;

    public UpdateFriendList(final Player player, final List<FriendEntry> friends) {
        this.player = player;
        this.friends = friends;
    }

    @Override
    public void log(@NotNull final Player player) {
        log(player, Strings.EMPTY);
    }

    @Override
    public GamePacketOut encode() {
        final com.zenyte.game.packet.ServerProt prot = ServerProt.UPDATE_FRIENDLIST;
        final com.zenyte.network.io.RSBuffer buffer = new RSBuffer(prot);
        for (final com.zenyte.game.packet.out.UpdateFriendList.FriendEntry entry : friends) {
            final java.lang.String username = entry.username;
            final java.lang.String displayname = Utils.formatString(entry.username);
            final java.util.Optional<com.zenyte.game.world.entity.player.Player> friend = World.getPlayer(username);
            int world = 0;
            if (friend.isPresent() && player.getSocialManager().isVisible(friend.get())) {
                world = Constants.WORLD_PROFILE.getNumber();
            }
            buffer.writeByte(entry.added ? 1 : 0);
            buffer.writeString(displayname);
            buffer.writeString(Strings.EMPTY);
            buffer.writeShort(world);
            buffer.writeByte(ClanManager.getRank(player, username).getId());
            buffer.writeByte(0);
            if (world > 0) {
                buffer.writeString("");
                buffer.writeByte(0);
                buffer.writeInt(0);
            }
            buffer.writeString("");
        }
        return new GamePacketOut(prot, buffer);
    }


    public static final class FriendEntry {
        private final String username;
        private final boolean added;

        public FriendEntry(final String username, final boolean added) {
            this.username = username;
            this.added = added;
        }
    }

    @Override
    public LogLevel level() {
        return LogLevel.LOW_PACKET;
    }
}
