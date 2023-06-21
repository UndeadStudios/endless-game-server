package com.zenyte.game.packet.out;

import com.zenyte.game.packet.GamePacketEncoder;
import com.zenyte.game.packet.ServerProt;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.game.packet.GamePacketOut;
import com.zenyte.network.io.RSBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Tommeh | 28 jul. 2018 | 18:22:23
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class IfSetModel implements GamePacketEncoder {
	private int interfaceId;
	private int componentId;
	private int modelId;

	@Override
	public void log(@NotNull final Player player) {
		log(player, "Interface: " + interfaceId + ", component: " + componentId + ", model: " + modelId);
	}

	@Override
	public GamePacketOut encode() {
		final com.zenyte.game.packet.ServerProt prot = ServerProt.IF_SETMODEL;
		final com.zenyte.network.io.RSBuffer buffer = new RSBuffer(prot);
		buffer.writeIntV2(interfaceId << 16 | componentId);
		buffer.writeShortLE128(modelId);
		return new GamePacketOut(prot, buffer);
	}

	@Override
	public LogLevel level() {
		return LogLevel.LOW_PACKET;
	}

	public IfSetModel(final int interfaceId, final int componentId, final int modelId) {
		this.interfaceId = interfaceId;
		this.componentId = componentId;
		this.modelId = modelId;
	}
}
