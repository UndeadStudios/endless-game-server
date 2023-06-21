package com.zenyte.game.packet.in.decoder;

import com.zenyte.game.packet.ClientProtDecoder;
import com.zenyte.game.packet.in.event.OpNpcUEvent;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.io.RSBuffer;

/**
 * @author Tommeh | 28 jul. 2018 | 19:48:59
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class OpNpcUDecoder implements ClientProtDecoder<OpNpcUEvent> {
	@Override
	public OpNpcUEvent decode(Player player, int opcode, RSBuffer buffer) {
		final short itemId = buffer.readShortLE128();
		final short slotId = buffer.readShort();
		final short index = buffer.readShortLE128();
		final boolean run = buffer.readByteC() == 1;
		final int compressed = buffer.readInt();
		final int interfaceId = compressed >> 16;
		final int componentId = compressed & 65535;
		return new OpNpcUEvent(interfaceId, componentId, slotId, itemId, index, run);
	}
}
