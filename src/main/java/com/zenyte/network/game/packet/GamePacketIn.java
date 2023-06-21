package com.zenyte.network.game.packet;

import com.zenyte.network.PacketIn;
import com.zenyte.network.io.RSBuffer;

/**
 * @author Tommeh | 28 jul. 2018 | 12:39:39
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class GamePacketIn implements PacketIn {
	private int opcode;
	private RSBuffer buffer;

	public int getOpcode() {
		return this.opcode;
	}

	public RSBuffer getBuffer() {
		return this.buffer;
	}

	public GamePacketIn(final int opcode, final RSBuffer buffer) {
		this.opcode = opcode;
		this.buffer = buffer;
	}
}
