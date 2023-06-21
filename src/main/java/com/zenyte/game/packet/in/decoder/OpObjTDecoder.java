package com.zenyte.game.packet.in.decoder;

import com.zenyte.game.packet.ClientProtDecoder;
import com.zenyte.game.packet.in.event.InterfaceOnFloorItemEvent;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.io.RSBuffer;

/**
 * @author Kris | 1. apr 2018 : 21:15.54
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>}
 */
public final class OpObjTDecoder implements ClientProtDecoder<InterfaceOnFloorItemEvent> {
	@Override
	public InterfaceOnFloorItemEvent decode(Player player, int opcode, RSBuffer buffer) {
		final int compressed = buffer.readIntV1();
		buffer.readShort();
		final short y = buffer.readShort128();
		final short x = buffer.readShort128();
		buffer.readByte128();
		final short itemId = buffer.readShortLE();
		final int interfaceId = compressed >> 16;
		final int componentId = compressed & 65535;
		return new InterfaceOnFloorItemEvent(interfaceId, componentId, itemId, x, y);
	}
}
