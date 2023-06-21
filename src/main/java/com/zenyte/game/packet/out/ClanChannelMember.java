package com.zenyte.game.packet.out;

import com.zenyte.game.content.clans.ClanChannelBuilder;
import com.zenyte.game.packet.GamePacketEncoder;
import com.zenyte.game.packet.ServerProt;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.game.packet.GamePacketOut;
import com.zenyte.network.io.RSBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Tommeh | 28 jul. 2018 | 18:10:44
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
 */
public class ClanChannelMember implements GamePacketEncoder {
	private final ClanChannelBuilder builder;

	@Override
	public void log(@NotNull final Player player) {
		final com.zenyte.game.content.clans.ClanChannel channel = builder.getChannel();
		this.log(player, "Channel owner: " + channel.getOwner() + ", prefix: " + channel.getPrefix() + ", members: " + channel.getMembers().size());
	}

	@Override
	public GamePacketOut encode() {
		final com.zenyte.game.packet.ServerProt prot = ServerProt.CLANCHANNEL_MEMBER;
		final com.zenyte.network.io.RSBuffer buffer = new RSBuffer(prot);
		buffer.writeBytes(builder.getBuffer().resetReaderIndex());
		return new GamePacketOut(prot, buffer);
	}

	@Override
	public LogLevel level() {
		return LogLevel.LOW_PACKET;
	}

	public ClanChannelMember(final ClanChannelBuilder builder) {
		this.builder = builder;
	}
}
