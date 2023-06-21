package com.zenyte.game.packet.in.decoder;

import com.zenyte.game.packet.ClientProtDecoder;
import com.zenyte.game.packet.in.event.BugReportEvent;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.io.RSBuffer;

/**
 * @author Kris | 1. apr 2018 : 22:57.16
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>}
 */
public final class BugReportDecoder implements ClientProtDecoder<BugReportEvent> {
	@Override
	public BugReportEvent decode(Player player, int opcode, RSBuffer buffer) {
		final java.lang.String instructions = buffer.readString();
		final byte bit = buffer.read128Byte();
		final java.lang.String description = buffer.readString();
		return new BugReportEvent(instructions, description, bit);
	}
}
