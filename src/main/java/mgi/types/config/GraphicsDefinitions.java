package mgi.types.config;

import com.zenyte.Game;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import mgi.tools.jagcached.ArchiveType;
import mgi.tools.jagcached.GroupType;
import mgi.tools.jagcached.cache.Cache;
import mgi.tools.jagcached.cache.File;
import mgi.types.Definitions;
import mgi.utilities.ByteBuffer;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Kris | 6. apr 2018 : 21:12.37
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 * profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status
 * profile</a>}
 */
public final class GraphicsDefinitions implements Definitions {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GraphicsDefinitions.class);
    public static GraphicsDefinitions[] definitions;
    private int id;
    private int modelId;
    private int animationId;
    private int resizeX;
    private int resizeY;
    private int rotation;
    private int ambience;
    private int contrast;
    private short[] originalColours;
    private short[] retextureToFind;
    private short[] replacementColours;
    private short[] retextureToReplace;

    public GraphicsDefinitions(final int id, final ByteBuffer buffer) {
        this.id = id;
        setDefaults();
        decode(buffer);
    }

    public static final GraphicsDefinitions get(final int id) {
        if (id < 0 || id >= definitions.length) {
            return null;
        }
        return definitions[id];
    }

    public static final void printGraphicsDifferences(final Cache cache, final Cache cacheToCompareWith) {
        final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<byte[]> currentAnimations = getAnimations(cache);
        final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<byte[]> animations = getAnimations(cacheToCompareWith);
        it.unimi.dsi.fastutil.objects.ObjectIterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<byte[]>> iterator = currentAnimations.int2ObjectEntrySet().iterator();
        final it.unimi.dsi.fastutil.ints.IntArrayList list = new IntArrayList();
        while (iterator.hasNext()) {
            final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<byte[]> next = iterator.next();
            final int id = next.getIntKey();
            final byte[] bytes = next.getValue();
            final byte[] otherBytes = animations.get(id);
            if (!Arrays.equals(bytes, otherBytes)) {
                list.add(id);
            }
        }
        iterator = animations.int2ObjectEntrySet().iterator();
        while (iterator.hasNext()) {
            final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<byte[]> next = iterator.next();
            final int id = next.getIntKey();
            final byte[] bytes = next.getValue();
            final byte[] otherBytes = currentAnimations.get(id);
            if (otherBytes == null || !Arrays.equals(bytes, otherBytes)) {
                if (!list.contains(id)) list.add(id);
            }
        }
        Collections.sort(list);
        for (int id : list) {
            System.err.println("Graphics difference: " + id);
        }
        System.err.println("Graphics difference checking complete!");
    }

    private static final Int2ObjectOpenHashMap<byte[]> getAnimations(final Cache cache) {
        final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<byte[]> map = new Int2ObjectOpenHashMap<byte[]>();
        try {
            final mgi.tools.jagcached.cache.Archive configs = cache.getArchive(ArchiveType.CONFIGS);
            final mgi.tools.jagcached.cache.Group graphics = configs.findGroupByID(GroupType.SPOTANIM);
            for (int id = 0; id < graphics.getHighestFileId(); id++) {
                final mgi.tools.jagcached.cache.File file = graphics.findFileByID(id);
                if (file == null) {
                    continue;
                }
                final mgi.utilities.ByteBuffer buffer = file.getData();
                if (buffer == null) {
                    continue;
                }
                map.put(id, buffer.getBuffer());
            }
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
        return map;
    }

    @Override
    public void load() {
        final mgi.tools.jagcached.cache.Cache cache = Game.getCacheMgi();
        final mgi.tools.jagcached.cache.Archive configs = cache.getArchive(ArchiveType.CONFIGS);
        final mgi.tools.jagcached.cache.Group graphics = configs.findGroupByID(GroupType.SPOTANIM);
        definitions = new GraphicsDefinitions[graphics.getHighestFileId()];
        for (int id = 0; id < graphics.getHighestFileId(); id++) {
            final mgi.tools.jagcached.cache.File file = graphics.findFileByID(id);
            if (file == null) {
                continue;
            }
            final mgi.utilities.ByteBuffer buffer = file.getData();
            if (buffer == null) {
                continue;
            }
            definitions[id] = new GraphicsDefinitions(id, buffer);
        }
    }

    private void setDefaults() {
        animationId = -1;
        resizeX = 128;
        resizeY = 128;
        rotation = 0;
        ambience = 0;
        contrast = 0;
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

    @Override
    public void decode(final ByteBuffer buffer, final int opcode) {
        switch (opcode) {
        case 1: 
            modelId = buffer.readUnsignedShort();
            return;
        case 2: 
            animationId = buffer.readUnsignedShort();
            return;
        case 4: 
            resizeX = buffer.readUnsignedShort();
            return;
        case 5: 
            resizeY = buffer.readUnsignedShort();
            return;
        case 6: 
            rotation = buffer.readUnsignedShort();
            return;
        case 7: 
            ambience = buffer.readUnsignedByte();
            return;
        case 8: 
            contrast = buffer.readUnsignedByte();
            return;
        case 40: 
            {
                final int length = buffer.readUnsignedByte();
                originalColours = new short[length];
                replacementColours = new short[length];
                for (int index = 0; index < length; ++index) {
                    originalColours[index] = (short) buffer.readUnsignedShort();
                    replacementColours[index] = (short) buffer.readUnsignedShort();
                }
                return;
            }
        case 41: 
            {
                final int length = buffer.readUnsignedByte();
                retextureToFind = new short[length];
                retextureToReplace = new short[length];
                for (int index = 0; index < length; ++index) {
                    retextureToFind[index] = (short) buffer.readUnsignedShort();
                    retextureToReplace[index] = (short) buffer.readUnsignedShort();
                }
            }
            return;
        }
    }

    @Override
    public ByteBuffer encode() {
        final mgi.utilities.ByteBuffer buffer = new ByteBuffer(128);
        if (modelId != -1) {
            buffer.writeByte(1);
            buffer.writeShort(modelId);
        }
        if (animationId != -1) {
            buffer.writeByte(2);
            buffer.writeShort(animationId);
        }
        if (resizeX != 0) {
            buffer.writeByte(4);
            buffer.writeShort(resizeX);
        }
        if (resizeY != 0) {
            buffer.writeByte(5);
            buffer.writeShort(resizeY);
        }
        if (rotation != -1) {
            buffer.writeByte(6);
            buffer.writeShort(rotation);
        }
        if (ambience != -1) {
            buffer.writeByte(7);
            buffer.writeByte(ambience);
        }
        if (contrast != -1) {
            buffer.writeByte(8);
            buffer.writeByte(contrast);
        }
        if (originalColours != null && originalColours.length > 0) {
            buffer.writeByte(40);
            buffer.writeByte(originalColours.length);
            for (int index = 0; index < originalColours.length; index++) {
                buffer.writeShort(originalColours[index]);
                buffer.writeShort(replacementColours[index]);
            }
        }
        if (retextureToFind != null && retextureToFind.length > 0) {
            buffer.writeByte(41);
            buffer.writeByte(retextureToFind.length);
            for (int index = 0; index < retextureToFind.length; index++) {
                buffer.writeShort(retextureToFind[index]);
                buffer.writeShort(retextureToReplace[index]);
            }
        }
        buffer.writeByte(0);
        return buffer;
    }

    @Override
    public void pack() {
        Game.getCacheMgi().getArchive(ArchiveType.CONFIGS).findGroupByID(GroupType.SPOTANIM).addFile(new File(id, encode()));
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "GraphicsDefinitions(id=" + this.getId() + ", modelId=" + this.getModelId() + ", animationId=" + this.getAnimationId() + ", resizeX=" + this.getResizeX() + ", resizeY=" + this.getResizeY() + ", rotation=" + this.getRotation() + ", ambience=" + this.getAmbience() + ", contrast=" + this.getContrast() + ", originalColours=" + java.util.Arrays.toString(this.getOriginalColours()) + ", retextureToFind=" + java.util.Arrays.toString(this.getRetextureToFind()) + ", replacementColours=" + java.util.Arrays.toString(this.getReplacementColours()) + ", retextureToReplace=" + java.util.Arrays.toString(this.getRetextureToReplace()) + ")";
    }

    public GraphicsDefinitions() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getModelId() {
        return this.modelId;
    }

    public int getAnimationId() {
        return this.animationId;
    }

    public int getResizeX() {
        return this.resizeX;
    }

    public int getResizeY() {
        return this.resizeY;
    }

    public int getRotation() {
        return this.rotation;
    }

    public int getAmbience() {
        return this.ambience;
    }

    public int getContrast() {
        return this.contrast;
    }

    public void setModelId(final int modelId) {
        this.modelId = modelId;
    }

    public void setAnimationId(final int animationId) {
        this.animationId = animationId;
    }

    public void setResizeX(final int resizeX) {
        this.resizeX = resizeX;
    }

    public void setResizeY(final int resizeY) {
        this.resizeY = resizeY;
    }

    public void setRotation(final int rotation) {
        this.rotation = rotation;
    }

    public void setAmbience(final int ambience) {
        this.ambience = ambience;
    }

    public void setContrast(final int contrast) {
        this.contrast = contrast;
    }

    public short[] getOriginalColours() {
        return this.originalColours;
    }

    public short[] getRetextureToFind() {
        return this.retextureToFind;
    }

    public short[] getReplacementColours() {
        return this.replacementColours;
    }

    public short[] getRetextureToReplace() {
        return this.retextureToReplace;
    }

    public void setOriginalColours(final short[] originalColours) {
        this.originalColours = originalColours;
    }

    public void setRetextureToFind(final short[] retextureToFind) {
        this.retextureToFind = retextureToFind;
    }

    public void setReplacementColours(final short[] replacementColours) {
        this.replacementColours = replacementColours;
    }

    public void setRetextureToReplace(final short[] retextureToReplace) {
        this.retextureToReplace = retextureToReplace;
    }
}
