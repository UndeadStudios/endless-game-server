package com.zenyte.game.packet.out;

import com.zenyte.game.packet.GamePacketEncoder;
import com.zenyte.game.packet.ServerProt;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.object.WorldObject;
import com.zenyte.network.game.packet.GamePacketOut;
import com.zenyte.network.io.RSBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Tommeh | 28 jul. 2018 | 18:28:11
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class LocAdd implements GamePacketEncoder {
	private final WorldObject object;

	@Override
	public void log(@NotNull final Player player) {
		log(player, "Id: " + object.getId() + ", type: " + object.getType() + ", rotation: " + object.getRotation() + ", x: " + object.getX() + ", y: " + object.getY() + ", z: " + object.getPlane());
	}

	@Override
	public GamePacketOut encode() {
		final com.zenyte.game.packet.ServerProt prot = ServerProt.LOC_ADD;
		final com.zenyte.network.io.RSBuffer buffer = new RSBuffer(prot);
		final int targetLocalX = object.getX() - ((object.getX() >> 3) << 3);
		final int targetLocalY = object.getY() - ((object.getY() >> 3) << 3);
		final int offsetHash = (targetLocalX & 7) << 4 | (targetLocalY & 7);
		buffer.writeByte128(offsetHash);
		buffer.write128Byte((object.getType() << 2) | object.getRotation());
		buffer.writeShort128(object.getId());
		return new GamePacketOut(prot, buffer);
	}

	@Override
	public LogLevel level() {
		return LogLevel.LOW_PACKET;
	}

	public LocAdd(final WorldObject object) {
		this.object = object;
	}
}
