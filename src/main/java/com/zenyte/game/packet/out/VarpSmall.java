package com.zenyte.game.packet.out;

import com.zenyte.game.packet.GamePacketEncoder;
import com.zenyte.game.packet.ServerProt;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.game.packet.GamePacketOut;
import com.zenyte.network.io.RSBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Tommeh | 28 jul. 2018 | 19:07:45
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class VarpSmall implements GamePacketEncoder {
	private int config;
	private int value;

	@Override
	public void log(@NotNull final Player player) {
		this.log(player, "Varp: " + config + ", value: " + value);
	}

	@Override
	public GamePacketOut encode() {
		final com.zenyte.game.packet.ServerProt prot = ServerProt.VARP_SMALL;
		final com.zenyte.network.io.RSBuffer buffer = new RSBuffer(prot);
		buffer.writeByte128(value);
		buffer.writeShort128(config);
		return new GamePacketOut(prot, buffer);
	}

	@Override
	public LogLevel level() {
		return LogLevel.LOW_PACKET;
	}

	public VarpSmall(final int config, final int value) {
		this.config = config;
		this.value = value;
	}
}
