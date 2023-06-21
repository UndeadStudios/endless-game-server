package com.zenyte.game.packet.out;

import com.zenyte.game.packet.GamePacketEncoder;
import com.zenyte.game.packet.ServerProt;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.region.DynamicRegion;
import com.zenyte.game.world.region.XTEALoader;
import com.zenyte.network.game.packet.GamePacketOut;
import com.zenyte.network.io.BitBuffer;
import com.zenyte.network.io.RSBuffer;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import org.jetbrains.annotations.NotNull;

/**
 * @author Tommeh | 28 jul. 2018 | 19:10:26
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class RebuildRegion implements GamePacketEncoder {
	private final Player player;
	private static final int BITBUFFER_LENGTH = (int) Math.ceil(13.0F * 13.0F * 4.0F * 27.0F / 8.0F);

	@Override
	public void log(@NotNull final Player player) {
		this.log(player, "Tile: x: " + player.getX() + ", y: " + player.getY() + ", z: " + player.getPlane());
	}

	@Override
	public boolean prioritized() {
		return true;
	}

	@Override
	public GamePacketOut encode() {
		final com.zenyte.game.packet.ServerProt prot = ServerProt.REBUILD_REGION;
		final com.zenyte.network.io.RSBuffer buffer = new RSBuffer(prot);
		final int centerChunkX = player.getLocation().getChunkX();
		final int centerChunkY = player.getLocation().getChunkY();
		buffer.writeShort128(centerChunkY);
		buffer.writeShortLE128(centerChunkX);
		buffer.writeByte(player.isForceReloadMap() ? 1 : 0);
		final it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet regions = new IntLinkedOpenHashSet();
		final com.zenyte.network.io.BitBuffer bitBuffer = new BitBuffer(255, BITBUFFER_LENGTH);
		for (int plane = 0; plane < 4; plane++) {
			for (int chunkX = centerChunkX - 6; chunkX <= centerChunkX + 6; chunkX++) {
				// calcs
				for (int chunkY = centerChunkY - 6; chunkY <= centerChunkY + 6; chunkY++) {
					// calcs
					final int regionId = ((chunkX >> 3) << 8) + (chunkY >> 3);
					final com.zenyte.game.world.region.Region region = World.getRegions().get(regionId);
					int displayedChunkX = chunkX;
					int displayedChunkY = chunkY;
					int displayedPlane = plane;
					int rotation = 0;
					if (region instanceof DynamicRegion) {
						final com.zenyte.game.world.region.DynamicRegion dynamicRegion = (DynamicRegion) region;
						final int hash = dynamicRegion.getLocationHash(chunkX - ((chunkX >> 3) << 3), chunkY - ((chunkY >> 3) << 3), plane);
						displayedChunkX = hash & 2047;
						displayedChunkY = hash >> 11 & 2047;
						displayedPlane = hash >> 22 & 3;
						rotation = hash >> 24 & 3;
					}
					if (displayedChunkX == 0 && displayedChunkY == 0) {
						bitBuffer.write(1, 0);
					} else {
						bitBuffer.write(1, 1);
						bitBuffer.write(26, rotation << 1 | displayedPlane << 24 | displayedChunkX << 14 | displayedChunkY << 3);
						regions.add(((displayedChunkX >> 3) << 8) + (displayedChunkY >> 3));
					}
				}
			}
		}
		buffer.writeShort(regions.size());
		buffer.writeBits(bitBuffer);
		for (final java.lang.Integer region : regions) {
			for (int xtea : XTEALoader.getXTEAs(region)) {
				buffer.writeInt(xtea);
			}
		}
		return new GamePacketOut(prot, buffer);
	}

	@Override
	public LogLevel level() {
		return LogLevel.LOW_PACKET;
	}

	public RebuildRegion(final Player player) {
		this.player = player;
	}
}
