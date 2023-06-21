package com.zenyte.game.packet.out;

import com.zenyte.game.packet.GamePacketEncoder;
import com.zenyte.game.packet.ServerProt;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.game.packet.GamePacketOut;
import com.zenyte.network.io.RSBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kris | 03/03/2019 23:46
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class IfModelRotate implements GamePacketEncoder {
    private final int interfaceId;
    private final int componentId;
    private final int roll;
    private final int pitch;

    @Override
    public void log(@NotNull final Player player) {
        log(player, "Interface: " + interfaceId + ", component: " + componentId + ", roll: " + roll + ", pitch: " + pitch);
    }

    @Override
    public GamePacketOut encode() {
        final com.zenyte.game.packet.ServerProt prot = ServerProt.IF_MODEL_ROTATE;
        final com.zenyte.network.io.RSBuffer buffer = new RSBuffer(prot);
        buffer.writeShort128(roll);
        buffer.writeShortLE(pitch);
        buffer.writeIntV1(interfaceId << 16 | componentId);
        return new GamePacketOut(prot, buffer);
    }

    @Override
    public LogLevel level() {
        return LogLevel.LOW_PACKET;
    }

    public IfModelRotate(final int interfaceId, final int componentId, final int roll, final int pitch) {
        this.interfaceId = interfaceId;
        this.componentId = componentId;
        this.roll = roll;
        this.pitch = pitch;
    }
}
