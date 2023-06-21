package com.zenyte.game.packet.in.decoder;

import com.zenyte.game.packet.ClientProtDecoder;
import com.zenyte.game.packet.in.event.PingStatisticsEvent;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.io.RSBuffer;

/**
 * @author Kris | 1. apr 2018 : 22:05.58
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>}
 */
public class PingStatisticsDecoder implements ClientProtDecoder<PingStatisticsEvent> {
	@Override
	public PingStatisticsEvent decode(Player player, int opcode, RSBuffer buffer) {
		final long val1 = buffer.readInt() & 4294967295L;
		final long val2 = buffer.readInt() & 4294967295L;
		final byte gc = buffer.readByteC();
		final byte fps = buffer.readByte();
		final long ms = (val1 << 32) + val2;
		return new PingStatisticsEvent(gc, fps, ms);
	}
}
