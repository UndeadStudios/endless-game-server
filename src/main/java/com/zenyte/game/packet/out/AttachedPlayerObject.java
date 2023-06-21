package com.zenyte.game.packet.out;

import com.zenyte.game.packet.GamePacketEncoder;
import com.zenyte.game.packet.ServerProt;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.object.AttachedObject;
import com.zenyte.network.game.packet.GamePacketOut;
import com.zenyte.network.io.RSBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * Sends an object's model to the requested coordinates. The point of the packet
 * is to properly render the character and not have it glitch through the object.
 * Attaches the specified object to the requested player.
 *
 * @author Kris | 1. apr 2018 : 3:54.16
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 *      profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status
 *      profile</a>}
 */
public final class AttachedPlayerObject implements GamePacketEncoder {
	private final int index;
	private final AttachedObject object;

	@Override
	public GamePacketOut encode() {
		final com.zenyte.game.packet.ServerProt prot = ServerProt.ATTACHED_PLAYER_OBJECT;
		final com.zenyte.network.io.RSBuffer buffer = new RSBuffer(prot);
		final com.zenyte.game.world.object.WorldObject obj = object.getObject();
		buffer.writeByteC(object.getMaxX());
		buffer.writeShortLE128(object.getEndTime());
		buffer.writeByteC(object.getMaxY());
		buffer.writeShort128(obj.getId());
		buffer.writeShort(object.getStartTime());
		buffer.writeByte128((obj.getType() << 2) | obj.getRotation());
		buffer.writeByte128(object.getMinY());
		buffer.writeShortLE(index);
		buffer.writeByteC(object.getMinX());
		buffer.writeByteC(((obj.getX() & 7) << 4) | (obj.getY() & 7));
		return new GamePacketOut(prot, buffer);
	}

	@Override
	public void log(@NotNull final Player player) {
		final com.zenyte.game.world.entity.player.Player targetPlayer = World.getPlayers().get(index);
		final com.zenyte.game.world.object.WorldObject obj = object.getObject();
		log(player, "Index: " + index + ", name: " + targetPlayer.getUsername() + ", id: " + obj.getId() + ", type: " + obj.getType() + ", rotation: " + obj.getRotation() + ", timeframe: " + object.getStartTime() + " - " + object.getEndTime() + ", width: " + object.getMinX() + " - " + object.getMaxX() + ", height: " + object.getMinY() + " - " + object.getMaxY());
	}

	@Override
	public LogLevel level() {
		return LogLevel.LOW_PACKET;
	}

	public AttachedPlayerObject(final int index, final AttachedObject object) {
		this.index = index;
		this.object = object;
	}
}
