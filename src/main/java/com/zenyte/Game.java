package com.zenyte;

import com.zenyte.cores.WorldThread;
import com.zenyte.game.packet.ClientProtDecoder;
import com.zenyte.game.packet.ClientProtLoader;
import com.zenyte.game.util.Huffman;
import mgi.tools.jagcached.cache.Cache;
import mgi.types.Definitions;

import java.nio.ByteBuffer;

/**
 * @author Tommeh | 28 jul. 2018 | 13:03:30
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class Game {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Game.class);
    private static mgi.tools.jagcached.cache.Cache cacheMgi;
    public static ByteBuffer checksumBuffer;
    public static ClientProtDecoder[] decoders = new ClientProtDecoder[256];
    public static int[] crc;

    public static final long getCurrentCycle() {
        return WorldThread.WORLD_CYCLE;
    }

    public static void load() {
        cacheMgi = Cache.openCache("./data/cache/");
        crc = cacheMgi.getCrcs();
        byte[] buffer = cacheMgi.generateInformationStoreDescriptor().getBuffer();
        checksumBuffer = ByteBuffer.allocateDirect(buffer.length);
        checksumBuffer.put(buffer);
        checksumBuffer.flip();
        Huffman.load();
        ClientProtLoader.load();
        for (final java.lang.Class<?> clazz : Definitions.HIGH_PRIORITY_DEFINITIONS) {
            Definitions.load(clazz).run();
        }
    }

    public static mgi.tools.jagcached.cache.Cache getCacheMgi() {
        return Game.cacheMgi;
    }

    public static void setCacheMgi(final mgi.tools.jagcached.cache.Cache cacheMgi) {
        Game.cacheMgi = cacheMgi;
    }

    public static ByteBuffer getChecksumBuffer() {
        return Game.checksumBuffer;
    }

    public static ClientProtDecoder[] getDecoders() {
        return Game.decoders;
    }
}
