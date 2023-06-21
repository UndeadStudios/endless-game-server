package com.zenyte.game.packet.out;

import com.zenyte.game.packet.GamePacketEncoder;
import com.zenyte.game.packet.ServerProt;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.game.packet.GamePacketOut;
import com.zenyte.network.io.RSBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kris | 04/03/2019 00:05
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class Heatmap implements GamePacketEncoder {
    @Override
    public void log(@NotNull final Player player) {
        log(player, "Enabled: " + enabled);
    }

    private final boolean enabled;

    @Override
    public GamePacketOut encode() {
        final com.zenyte.game.packet.ServerProt prot = ServerProt.HEATMAP;
        final com.zenyte.network.io.RSBuffer buffer = new RSBuffer(prot);
        buffer.writeByte(enabled ? 1 : 0);
        return new GamePacketOut(ServerProt.HEATMAP, buffer);
    }

    @Override
    public LogLevel level() {
        return LogLevel.HIGH_PACKET;
    }

    public Heatmap(final boolean enabled) {
        this.enabled = enabled;
    }
}
