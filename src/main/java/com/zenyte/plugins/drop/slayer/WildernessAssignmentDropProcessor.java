package com.zenyte.plugins.drop.slayer;

import com.zenyte.game.content.skills.slayer.BossTask;
import com.zenyte.game.content.skills.slayer.RegularTask;
import com.zenyte.game.content.skills.slayer.SlayerMaster;
import com.zenyte.game.item.Item;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.entity.npc.NPC;
import com.zenyte.game.world.entity.npc.combatdefs.NPCCDLoader;
import com.zenyte.game.world.entity.npc.drop.matrix.DropProcessor;
import com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.region.area.wilderness.WildernessArea;
import com.zenyte.utils.Ordinal;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import kotlin.Pair;
import mgi.Indice;
import mgi.types.config.npcs.NPCDefinitions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntPredicate;

/**
 * @author Kris | 20/04/2019 18:48
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class WildernessAssignmentDropProcessor extends DropProcessor {

    @Ordinal
    public enum Emblem {
        T1(12746, 10000), T2(12748, 9000), T3(12749, 7000), T4(12750, 5000), T5(12751, 2500), T6(12752, 1000), T7(12753, 500), T8(12754, 200), T9(12755, 100), T10(12756, 25);
        private final int id;
        private final int chance;
        private static int total;
        private static final Emblem[] values = values();
        private static final Map<Integer, Emblem> all = new HashMap<>(values.length);

        public int next() {
            final com.zenyte.plugins.drop.slayer.WildernessAssignmentDropProcessor.Emblem next = values[ordinal() + 1];
            return next == null ? -1 : next.id;
        }

        static {
            for (final com.zenyte.plugins.drop.slayer.WildernessAssignmentDropProcessor.Emblem value : values) {
                total += value.chance;
                all.put(value.id, value);
            }
        }

        public static Emblem get(final Item item) {
            return all.get(item.getId());
        }

        private static final Emblem get() {
            int random = Utils.random(total);
            int current = 0;
            for (final com.zenyte.plugins.drop.slayer.WildernessAssignmentDropProcessor.Emblem reward : values) {
                if ((current += reward.chance) >= random) {
                    return reward;
                }
            }
            return null;
        }

        private Emblem(final int id, final int chance) {
            this.id = id;
            this.chance = chance;
        }

        public int getId() {
            return this.id;
        }

        public int getChance() {
            return this.chance;
        }
    }

    @Override
    public void attach() {
        for (final int i : allIds) {
            final mgi.types.config.npcs.NPCDefinitions definitions = NPCDefinitions.get(i);
            if (definitions == null) {
                continue;
            }
            final com.zenyte.game.world.entity.npc.combatdefs.NPCCombatDefinitions combatDefinitions = NPCCDLoader.get(i);
            if (combatDefinitions == null) {
                continue;
            }
            final int hitpoints = combatDefinitions.getHitpoints();
            final java.lang.String name = definitions.getName().toLowerCase();
            final float emblemChance = 1.0F / (155 - (hitpoints / 2.0F));
            final float percentage = emblemChance * 100.0F;
            final int fraction = (int) (100.0F / percentage);
            appendDrop(new DisplayedDrop(12746, 1, 1, fraction, (player, npcId) -> npcId == i, i));
            put(i, 12746, new PredicatedDrop("Only dropped by those found in Wilderness while on a slayer assignment from Krystilia. May occasionally drop as a higher tier."));
            boolean isBossTask = false;
            for (final com.zenyte.game.content.skills.slayer.BossTask task : BossTask.VALUES) {
                if (name.equals(task.getTaskName().toLowerCase())) {
                    isBossTask = true;
                    break;
                }
            }
            final float enchantmentChance = (1.0F / (320 - (hitpoints * 0.8F)));
            final float enchantmentPercentage = enchantmentChance * 100.0F;
            final int enchantmentFraction = (int) (100.0F / enchantmentPercentage);
            appendDrop(new DisplayedDrop(21257, 1, 1, isBossTask ? 30 : enchantmentFraction, (player, npcId) -> npcId == i, i));
            put(i, 21257, new PredicatedDrop("Only dropped by those found in Wilderness while on a slayer assignment from Krystilia."));
        }
    }

    @Override
    public void onDeath(final NPC npc, final Player killer) {
        final com.zenyte.game.content.skills.slayer.Slayer slayer = killer.getSlayer();
        final com.zenyte.game.content.skills.slayer.Assignment assignment = slayer.getAssignment();
        if (assignment == null || assignment.getMaster() != SlayerMaster.KRYSTILIA || !assignment.isValid(killer, npc) || !WildernessArea.isWithinWilderness(npc.getX(), npc.getY())) {
            return;
        }
        final float emblemChance = 1.0F / (155 - (npc.getMaxHitpoints() / 2.0F));
        if (Utils.randomDouble() <= emblemChance) {
            npc.dropItem(killer, new Item(Objects.requireNonNull(Emblem.get()).id));
        }
        final float enchantmentChance = assignment.getTask() instanceof BossTask ? 0.0333F : (1.0F / (320 - (npc.getMaxHitpoints() * 0.8F)));
        if (Utils.randomDouble() <= enchantmentChance) {
            npc.dropItem(killer, new Item(21257));
        }
    }

    @Override
    public int[] ids() {
        final it.unimi.dsi.fastutil.ints.IntOpenHashSet set = new IntOpenHashSet();
        final it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet<kotlin.Pair<java.lang.String, java.lang.Boolean>> names = new ObjectLinkedOpenHashSet<Pair<String, Boolean>>();
        loop:
        for (final com.zenyte.game.content.skills.slayer.RegularTask regularTask : RegularTask.VALUES) {
            for (final com.zenyte.game.content.skills.slayer.Task entry : regularTask.getTaskSet()) {
                if (entry.getSlayerMaster() == SlayerMaster.KRYSTILIA) {
                    for (final java.lang.String name : regularTask.getMonsters()) {
                        names.add(new Pair<>(name.toLowerCase(), true));
                    }
                    continue loop;
                }
            }
        }
        for (final com.zenyte.game.content.skills.slayer.BossTask bossTaskTask : BossTask.VALUES) {
            if (!bossTaskTask.isAssignableByKrystilia()) {
                continue;
            }
            names.add(new Pair<>(bossTaskTask.getTaskName().toLowerCase(), true));
        }
        loop:
        for (int i = 0; i < Utils.getIndiceSize(Indice.NPC_DEFINITIONS); i++) {
            final mgi.types.config.npcs.NPCDefinitions definitions = NPCDefinitions.get(i);
            if (definitions == null || definitions.getCombatLevel() == 0) {
                continue;
            }
            final java.lang.String name = definitions.getName().toLowerCase();
            for (final kotlin.Pair<java.lang.String, java.lang.Boolean> validName : names) {
                if (validName.getSecond() ? name.equals(validName.getFirst()) : name.contains(validName.getFirst())) {
                    set.add(i);
                    continue loop;
                }
            }
        }
        final it.unimi.dsi.fastutil.ints.IntOpenHashSet wildyNPCs = new IntOpenHashSet();
        for (final com.zenyte.game.world.entity.npc.spawns.NPCSpawn spawn : NPCSpawnLoader.DEFINITIONS) {
            if (WildernessArea.isWithinWilderness(spawn.getX(), spawn.getY())) {
                wildyNPCs.add(spawn.getId());
            }
        }
        wildyNPCs.add(6616);
        wildyNPCs.add(6617);
        wildyNPCs.add(6612);
        set.removeIf((IntPredicate) id -> !wildyNPCs.contains(id));
        return set.toIntArray();
    }
}
