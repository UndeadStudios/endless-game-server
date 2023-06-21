package com.zenyte.game.world.entity.npc.spawns;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zenyte.game.parser.Parse;
import com.zenyte.game.tasks.WorldTasksManager;
import com.zenyte.game.util.Direction;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.npc.impl.Crab;
import com.zenyte.game.world.entity.npc.impl.slayer.superior.SuperiorMonster;
import com.zenyte.game.world.region.GlobalAreaManager;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mgi.types.config.npcs.NPCDefinitions;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

public class NPCSpawnLoader implements Parse {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NPCSpawnLoader.class);
    public static final IntSet dropViewerNPCs = IntSets.synchronize(new IntOpenHashSet());
    public static final IntSet ignoredMonsters = IntSets.synchronize(new IntOpenHashSet());
    private static final Int2ObjectMap<Set<String>> npcAreaMap = new Int2ObjectOpenHashMap<>();
    private static final int MAXIMUM_AREA_CHECK_RADIUS = 25;
    private static final Map<Location, String> areaMap = new Object2ObjectOpenHashMap<>();
    private static final List<NPCSpawn> artificialSpawns = new ArrayList<>();
    private static final Int2IntMap npcTransformers = new Int2IntOpenHashMap();

    public static final Set<String> getFoundLocations(final int npcId) {
        return npcAreaMap.get(npcId);
    }

    public static final void populateAreaMap() {
        final Int2ObjectMap<Set<String>> npcAreaMap = new Int2ObjectOpenHashMap<>();
        final java.util.ArrayList<com.zenyte.game.world.entity.npc.spawns.NPCSpawn> spawns = new ArrayList<NPCSpawn>();
        spawns.addAll(artificialSpawns);
        spawns.addAll(new ArrayList<>(DEFINITIONS));
        for (final com.zenyte.game.world.entity.npc.spawns.NPCSpawn spawn : spawns) {
            final com.zenyte.game.world.entity.Location tile = new Location(spawn.getX(), spawn.getY(), spawn.getZ());
            final com.zenyte.game.world.region.Area area = GlobalAreaManager.getArea(tile);
            if (area != null) {
                areaMap.put(tile, area.name());
            }
        }
        for (final com.zenyte.game.world.entity.npc.spawns.NPCSpawn spawn : spawns) {
            final com.zenyte.game.world.entity.Location tile = new Location(spawn.getX(), spawn.getY(), spawn.getZ());
            final com.zenyte.game.world.region.Area area = GlobalAreaManager.getArea(tile);
            String name = area == null ? "Undefined area" : area.name();
            if (area == null) {
                for (final java.util.Map.Entry<com.zenyte.game.world.entity.Location, java.lang.String> entry : areaMap.entrySet()) {
                    final com.zenyte.game.world.entity.Location t = entry.getKey();
                    if (t.withinDistance(tile, MAXIMUM_AREA_CHECK_RADIUS)) {
                        name = entry.getValue();
                        break;
                    }
                }
            }
            npcAreaMap.computeIfAbsent(npcTransformers.getOrDefault(spawn.getId(), spawn.getId()), a -> new ObjectOpenHashSet<>()).add(name);
        }
        npcAreaMap.forEach((id, set) -> NPCSpawnLoader.npcAreaMap.put(id.intValue(), set));
    }

    static {
        //Skotizo
        artificialSpawns.add(new NPCSpawn(7286, 1698, 9886, 0, Direction.SOUTH, 5));
        //Alchemical Hydra
        artificialSpawns.add(new NPCSpawn(8621, 1364, 10265, 0, Direction.SOUTH, 5));
        //Dusk (Grotesque Guardians)
        artificialSpawns.add(new NPCSpawn(7888, 3426, 3542, 2, Direction.SOUTH, 5));
        //Obor
        artificialSpawns.add(new NPCSpawn(7416, 3095, 9832, 0, Direction.SOUTH, 5));
        //Bryophyta
        artificialSpawns.add(new NPCSpawn(8195, 3174, 9900, 0, Direction.SOUTH, 5));
        //Zulrah
        artificialSpawns.add(new NPCSpawn(2042, 2267, 3072, 0, Direction.SOUTH, 5));
        //Vorkath
        artificialSpawns.add(new NPCSpawn(8061, 2269, 4062, 0, Direction.SOUTH, 5));
        //Cerberus
        artificialSpawns.add(new NPCSpawn(5862, 1239, 1250, 0, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(5862, 1303, 1313, 0, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(5862, 1367, 1249, 0, Direction.SOUTH, 5));
        //Corporeal beast
        artificialSpawns.add(new NPCSpawn(319, 2990, 4381, 2, Direction.SOUTH, 5));
        //Scavenger beasts
        artificialSpawns.add(new NPCSpawn(7548, 3279, 5225, 1, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(7549, 3279, 5225, 1, Direction.SOUTH, 5));
        //Mogre
        artificialSpawns.add(new NPCSpawn(2592, 2993, 3108, 0, Direction.SOUTH, 5));
        //The Mimic
        artificialSpawns.add(new NPCSpawn(8633, 2719, 4318, 1, Direction.SOUTH, 5));
        dropViewerNPCs.addAll(SuperiorMonster.superiorMonsters);
        //Armadylian guardian
        dropViewerNPCs.add(6587);
        //Bandosian guardian
        dropViewerNPCs.add(6587);
        //Brassican mage
        dropViewerNPCs.add(7310);
        //Ancient wizard
        dropViewerNPCs.add(7307);
        //Krakens
        dropViewerNPCs.add(494);
        dropViewerNPCs.add(492);
        npcTransformers.put(493, 492);
        npcTransformers.put(496, 494);
        //Werewolves
        npcTransformers.put(2631, 2593);
        //All kinds of crabs.
        npcTransformers.putAll(Crab.rocks2AliveMap);
        //Wall beast
        dropViewerNPCs.add(476);
        npcTransformers.put(475, 476);
        //Zygomites
        dropViewerNPCs.add(473);
        dropViewerNPCs.add(474);
        npcTransformers.put(471, 473);
        npcTransformers.put(472, 474);
        dropViewerNPCs.add(7797);
        npcTransformers.put(7798, 7797);
        //Tree spirits
        dropViewerNPCs.add(1163);
        dropViewerNPCs.add(1861);
        dropViewerNPCs.add(1862);
        dropViewerNPCs.add(1863);
        dropViewerNPCs.add(1864);
        dropViewerNPCs.add(1865);
        dropViewerNPCs.add(1866);
        dropViewerNPCs.add(6380);
        //Locost riders
        dropViewerNPCs.add(795);
        dropViewerNPCs.add(796);
        dropViewerNPCs.add(800);
        dropViewerNPCs.add(801);
        //Brutal green dragons
        dropViewerNPCs.add(2918);
        dropViewerNPCs.add(8081);
        dropViewerNPCs.add(8583);
        //Godwars sergeants
        artificialSpawns.add(new NPCSpawn(2216, 2868, 5362, 2, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(2217, 2872, 5354, 2, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(2218, 2871, 5359, 2, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(2206, 2901, 5264, 0, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(2207, 2897, 5263, 0, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(2208, 2895, 5265, 0, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(3130, 2929, 5327, 2, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(3131, 2921, 5327, 2, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(3132, 2923, 5324, 2, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(3163, 2834, 5297, 2, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(3164, 2827, 5299, 2, Direction.SOUTH, 5));
        artificialSpawns.add(new NPCSpawn(3165, 2829, 5300, 2, Direction.SOUTH, 5));
        dropViewerNPCs.addAll(npcTransformers.values());
        artificialSpawns.forEach(spawn -> dropViewerNPCs.add(spawn.getId()));
    }

    /**
     * A mapping of the definitions to their item id.
     */
    public static final List<NPCSpawn> DEFINITIONS = new ArrayList<>();

    public static final void parseDefinitions() {
        try {
            new NPCSpawnLoader().parse();
        } catch (Throwable throwable) {
            log.error(Strings.EMPTY, throwable);
        }
    }

    public static final void loadNPCSpawns() {
        try {
            WorldTasksManager.schedule(() -> DEFINITIONS.forEach(v -> {
                dropViewerNPCs.add(v.getId());
                final com.zenyte.game.world.entity.Location tile = new Location(v.getX(), v.getY(), v.getZ());
                World.getChunk(tile.getChunkHash());
                World.spawnNPC(v, v.getId(), tile, v.getDirection(), v.getRadius());
            }));
        } catch (final 
        //DEFINITIONS.clear();
        Throwable e) {
            log.error(Strings.EMPTY, e);
        }
    }

    public static void save() {
        final String toJson = World.getGson().toJson(DEFINITIONS);
        try {
            final PrintWriter pw = new PrintWriter("data/npcs/spawns.json", "UTF-8");
            pw.println(toJson);
            pw.close();
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    @Override
    public void parse() throws Throwable {
        final BufferedReader br = new BufferedReader(new FileReader("data/npcs/spawns.json"));
        final NPCSpawn[] item_definitions = World.getGson().fromJson(br, NPCSpawn[].class);
        for (final NPCSpawn def : item_definitions) {
            if (def != null) {
                DEFINITIONS.add(def);
            }
        }
    }

    @Deprecated
    public void parseNew() {
        final Map<Integer, Spawn> KRIS_SPAWNS = new HashMap<Integer, Spawn>();
        final Map<Integer, Spawn> MTARIK_SPAWNS = new HashMap<Integer, Spawn>();
        final Map<Integer, Spawn> spawns = new HashMap<Integer, Spawn>();
        try {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            final java.io.BufferedReader reader = new BufferedReader(new FileReader("data/npcs/NPC Spawns.json"));
            final com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader.Spawn[] definitions = gson.fromJson(reader, Spawn[].class);
            for (final com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader.Spawn spawn : definitions) {
                KRIS_SPAWNS.put(spawn.index, spawn);
            }
            final java.io.BufferedReader mtarikreader = new BufferedReader(new FileReader("data/npcs/MTARIK_NPC Spawns.json"));
            final com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader.Spawn[] mtarikdefinitions = gson.fromJson(mtarikreader, Spawn[].class);
            for (final com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader.Spawn spawn : mtarikdefinitions) {
                MTARIK_SPAWNS.put(spawn.index, spawn);
            }
            for (final java.util.Map.Entry<java.lang.Integer, com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader.Spawn> entry : KRIS_SPAWNS.entrySet()) {
                final java.lang.Integer index = entry.getKey();
                final com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader.Spawn spawn = entry.getValue();
                if (!MTARIK_SPAWNS.containsKey(index)) {
                    spawns.put(index, spawn);
                    continue;
                }
            }
            for (final java.util.Map.Entry<java.lang.Integer, com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader.Spawn> entry : MTARIK_SPAWNS.entrySet()) {
                final java.lang.Integer index = entry.getKey();
                final com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader.Spawn spawn = entry.getValue();
                if (!KRIS_SPAWNS.containsKey(index)) {
                    spawns.put(index, spawn);
                    continue;
                }
            }
            for (final java.util.Map.Entry<java.lang.Integer, com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader.Spawn> entry : KRIS_SPAWNS.entrySet()) {
                final java.lang.Integer index = entry.getKey();
                final com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader.Spawn spawn = entry.getValue();
                if (MTARIK_SPAWNS.containsKey(index)) {
                    final com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader.Spawn comparableSpawn = MTARIK_SPAWNS.get(index);
                    if (comparableSpawn.minX < spawn.minX) {
                        spawn.minX = comparableSpawn.minX;
                    }
                    if (comparableSpawn.maxX > spawn.maxX) {
                        spawn.maxX = comparableSpawn.maxX;
                    }
                    if (comparableSpawn.minY < spawn.minY) {
                        spawn.minY = comparableSpawn.minY;
                    }
                    if (comparableSpawn.minY > spawn.minY) {
                        spawn.minY = comparableSpawn.minY;
                    }
                    spawns.put(index, spawn);
                }
                //spawns.put(index, spawn);
                //continue;
            }
            for (final com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader.Spawn spawn : spawns.values()) {
                final mgi.types.config.npcs.NPCDefinitions defs = NPCDefinitions.get(spawn.id);
                if (defs == null) {
                    continue;
                }
                final java.lang.String defName = defs.getName();
                if (spawn.id == 324 || spawn.id == 2779 || defName.startsWith("Reanimated") || defName.startsWith("Animated") && !defName.endsWith("spade") || defs.containsOption("Dismiss") || defs.isFamiliar()) {
                    continue;
                }
                if (defName.toLowerCase().contains("crab")) {
                    switch (spawn.id) {
                    case 100: 
                    case 5935: 
                    case 7206: 
                    case 2261: 
                    case 5940: 
                    case 7799: 
                        spawn.id++;
                        spawn.minX = spawn.maxX = ((spawn.minX + spawn.maxX) / 2);
                        spawn.minY = spawn.maxY = ((spawn.minY + spawn.maxY) / 2);
                        break;
                    }
                }
                if (defName.equals("Rocks")) {
                    spawn.minX = spawn.maxX = ((spawn.minX + spawn.maxX) / 2);
                    spawn.minY = spawn.maxY = ((spawn.minY + spawn.maxY) / 2);
                }
                if (defName.toLowerCase().contains("fishing spot")) {
                    spawn.minX = spawn.maxX;
                    spawn.minY = spawn.maxY;
                }
                final com.zenyte.game.world.entity.npc.spawns.NPCSpawn s = new NPCSpawn();
                s.setId(spawn.id);
                s.setDirection(Direction.npcMap.get(spawn.direction));
                final int x = (spawn.getMaxX() + spawn.getMinX()) / 2;
                final int y = (spawn.getMaxY() + spawn.getMinY()) / 2;
                int radius = Math.max(spawn.getMaxX() - spawn.getMinX(), spawn.getMaxY() - spawn.getMinY());
                //s.setKnownIndex(spawn.getIndex());
                s.setX(x);
                s.setY(y);
                if (radius > 0) {
                    radius = Math.max(radius, 4);
                }
                if (radius == 0) {
                    if (defs.containsOption("Attack")) {
                        radius = 4;
                    }
                }
                s.setRadius(radius);
                s.setZ(spawn.z);
                if (radius == 0) {
                    s.setRadius(null);
                }
                if (s.getDirection() == Direction.SOUTH || radius > 0) {
                    s.setDirection(null);
                }
                DEFINITIONS.add(s);
            }
            final String toJson = gson.toJson(DEFINITIONS);
            try {
                final PrintWriter pw = new PrintWriter("data/npcs/spawns.json", "UTF-8");
                pw.println(toJson);
                pw.close();
            } catch (final Exception e) {
                log.error(Strings.EMPTY, e);
            }
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }


    @Deprecated
    private static final class Spawn {
        private String name;
        private int id;
        private int index;
        private int direction;
        private int minX;
        private int minY;
        private int maxX;
        private int maxY;
        private int z;

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public int getId() {
            return this.id;
        }

        public int getIndex() {
            return this.index;
        }

        public void setId(final int id) {
            this.id = id;
        }

        public void setIndex(final int index) {
            this.index = index;
        }

        public int getDirection() {
            return this.direction;
        }

        public int getMinX() {
            return this.minX;
        }

        public int getMinY() {
            return this.minY;
        }

        public int getMaxX() {
            return this.maxX;
        }

        public int getMaxY() {
            return this.maxY;
        }

        public int getZ() {
            return this.z;
        }

        public void setDirection(final int direction) {
            this.direction = direction;
        }

        public void setMinX(final int minX) {
            this.minX = minX;
        }

        public void setMinY(final int minY) {
            this.minY = minY;
        }

        public void setMaxX(final int maxX) {
            this.maxX = maxX;
        }

        public void setMaxY(final int maxY) {
            this.maxY = maxY;
        }

        public void setZ(final int z) {
            this.z = z;
        }
    }
}
