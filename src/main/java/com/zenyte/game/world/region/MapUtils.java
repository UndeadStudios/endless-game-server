package com.zenyte.game.world.region;

import com.google.common.base.Preconditions;
import com.zenyte.Game;
import com.zenyte.game.world.object.WorldObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import mgi.tools.jagcached.ArchiveType;
import mgi.utilities.ByteBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Kris | 03/04/2019 14:20
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class MapUtils {
    public static final ByteBuffer encode(@NotNull final Collection<WorldObject> objects) {
        final mgi.utilities.ByteBuffer buffer = new ByteBuffer(1024 * 10 * 10);
        final it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap<java.util.List<com.zenyte.game.world.object.WorldObject>> map = new Int2ObjectLinkedOpenHashMap<List<WorldObject>>();
        final java.util.ArrayList<com.zenyte.game.world.object.WorldObject> list = new ArrayList<WorldObject>(objects);
        list.sort((c1, c2) -> {
            final int c1id = c1.getId();
            final int c2id = c2.getId();
            if (c1id == c2id) {
                return Integer.compare(c1.hashInRegion(), c2.hashInRegion());
            }
            return Integer.compare(c1id, c2id);
        });
        for (final com.zenyte.game.world.object.WorldObject o : list) {
            map.computeIfAbsent(o.getId(), i -> new ArrayList<>()).add(o);
        }
        int lastId = -1;
        int lastHash = 0;
        for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<java.util.List<com.zenyte.game.world.object.WorldObject>> entry : map.int2ObjectEntrySet()) {
            buffer.writeHugeSmart(entry.getIntKey() - lastId);
            lastId = entry.getIntKey();
            for (final com.zenyte.game.world.object.WorldObject value : entry.getValue()) {
                final int x = value.getX() & 63;
                final int y = value.getY() & 63;
                final int z = value.getPlane() & 3;
                final int hash = (x << 6) | (y) | (z << 12);
                buffer.writeSmart(1 + hash - lastHash);
                lastHash = hash;
                buffer.writeByte(value.getRotation() | (value.getType() << 2));
            }
            lastHash = 0;
            buffer.writeHugeSmart(0);
        }
        buffer.writeHugeSmart(0);
        return buffer;
    }

    public static final Collection<WorldObject> decode(@NotNull final ByteBuffer buffer) {
        final java.util.ArrayList<com.zenyte.game.world.object.WorldObject> collection = new ArrayList<WorldObject>();
        int objectId = -1;
        int incr;
        buffer.setPosition(0);
        while ((incr = buffer.readHugeSmart()) != 0) {
            objectId += incr;
            int location = 0;
            int incr2;
            while ((incr2 = buffer.readUnsignedSmart()) != 0) {
                location += incr2 - 1;
                final int localX = (location >> 6 & 63);
                final int localY = (location & 63);
                final int plane = location >> 12;
                final int objectData = buffer.readUnsignedByte();
                final int type = objectData >> 2;
                final int rotation = objectData & 3;
                collection.add(new WorldObject(objectId, type, rotation, localX, localY, plane));
            }
        }
        return collection;
    }


    public static class Tile {
        private final int x;
        private final int y;
        private final int z;
        public Integer height;
        public byte settings;
        public byte overlayId;
        public byte overlayPath;
        public byte overlayRotation;
        public boolean forceOverlay;
        public byte underlayId;

        public Tile(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getZ() {
            return this.z;
        }

        public Integer getHeight() {
            return this.height;
        }

        public byte getSettings() {
            return this.settings;
        }

        public byte getOverlayId() {
            return this.overlayId;
        }

        public byte getOverlayPath() {
            return this.overlayPath;
        }

        public byte getOverlayRotation() {
            return this.overlayRotation;
        }

        public boolean isForceOverlay() {
            return this.forceOverlay;
        }

        public byte getUnderlayId() {
            return this.underlayId;
        }

        public void setHeight(final Integer height) {
            this.height = height;
        }

        public void setSettings(final byte settings) {
            this.settings = settings;
        }

        public void setOverlayId(final byte overlayId) {
            this.overlayId = overlayId;
        }

        public void setOverlayPath(final byte overlayPath) {
            this.overlayPath = overlayPath;
        }

        public void setOverlayRotation(final byte overlayRotation) {
            this.overlayRotation = overlayRotation;
        }

        public void setForceOverlay(final boolean forceOverlay) {
            this.forceOverlay = forceOverlay;
        }

        public void setUnderlayId(final byte underlayId) {
            this.underlayId = underlayId;
        }

        @Override
        public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
            if (o == this) return true;
            if (!(o instanceof MapUtils.Tile)) return false;
            final MapUtils.Tile other = (MapUtils.Tile) o;
            if (!other.canEqual((Object) this)) return false;
            if (this.getX() != other.getX()) return false;
            if (this.getY() != other.getY()) return false;
            if (this.getZ() != other.getZ()) return false;
            if (this.getSettings() != other.getSettings()) return false;
            if (this.getOverlayId() != other.getOverlayId()) return false;
            if (this.getOverlayPath() != other.getOverlayPath()) return false;
            if (this.getOverlayRotation() != other.getOverlayRotation()) return false;
            if (this.isForceOverlay() != other.isForceOverlay()) return false;
            if (this.getUnderlayId() != other.getUnderlayId()) return false;
            final Object this$height = this.getHeight();
            final Object other$height = other.getHeight();
            if (this$height == null ? other$height != null : !this$height.equals(other$height)) return false;
            return true;
        }

        protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
            return other instanceof MapUtils.Tile;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.getX();
            result = result * PRIME + this.getY();
            result = result * PRIME + this.getZ();
            result = result * PRIME + this.getSettings();
            result = result * PRIME + this.getOverlayId();
            result = result * PRIME + this.getOverlayPath();
            result = result * PRIME + this.getOverlayRotation();
            result = result * PRIME + (this.isForceOverlay() ? 79 : 97);
            result = result * PRIME + this.getUnderlayId();
            final Object $height = this.getHeight();
            result = result * PRIME + ($height == null ? 43 : $height.hashCode());
            return result;
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public String toString() {
            return "MapUtils.Tile(x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ", height=" + this.getHeight() + ", settings=" + this.getSettings() + ", overlayId=" + this.getOverlayId() + ", overlayPath=" + this.getOverlayPath() + ", overlayRotation=" + this.getOverlayRotation() + ", forceOverlay=" + this.isForceOverlay() + ", underlayId=" + this.getUnderlayId() + ")";
        }
    }

    public static ByteBuffer processTiles(@NotNull final ByteBuffer buffer, @NotNull final Consumer<Tile> consumer) {
        Tile[][][] tiles = new Tile[4][64][64];
        for (int z = 0; z < 4; z++) {
            for (int x = 0; x < 64; x++) {
                for (int y = 0; y < 64; y++) {
                    Tile tile = tiles[z][x][y] = new Tile(x, y, z);
                    while (true) {
                        int attribute = buffer.readUnsignedByte();
                        if (attribute == 0) {
                            break;
                        } else if (attribute == 1) {
                            int height = buffer.readUnsignedByte();
                            tile.height = height;
                            break;
                        } else if (attribute <= 49) {
                            //tile.attrOpcode = attribute;
                            tile.overlayId = buffer.readByte();
                            tile.overlayPath = (byte) ((attribute - 2) / 4);
                            tile.overlayRotation = (byte) (attribute - 2 & 3);
                        } else if (attribute <= 81) {
                            tile.settings = (byte) (attribute - 49);
                        } else {
                            tile.underlayId = (byte) (attribute - 81);
                        }
                    }
                }
            }
        }
        for (int z = 0; z < 4; z++) {
            for (int x = 0; x < 64; x++) {
                for (int y = 0; y < 64; y++) {
                    consumer.accept(tiles[z][x][y]);
                }
            }
        }
        ByteBuffer buf = new ByteBuffer(1024 * 10 * 10);
        for (int z = 0; z < 4; z++) {
            for (int x = 0; x < 64; x++) {
                for (int y = 0; y < 64; y++) {
                    final com.zenyte.game.world.region.MapUtils.Tile tile = tiles[z][x][y];
                    if (tile.overlayId != 0 || tile.forceOverlay) {
                        final int opcode = 2 + ((tile.overlayRotation & 3) | (tile.overlayPath << 2));
                        buf.writeByte(opcode);
                        buf.writeByte(tile.overlayId);
                    }
                    if (tile.settings != 0) {
                        buf.writeByte(tile.settings + 49);
                    }
                    if (tile.underlayId != 0) {
                        buf.writeByte(tile.underlayId + 81);
                    }
                    if (tile.height != null) {
                        buf.writeByte(1);
                        buf.writeByte(tile.height.intValue());
                        continue;
                    }
                    buf.writeByte(0);
                }
            }
        }
        return buf;
    }

    public static final byte[] inject(final byte[] buffer, @Nullable final Predicate<WorldObject> filter, @NotNull final WorldObject... objects) {
        try {
            final mgi.utilities.ByteBuffer landBuffer = new ByteBuffer(buffer);
            final java.util.Collection<com.zenyte.game.world.object.WorldObject> mapObjects = decode(landBuffer);
            mapObjects.removeIf(obj -> {
                final int mapObjHash = obj.hashInRegion();
                final int mapObjSlot = Region.OBJECT_SLOTS[obj.getType()];
                for (final com.zenyte.game.world.object.WorldObject o : objects) {
                    final int hash = o.hashInRegion();
                    final int slot = Region.OBJECT_SLOTS[o.getType()];
                    if (hash == mapObjHash && slot == mapObjSlot) {
                        return true;
                    }
                }
                return false;
            });
            if (filter != null) {
                mapObjects.removeIf(filter);
            }
            mapObjects.addAll(Arrays.asList(objects));
            final mgi.utilities.ByteBuffer encoded = encode(mapObjects);
            final mgi.utilities.ByteBuffer newBuffer = new ByteBuffer(encoded.getBuffer());
            return newBuffer.getBuffer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final byte[] inject(final int regionId, @Nullable final Predicate<WorldObject> filter, @NotNull final WorldObject... objects) {
        try {
            final int[] xteas = XTEALoader.getXTEAs(regionId);
            final mgi.tools.jagcached.cache.Cache cache = Game.getCacheMgi();
            final mgi.tools.jagcached.cache.Archive archive = cache.getArchive(ArchiveType.MAPS);
            final mgi.tools.jagcached.cache.Group landGroup = archive.findGroupByName("l" + (regionId >> 8) + "_" + (regionId & 255), xteas);
            final int locFileId = landGroup == null ? -1 : landGroup.getID();
            Preconditions.checkArgument(locFileId != -1);
            final mgi.utilities.ByteBuffer landBuffer = landGroup == null ? null : landGroup.findFileByID(0).getData();
            final java.util.Collection<com.zenyte.game.world.object.WorldObject> mapObjects = decode(landBuffer);
            mapObjects.removeIf(obj -> {
                final int mapObjHash = ((obj.getX() & 63) << 6) | (obj.getY() & 63) | ((obj.getPlane() & 3) << 12);
                final int mapObjSlot = Region.OBJECT_SLOTS[obj.getType()];
                for (final com.zenyte.game.world.object.WorldObject o : objects) {
                    final int hash = ((o.getX() & 63) << 6) | (o.getY() & 63) | ((o.getPlane() & 3) << 12);
                    final int slot = Region.OBJECT_SLOTS[o.getType()];
                    if (hash == mapObjHash && slot == mapObjSlot) {
                        return true;
                    }
                }
                return false;
            });
            if (filter != null) {
                mapObjects.removeIf(filter);
            }
            mapObjects.addAll(Arrays.asList(objects));
            final mgi.utilities.ByteBuffer encoded = encode(mapObjects);
            final mgi.utilities.ByteBuffer newBuffer = new ByteBuffer(encoded.getBuffer());
            return newBuffer.getBuffer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
