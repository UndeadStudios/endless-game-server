package com.zenyte.game.util;

import com.zenyte.Game;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.region.XTEALoader;
import mgi.tools.jagcached.ArchiveType;
import mgi.tools.jagcached.cache.Group;
import mgi.utilities.ByteBuffer;

/**
 * @author Kris | 24/10/2018 12:22
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class MapPacker {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MapPacker.class);

    public static final void main(final String... args) throws Throwable {
        new MapPacker().repack();
    }

    private void repack() throws Throwable {
        Game.load();
        XTEALoader.load();
        final mgi.tools.jagcached.cache.Cache cache = Game.getCacheMgi();
        final mgi.tools.jagcached.cache.Archive archive = cache.getArchive(ArchiveType.MAPS);
        for (int x = 0; x < 16383; x += 64) {
            for (int y = 0; y < 16383; y += 64) {
                final int regionId = Location.getRegionId(x, y);
                if (regionId == 12342) {
                    continue;
                }
                System.err.println("Writing region: " + regionId);
                Group group = null;
                try {
                    final int[] xteas = XTEALoader.getXTEAKeys(regionId);
                    group = archive.findGroupByName("l" + (regionId >> 8) + "_" + (regionId & 255), xteas);
                    if (group == null) {
                        continue;
                    }
                    group.setXTEA(new int[4]);
                } catch (RuntimeException e) {
                    if (group == null) {
                        continue;
                    }
                    group.findFileByID(0).setData(new ByteBuffer(new byte[] {0}));
                    group.setXTEA(new int[4]);
                }
            }
        }
        Game.getCacheMgi().close();
    }
}
