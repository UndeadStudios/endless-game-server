package com.zenyte.game.packet.out;

import com.zenyte.game.packet.GamePacketEncoder;
import com.zenyte.game.packet.ServerProt;
import com.zenyte.game.util.TextUtils;
import com.zenyte.game.world.entity.masks.ChatMessage;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.network.game.packet.GamePacketOut;
import com.zenyte.network.io.RSBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Tommeh | 28 jul. 2018 | 18:33:11
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class MessageClanChannel implements GamePacketEncoder {
	private final Player sender;
	private final String channelName;
	private final int icon;
	private final ChatMessage message;

	@Override
	public void log(@NotNull final Player player) {
		log(player, "Channel: " + channelName + ", icon: " + icon + ", sender: " + sender.getUsername() + ", message: " + message.getChatText());
	}

	@Override
	public GamePacketOut encode() {
		final com.zenyte.game.packet.ServerProt prot = ServerProt.MESSAGE_CLANCHANNEL;
		final com.zenyte.network.io.RSBuffer buffer = new RSBuffer(prot);
		final java.lang.String senderName = sender.getPlayerInformation().getDisplayname();
		final int message_uid = sender.getSocialManager().getNextUniqueId();
		buffer.writeString(senderName);
		buffer.writeLong(TextUtils.stringToLong(channelName));
		buffer.writeShort(1);
		buffer.write24BitInteger(message_uid);
		buffer.writeShort(icon);
		buffer.writeBytes(message.getCompressedArray(), 0, message.getOffset());
		return new GamePacketOut(prot, buffer);
	}

	@Override
	public LogLevel level() {
		return LogLevel.HIGH_PACKET;
	}

	public MessageClanChannel(final Player sender, final String channelName, final int icon, final ChatMessage message) {
		this.sender = sender;
		this.channelName = channelName;
		this.icon = icon;
		this.message = message;
	}
}
