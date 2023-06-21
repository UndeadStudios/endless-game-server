package mgi.custom;

import com.zenyte.Game;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mgi.tools.jagcached.ArchiveType;
import mgi.tools.jagcached.cache.File;
import mgi.tools.jagcached.cache.Group;
import mgi.utilities.ByteBuffer;

/**
 * @author Kris | 28/11/2019
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class FramePacker {
    private static final Int2ObjectMap<byte[]> frames = new Int2ObjectOpenHashMap<>();

    public static final void add(final int file, final byte[] frames) {
        FramePacker.frames.put(file, frames);
    }

    public static final void write() {
        final mgi.tools.jagcached.cache.Cache cache = Game.getCacheMgi();
        final mgi.tools.jagcached.cache.Archive archive = cache.getArchive(ArchiveType.SKELETONS);
        final it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap<byte[]> sortedMap = new Int2ObjectAVLTreeMap<byte[]>(frames);
        for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<byte[]> entry : sortedMap.int2ObjectEntrySet()) {
            final int key = entry.getIntKey();
            final byte[] bytes = entry.getValue();
            final mgi.tools.jagcached.cache.File file = new File(key & 65535, new ByteBuffer(bytes));
            final mgi.tools.jagcached.cache.Group group = archive.findGroupByID(key >> 16);
            if (group == null) {
                archive.addGroup(new Group(key >> 16, file));
            } else {
                group.addFile(file);
            }
        }
    }
}
