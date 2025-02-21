package mgi.tools.dumpers;

import mgi.tools.jagcached.ArchiveType;
import mgi.tools.jagcached.cache.Cache;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Tommeh | 11/02/2020 | 11:12
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
 */
public class ScriptDumper {
    public static void main(String[] args) throws IOException {
        File directory = new File("dumps/cs2/");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        final mgi.tools.jagcached.cache.Cache cache = Cache.openCache("./data/cache/");
        final int id = 10033;
        final mgi.tools.jagcached.cache.Archive archive = cache.getArchive(ArchiveType.CLIENTSCRIPTS);
        final mgi.tools.jagcached.cache.Group group = archive.findGroupByID(id);
        if (group == null) {
            System.err.println("Script doesn\'t exist!");
            return;
        }
        final java.io.DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(directory, id + ".cs2")));
        dos.write(group.findFileByID(0).getData().getBuffer());
    }
}
