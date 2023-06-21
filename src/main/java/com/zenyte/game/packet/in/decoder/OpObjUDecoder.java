package com.zenyte.game.packet.in.decoder;

import com.zenyte.game.packet.ClientProtDecoder;
import com.zenyte.game.packet.in.event.OpObjUEvent;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.io.RSBuffer;

/**
 * @author Kris | 1. apr 2018 : 21:15.54
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>}
 */
public final class OpObjUDecoder implements ClientProtDecoder<OpObjUEvent> {
	@Override
	public OpObjUEvent decode(Player player, int opcode, RSBuffer buffer) {
		final int compressed = buffer.readIntV1();
		final short itemId = buffer.readShortLE128();
		final boolean run = buffer.readByte128() == 1;
		final short slotId = buffer.readShortLE();
		final short floorItemId = buffer.readShortLE128();
		final short x = buffer.readShortLE128();
		final short y = buffer.readShortLE();
		final int interfaceId = compressed >> 16;
		final int componentId = compressed & 65535;
		return new OpObjUEvent(interfaceId, componentId, slotId, itemId, floorItemId, x, y, run);
	}
}
