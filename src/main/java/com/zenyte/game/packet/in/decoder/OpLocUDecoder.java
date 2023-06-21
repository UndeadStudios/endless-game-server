package com.zenyte.game.packet.in.decoder;

import com.zenyte.game.packet.ClientProtDecoder;
import com.zenyte.game.packet.in.event.OpLocUEvent;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.io.RSBuffer;

/**
 * @author Tommeh | 28 jul. 2018 | 19:49:43
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class OpLocUDecoder implements ClientProtDecoder<OpLocUEvent> {
	@Override
	public OpLocUEvent decode(Player player, int opcode, RSBuffer buffer) {
		final short y = buffer.readShortLE128();
		final short slotId = buffer.readShortLE();
		final boolean run = buffer.readByte128() == 1;
		final int compressed = buffer.readIntLE();
		final short x = buffer.readShortLE128();
		final int objectId = buffer.readShort() & 65535;
		final short itemId = buffer.readShortLE();
		final int interfaceId = compressed >> 16;
		final int componentId = compressed & 65535;
		return new OpLocUEvent(interfaceId, componentId, slotId, itemId, objectId, x, y, run);
	}
}
