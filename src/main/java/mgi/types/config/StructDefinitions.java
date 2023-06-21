package mgi.types.config;

import com.zenyte.Game;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mgi.tools.jagcached.ArchiveType;
import mgi.tools.jagcached.GroupType;
import mgi.tools.jagcached.cache.File;
import mgi.types.Definitions;
import mgi.utilities.ByteBuffer;

import java.util.Optional;

/**
 * @author Kris | 14/01/2019 00:39
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class StructDefinitions implements Definitions, Cloneable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StructDefinitions.class);
    private static StructDefinitions[] definitions;

    public static final StructDefinitions get(final int id) {
        if (id < 0 || id >= definitions.length) throw new IllegalArgumentException();
        return definitions[id];
    }

    private StructDefinitions(final int id, final ByteBuffer buffer) {
        this.id = id;
        decode(buffer);
    }

    private final int id;
    private Int2ObjectOpenHashMap<Object> parameters;

    @Override
    public void load() {
        final mgi.tools.jagcached.cache.Cache cache = Game.getCacheMgi();
        final mgi.tools.jagcached.cache.Archive configs = cache.getArchive(ArchiveType.CONFIGS);
        final mgi.tools.jagcached.cache.Group structs = configs.findGroupByID(GroupType.STRUCT);
        definitions = new StructDefinitions[structs.getHighestFileId()];
        for (int id = 0; id < structs.getHighestFileId(); id++) {
            final mgi.tools.jagcached.cache.File file = structs.findFileByID(id);
            if (file == null) {
                continue;
            }
            final mgi.utilities.ByteBuffer buffer = file.getData();
            if (buffer == null) {
                continue;
            }
            if (buffer.remaining() < 1) {
                continue;
            }
            definitions[id] = new StructDefinitions(id, buffer);
        }
    }

    public StructDefinitions clone() throws CloneNotSupportedException {
        return (StructDefinitions) super.clone();
    }

    @Override
    public void decode(final ByteBuffer buffer) {
        while (true) {
            final int opcode = buffer.readUnsignedByte();
            if (opcode == 0) {
                return;
            }
            decode(buffer, opcode);
        }
    }

    public final Optional<?> getValue(final int id) {
        return Optional.ofNullable(parameters.get(id));
    }

    @Override
    public void decode(final ByteBuffer buffer, int opcode) {
        if (opcode == 249) {
            parameters = buffer.readParameters();
            return;
        }
        throw new IllegalStateException("Opcode: " + opcode);
    }

    @Override
    public ByteBuffer encode() {
        final mgi.utilities.ByteBuffer buffer = new ByteBuffer(1132);
        if (parameters != null && !parameters.isEmpty()) {
            buffer.writeByte(249);
            buffer.writeParameters(parameters);
        }
        buffer.writeByte(0);
        return buffer;
    }

    @Override
    public void pack() {
        Game.getCacheMgi().getArchive(ArchiveType.CONFIGS).findGroupByID(GroupType.STRUCT).addFile(new File(id, encode()));
    }

    public StructDefinitions() {
        this.id = 0;
    }

    public int getId() {
        return this.id;
    }

    public Int2ObjectOpenHashMap<Object> getParameters() {
        return this.parameters;
    }

    public void setParameters(final Int2ObjectOpenHashMap<Object> parameters) {
        this.parameters = parameters;
    }
}
