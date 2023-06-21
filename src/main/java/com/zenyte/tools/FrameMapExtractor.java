package com.zenyte.tools;

import com.zenyte.Constants;
import com.zenyte.game.util.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import mgi.Indice;
import mgi.types.config.AnimationDefinitions;
import mgi.types.config.GraphicsDefinitions;
import mgi.types.config.ObjectDefinitions;
import mgi.types.config.npcs.NPCDefinitions;
import mgi.types.skeleton.SkeletonDefinitions;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;

/**
 * @author Kris | 19. sept 2018 : 17:15:25
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>}
 */
public class FrameMapExtractor implements Extractor {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FrameMapExtractor.class);

	@Override
	public void extract() {
		try {
			final java.io.BufferedWriter writer = new BufferedWriter(new FileWriter(new File("info/#" + Constants.REVISION + " frame map.txt")));
			final it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap<it.unimi.dsi.fastutil.ints.IntArrayList> map = new Int2ObjectAVLTreeMap<IntArrayList>();
			final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<mgi.types.skeleton.SkeletonDefinitions> skeletonDefinitions = SkeletonDefinitions.getDefinitions();
			final it.unimi.dsi.fastutil.objects.ObjectIterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<mgi.types.skeleton.SkeletonDefinitions>> iterator = skeletonDefinitions.int2ObjectEntrySet().fastIterator();
			final int length = Utils.getIndiceSize(Indice.SKELETON);
			int count = 0;
			while (iterator.hasNext()) {
				final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<mgi.types.skeleton.SkeletonDefinitions> next = iterator.next();
				try {
					final mgi.types.skeleton.SkeletonDefinitions definitions = next.getValue();
					final int frameMapId = definitions.getFrameMapId();
					if (map.containsKey(frameMapId)) {
						System.err.println("[Skipping] Progress: " + ++count + "/" + length);
						continue;
					}
					final it.unimi.dsi.fastutil.ints.IntArrayList animations = new IntArrayList();
					final it.unimi.dsi.fastutil.ints.IntOpenHashSet frameIds = SkeletonDefinitions.getLinkedFrames(frameMapId);
					for (final java.lang.Integer frameId : frameIds) {
						final it.unimi.dsi.fastutil.ints.IntArrayList foundAnimations = AnimationDefinitions.getAnimationIdsByFrameId(frameId, frameIds);
						final it.unimi.dsi.fastutil.ints.IntListIterator it = foundAnimations.listIterator();
						while (it.hasNext()) {
							final int foundAnimation = it.nextInt();
							if (foundAnimation != -1 && !animations.contains(foundAnimation)) {
								animations.add(foundAnimation);
							}
						}
					}
					Collections.sort(animations);
					map.put(frameMapId, animations);
					System.err.println("Progress: " + ++count + "/" + length);
				} catch (final Exception e) {
					log.error(Strings.EMPTY, e);
				}
			}
			map.forEach((i, list) -> {
				try {
					writer.write("Frame map: " + i);
					writer.newLine();
					if (!list.isEmpty()) {
						writer.write("Linked animations: " + list.toString());
						writer.newLine();
					}
					final it.unimi.dsi.fastutil.ints.IntArrayList linkedNPCs = new IntArrayList();
					for (int a = 0; a < Utils.getIndiceSize(Indice.NPC_DEFINITIONS); a++) {
						final mgi.types.config.npcs.NPCDefinitions definitions = NPCDefinitions.get(a);
						if (definitions == null) continue;
						if (list.contains(definitions.getStandAnimation()) || list.contains(definitions.getWalkAnimation())) {
							if (!linkedNPCs.contains(a)) {
								linkedNPCs.add(a);
							}
						}
					}
					if (!linkedNPCs.isEmpty()) {
						final java.lang.StringBuilder builder = new StringBuilder();
						builder.append("Linked NPCs: ");
						for (final int id : linkedNPCs) {
							final mgi.types.config.npcs.NPCDefinitions npcDefinitions = NPCDefinitions.get(id);
							if (npcDefinitions == null) {
								continue;
							}
							builder.append(npcDefinitions.getName()).append(" (").append(id).append("), ");
						}
						if (builder.length() > 2) {
							builder.delete(builder.length() - 2, builder.length());
						}
						if (builder.length() > 0) {
							writer.write(builder.toString());
							writer.newLine();
						}
					}
					final it.unimi.dsi.fastutil.ints.IntArrayList linkedGFXs = new IntArrayList();
					for (int a = 0; a < Utils.getIndiceSize(Indice.GRAPHICS_DEFINITIONS); a++) {
						final mgi.types.config.GraphicsDefinitions definitions = GraphicsDefinitions.get(a);
						if (definitions == null) {
							continue;
						}
						if (list.contains(definitions.getAnimationId())) {
							if (!linkedGFXs.contains(a)) {
								linkedGFXs.add(a);
							}
						}
					}
					if (!linkedGFXs.isEmpty()) {
						final java.lang.StringBuilder builder = new StringBuilder();
						builder.append("Linked graphics: ");
						for (final int id : linkedGFXs) {
							final mgi.types.config.GraphicsDefinitions graphicsDefinitions = GraphicsDefinitions.get(id);
							if (graphicsDefinitions == null) {
								continue;
							}
							builder.append(id).append(", ");
						}
						if (builder.length() > 2) {
							builder.delete(builder.length() - 2, builder.length());
						}
						if (builder.length() > 0) {
							writer.write(builder.toString());
							writer.newLine();
						}
					}
					final it.unimi.dsi.fastutil.ints.IntArrayList linkedObjects = new IntArrayList();
					for (int a = 0; a < Utils.getIndiceSize(Indice.OBJECT_DEFINITIONS); a++) {
						final mgi.types.config.ObjectDefinitions definitions = ObjectDefinitions.get(a);
						if (definitions == null) {
							continue;
						}
						if (list.contains(definitions.getAnimationId())) {
							if (!linkedObjects.contains(a)) {
								linkedObjects.add(a);
							}
						}
					}
					if (!linkedObjects.isEmpty()) {
						final java.lang.StringBuilder builder = new StringBuilder();
						builder.append("Linked objects: ");
						for (final int id : linkedObjects) {
							final mgi.types.config.ObjectDefinitions objectDefinitions = ObjectDefinitions.get(id);
							if (objectDefinitions == null) {
								continue;
							}
							builder.append(objectDefinitions.getName()).append(" (").append(id).append("), ");
						}
						if (builder.length() > 2) {
							builder.delete(builder.length() - 2, builder.length());
						}
						if (builder.length() > 0) {
							writer.write(builder.toString());
							writer.newLine();
						}
					}
					writer.newLine();
				} catch (final Exception e) {
					log.error(Strings.EMPTY, e);
				}
			});
			System.err.println("Animation dump by frame map complete.");
			writer.flush();
			writer.close();
		} catch (final Exception e) {
			log.error(Strings.EMPTY, e);
		}
	}
}
