package mgi.types.config;

import com.zenyte.Game;
import com.zenyte.game.util.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import mgi.Indice;
import mgi.tools.jagcached.ArchiveType;
import mgi.tools.jagcached.GroupType;
import mgi.tools.jagcached.cache.Cache;
import mgi.tools.jagcached.cache.File;
import mgi.types.Definitions;
import mgi.utilities.ByteBuffer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AnimationDefinitions implements Definitions, Cloneable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnimationDefinitions.class);
    public static AnimationDefinitions[] definitions;
    /**
     * The id of the animation.
     */
    private int id;
    private int precedenceAnimating;
    /**
     * An array of frame ids. The value is a bitpacked number, with bits past 16 being the skeleton id.
     */
    private int[] frameIds;
    private int[] mergedBoneGroups;
    /**
     * Animation priority level.
     */
    private int priority;
    private int frameStep;
    /**
     * The length of each frame, with one value being equal to one actual frame, capping at 20 milliseconds (1 second / 50 FPS)
     */
    private int[] frameLengths;
    private boolean stretches;
    private int[] extraFrameIds;
    /**
     * The id of the item held in the left hand. If the id is 0, the helf left hand item is not displayed by the client.
     */
    private int leftHandItem;
    private int forcedPriority;
    /**
     * The id of the item held in the right hand. If the id is 0, the held right hand item is not displayed by the
     * client.
     */
    private int rightHandItem;
    /**
     * The maximum number of times the animation can replay itself.
     */
    private int iterations;
    private int replyMode;
    /**
     * An array of sound effects per each frame. The values are already shifted by 8 bits to get the id of the actual sound effect, as the
     * rest of the information is useless to us.
     */
    private int[] soundEffects;

    public AnimationDefinitions(final int id, final ByteBuffer buffer) {
        this.id = id;
        setDefaults();
        decode(buffer);
    }

    public AnimationDefinitions clone() throws CloneNotSupportedException {
        return (AnimationDefinitions) super.clone();
    }

    /**
     * Gets a list of all the animations that share the skeleton of the animation in arguments.
     *
     * @param animationId the animation to compare
     * @return a list of animations.
     */
    public static final List<Integer> getSkeletonAnimations(final int animationId) throws Throwable {
        final AnimationDefinitions d = AnimationDefinitions.get(animationId);
        if (d == null) {
            throw new Throwable("Animation is null.");
        }
        if (d.frameIds == null) {
            throw new Throwable("Animation images are null - unable to compare.");
        }
        final int frameId = d.frameIds[0] >> 16;
        final List<Integer> ids = new ArrayList<Integer>();
        for (int i = 0; i < Utils.getIndiceSize(Indice.ANIMATION_DEFINITIONS); i++) {
            final AnimationDefinitions defs = AnimationDefinitions.get(i);
            if (defs == null) {
                continue;
            }
            if (defs.frameIds == null) {
                continue;
            }
            if (defs.frameIds[0] >> 16 == frameId) {
                ids.add(i);
            }
        }
        return ids;
    }

    public static final AnimationDefinitions get(final int id) {
        if (id < 0 || id >= definitions.length) {
            return null;
        }
        return definitions[id];
    }

    public static final IntArrayList getAnimationIdsByFrameId(final int frameId, final IntOpenHashSet linkedAnimations) {
        final it.unimi.dsi.fastutil.ints.IntArrayList list = new IntArrayList();
        for (int i = 0; i < Utils.getIndiceSize(Indice.ANIMATION_DEFINITIONS); i++) {
            if (linkedAnimations != null && linkedAnimations.contains(i)) {
                continue;
            }
            final mgi.types.config.AnimationDefinitions definitions = AnimationDefinitions.get(i);
            if (definitions == null) {
                continue;
            }
            if (definitions.getFrameIds() != null) {
                if (ArrayUtils.contains(definitions.getFrameIds(), frameId)) {
                    if (!list.contains(i)) {
                        list.add(i);
                    }
                }
            }
            if (definitions.getExtraFrameIds() != null) {
                if (ArrayUtils.contains(definitions.getExtraFrameIds(), frameId)) {
                    if (!list.contains(i)) {
                        list.add(i);
                    }
                }
            }
        }
        return list;
    }

    public static final int getSkeletonId(final int animationId) {
        final mgi.types.config.AnimationDefinitions definitions = get(animationId);
        if (definitions == null) {
            return -1;
        }
        final int[] frames = definitions.frameIds;
        if (frames == null || frames.length == 0) {
            return -1;
        }
        return frames[0] >> 16;
    }

    public static final void printAnimationDifferences(final Cache cache, final Cache cacheToCompareWith) {
        final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<byte[]> currentAnimations = getAnimations(cache);
        final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<byte[]> animations = getAnimations(cacheToCompareWith);
        it.unimi.dsi.fastutil.objects.ObjectIterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<byte[]>> iterator = currentAnimations.int2ObjectEntrySet().iterator();
        final it.unimi.dsi.fastutil.ints.IntArrayList list = new IntArrayList();
        while (iterator.hasNext()) {
            final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<byte[]> next = iterator.next();
            final int id = next.getIntKey();
            final byte[] bytes = next.getValue();
            final byte[] otherBytes = animations.get(id);
            if (otherBytes == null || !Arrays.equals(bytes, otherBytes)) {
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
            System.err.println("Animation difference: " + id);
        }
        System.err.println("Animation difference checking complete!");
    }

    private static final Int2ObjectOpenHashMap<byte[]> getAnimations(final Cache cache) {
        final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<byte[]> map = new Int2ObjectOpenHashMap<byte[]>();
        try {
            final mgi.tools.jagcached.cache.Archive configs = cache.getArchive(ArchiveType.CONFIGS);
            final mgi.tools.jagcached.cache.Group animations = configs.findGroupByID(GroupType.SEQUENCE);
            for (int id = 0; id < animations.getHighestFileId(); id++) {
                final mgi.tools.jagcached.cache.File file = animations.findFileByID(id);
                if (file == null) {
                    continue;
                }
                final mgi.utilities.ByteBuffer buffer = file.getData();
                if (buffer == null) {
                    continue;
                }
                map.put(id, buffer.toArray(0, buffer.getBuffer().length));
            }
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
        return map;
    }

    @Override
    public void load() {
        try {
            final mgi.tools.jagcached.cache.Cache cache = Game.getCacheMgi();
            final mgi.tools.jagcached.cache.Archive configs = cache.getArchive(ArchiveType.CONFIGS);
            final mgi.tools.jagcached.cache.Group animations = configs.findGroupByID(GroupType.SEQUENCE);
            definitions = new AnimationDefinitions[animations.getHighestFileId()];
            for (int id = 0; id < animations.getHighestFileId(); id++) {
                final mgi.tools.jagcached.cache.File file = animations.findFileByID(id);
                if (file == null) {
                    continue;
                }
                final mgi.utilities.ByteBuffer buffer = file.getData();
                if (buffer == null) {
                    continue;
                }
                definitions[id] = new AnimationDefinitions(id, buffer);
            }
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    private void setDefaults() {
        frameStep = -1;
        stretches = false;
        forcedPriority = 5;
        leftHandItem = -1;
        rightHandItem = -1;
        iterations = 99;
        precedenceAnimating = -1;
        priority = -1;
        replyMode = 2;
    }

    public final int getDuration() {
        int duration = 0;
        if (frameLengths == null) {
            return 0;
        }
        for (final int i : frameLengths) {
            if (i > 30) {
                continue;
            }
            duration += i * 20;
        }
        return duration;
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

    public void decode(final ByteBuffer buffer, final int opcode) {
        switch (opcode) {
        case 1: 
            {
                final int count = buffer.readUnsignedShort();
                frameLengths = new int[count];
                for (int index = 0; index < count; ++index) {
                    frameLengths[index] = buffer.readUnsignedShort();
                }
                frameIds = new int[count];
                for (int index = 0; index < count; ++index) {
                    frameIds[index] = buffer.readUnsignedShort();
                }
                for (int index = 0; index < count; ++index) {
                    frameIds[index] += (buffer.readUnsignedShort()) << 16;
                }
                return;
            }
        case 2: 
            frameStep = buffer.readUnsignedShort();
            return;
        case 3: 
            {
                final int count = buffer.readUnsignedByte();
                mergedBoneGroups = new int[1 + count];
                for (int index = 0; index < count; ++index) {
                    mergedBoneGroups[index] = buffer.readUnsignedByte();
                }
                mergedBoneGroups[count] = 9999999;
                return;
            }
        case 4: 
            stretches = true;
            return;
        case 5: 
            forcedPriority = buffer.readUnsignedByte();
            return;
        case 6: 
            leftHandItem = buffer.readUnsignedShort();
            if (leftHandItem > 0) {
                leftHandItem -= 512;
            }
            return;
        case 7: 
            rightHandItem = buffer.readUnsignedShort();
            if (rightHandItem > 0) {
                rightHandItem -= 512;
            }
            return;
        case 8: 
            iterations = buffer.readUnsignedByte();
            return;
        case 9: 
            precedenceAnimating = buffer.readUnsignedByte();
            return;
        case 10: 
            priority = buffer.readUnsignedByte();
            return;
        case 11: 
            replyMode = buffer.readUnsignedByte();
            return;
        case 12: 
            {
                final int count = buffer.readUnsignedByte();
                extraFrameIds = new int[count];
                for (int index = 0; index < count; ++index) {
                    extraFrameIds[index] = buffer.readUnsignedShort();
                }
                for (int index = 0; index < count; ++index) {
                    extraFrameIds[index] += (buffer.readUnsignedShort()) << 16;
                }
                return;
            }
        case 13: 
            {
                final int count = buffer.readUnsignedByte();
                soundEffects = new int[count];
                for (int index = 0; index < count; ++index) {
                    soundEffects[index] = buffer.readMedium();
                }
            }
            return;
        }
    }

    @Override
    public ByteBuffer encode() {
        final mgi.utilities.ByteBuffer buffer = new ByteBuffer(1024 * 10 * 10);
        if (frameIds != null) {
            buffer.writeByte(1);
            buffer.writeShort(frameLengths.length);
            for (int frameLength : frameLengths) {
                buffer.writeShort(frameLength);
            }
            for (final int frameId : frameIds) {
                buffer.writeShort(frameId & 65535);
            }
            for (final int frameId : frameIds) {
                buffer.writeShort(frameId >> 16);
            }
        }
        if (frameStep != -1) {
            buffer.writeByte(2);
            buffer.writeShort(frameStep);
        }
        if (mergedBoneGroups != null) {
            buffer.writeByte(3);
            buffer.writeByte(mergedBoneGroups.length - 1);
            for (int i = 0, len = mergedBoneGroups.length - 1; i < len; i++) {
                buffer.writeByte(mergedBoneGroups[i]);
            }
        }
        if (stretches) {
            buffer.writeByte(4);
        }
        if (forcedPriority != 5) {
            buffer.writeByte(5);
            buffer.writeByte(forcedPriority);
        }
        if (leftHandItem != -1) {
            buffer.writeByte(6);
            buffer.writeShort(leftHandItem == 0 ? 0 : leftHandItem + 512);
        }
        if (rightHandItem != -1) {
            buffer.writeByte(7);
            buffer.writeShort(rightHandItem == 0 ? 0 : rightHandItem + 512);
        }
        if (iterations != 99) {
            buffer.writeByte(8);
            buffer.writeByte(iterations);
        }
        if (precedenceAnimating != -1) {
            buffer.writeByte(9);
            buffer.writeByte(precedenceAnimating);
        }
        if (priority != -1) {
            buffer.writeByte(10);
            buffer.writeByte(priority);
        }
        if (replyMode != 2) {
            buffer.writeByte(11);
            buffer.writeByte(replyMode);
        }
        if (extraFrameIds != null) {
            buffer.writeByte(12);
            buffer.writeByte(extraFrameIds.length);
            for (final int frameId : extraFrameIds) {
                buffer.writeShort(frameId & 65535);
            }
            for (final int frameId : extraFrameIds) {
                buffer.writeShort(frameId >> 16);
            }
        }
        if (soundEffects != null) {
            buffer.writeByte(13);
            buffer.writeByte(soundEffects.length);
            for (final int soundEffect : soundEffects) {
                buffer.writeMedium(soundEffect);
            }
        }
        buffer.writeByte(0);
        return buffer;
    }

    @Override
    public void pack() {
        final mgi.tools.jagcached.cache.Archive archive = Game.getCacheMgi().getArchive(ArchiveType.CONFIGS);
        final mgi.tools.jagcached.cache.Group animations = archive.findGroupByID(GroupType.SEQUENCE);
        animations.addFile(new File(id, encode()));
    }

    public final IntArrayList getUniqueFrames() {
        final it.unimi.dsi.fastutil.ints.IntArrayList list = new IntArrayList();
        if (frameIds != null) {
            for (final int frame : frameIds) {
                if (!list.contains(frame)) {
                    list.add(frame);
                }
            }
        }
        if (extraFrameIds != null) {
            for (final int frame : extraFrameIds) {
                if (!list.contains(frame)) {
                    list.add(frame);
                }
            }
        }
        return list;
    }

    public AnimationDefinitions() {
    }

    /**
     * The id of the animation.
     */
    public void setId(final int id) {
        this.id = id;
    }

    public void setPrecedenceAnimating(final int precedenceAnimating) {
        this.precedenceAnimating = precedenceAnimating;
    }

    /**
     * An array of frame ids. The value is a bitpacked number, with bits past 16 being the skeleton id.
     */
    public void setFrameIds(final int[] frameIds) {
        this.frameIds = frameIds;
    }

    public void setMergedBoneGroups(final int[] mergedBoneGroups) {
        this.mergedBoneGroups = mergedBoneGroups;
    }

    /**
     * Animation priority level.
     */
    public void setPriority(final int priority) {
        this.priority = priority;
    }

    public void setFrameStep(final int frameStep) {
        this.frameStep = frameStep;
    }

    /**
     * The length of each frame, with one value being equal to one actual frame, capping at 20 milliseconds (1 second / 50 FPS)
     */
    public void setFrameLengths(final int[] frameLengths) {
        this.frameLengths = frameLengths;
    }

    public void setStretches(final boolean stretches) {
        this.stretches = stretches;
    }

    public void setExtraFrameIds(final int[] extraFrameIds) {
        this.extraFrameIds = extraFrameIds;
    }

    /**
     * The id of the item held in the left hand. If the id is 0, the helf left hand item is not displayed by the client.
     */
    public void setLeftHandItem(final int leftHandItem) {
        this.leftHandItem = leftHandItem;
    }

    public void setForcedPriority(final int forcedPriority) {
        this.forcedPriority = forcedPriority;
    }

    /**
     * The id of the item held in the right hand. If the id is 0, the held right hand item is not displayed by the
     * client.
     */
    public void setRightHandItem(final int rightHandItem) {
        this.rightHandItem = rightHandItem;
    }

    /**
     * The maximum number of times the animation can replay itself.
     */
    public void setIterations(final int iterations) {
        this.iterations = iterations;
    }

    public void setReplyMode(final int replyMode) {
        this.replyMode = replyMode;
    }

    /**
     * An array of sound effects per each frame. The values are already shifted by 8 bits to get the id of the actual sound effect, as the
     * rest of the information is useless to us.
     */
    public void setSoundEffects(final int[] soundEffects) {
        this.soundEffects = soundEffects;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "AnimationDefinitions(id=" + this.getId() + ", precedenceAnimating=" + this.getPrecedenceAnimating() + ", frameIds=" + java.util.Arrays.toString(this.getFrameIds()) + ", mergedBoneGroups=" + java.util.Arrays.toString(this.getMergedBoneGroups()) + ", priority=" + this.getPriority() + ", frameStep=" + this.getFrameStep() + ", frameLengths=" + java.util.Arrays.toString(this.getFrameLengths()) + ", stretches=" + this.isStretches() + ", extraFrameIds=" + java.util.Arrays.toString(this.getExtraFrameIds()) + ", leftHandItem=" + this.getLeftHandItem() + ", forcedPriority=" + this.getForcedPriority() + ", rightHandItem=" + this.getRightHandItem() + ", iterations=" + this.getIterations() + ", replyMode=" + this.getReplyMode() + ", soundEffects=" + java.util.Arrays.toString(this.getSoundEffects()) + ")";
    }

    /**
     * The id of the animation.
     */
    public int getId() {
        return this.id;
    }

    public int getPrecedenceAnimating() {
        return this.precedenceAnimating;
    }

    /**
     * An array of frame ids. The value is a bitpacked number, with bits past 16 being the skeleton id.
     */
    public int[] getFrameIds() {
        return this.frameIds;
    }

    public int[] getMergedBoneGroups() {
        return this.mergedBoneGroups;
    }

    /**
     * Animation priority level.
     */
    public int getPriority() {
        return this.priority;
    }

    public int getFrameStep() {
        return this.frameStep;
    }

    /**
     * The length of each frame, with one value being equal to one actual frame, capping at 20 milliseconds (1 second / 50 FPS)
     */
    public int[] getFrameLengths() {
        return this.frameLengths;
    }

    public boolean isStretches() {
        return this.stretches;
    }

    public int[] getExtraFrameIds() {
        return this.extraFrameIds;
    }

    /**
     * The id of the item held in the left hand. If the id is 0, the helf left hand item is not displayed by the client.
     */
    public int getLeftHandItem() {
        return this.leftHandItem;
    }

    public int getForcedPriority() {
        return this.forcedPriority;
    }

    /**
     * The id of the item held in the right hand. If the id is 0, the held right hand item is not displayed by the
     * client.
     */
    public int getRightHandItem() {
        return this.rightHandItem;
    }

    /**
     * The maximum number of times the animation can replay itself.
     */
    public int getIterations() {
        return this.iterations;
    }

    public int getReplyMode() {
        return this.replyMode;
    }

    /**
     * An array of sound effects per each frame. The values are already shifted by 8 bits to get the id of the actual sound effect, as the
     * rest of the information is useless to us.
     */
    public int[] getSoundEffects() {
        return this.soundEffects;
    }
}
