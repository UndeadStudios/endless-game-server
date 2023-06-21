package mgi.tools.parser;

import com.esotericsoftware.kryo.Kryo;
import com.google.common.io.Files;
import com.moandjiezana.toml.Toml;
import com.zenyte.Constants;
import com.zenyte.Game;
import com.zenyte.Scanner;
import com.zenyte.game.constants.GameConstants;
import com.zenyte.game.content.achievementdiary.AchievementDiaries;
import com.zenyte.game.content.achievementdiary.DiaryArea;
import com.zenyte.game.content.event.easter2020.EasterConstants;
import com.zenyte.game.content.treasuretrails.stash.StashUnit;
import com.zenyte.game.ui.testinterfaces.BountyHunterStoreInterface;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;
import com.zenyte.game.world.object.WorldObject;
import com.zenyte.game.world.region.MapUtils;
import com.zenyte.game.world.region.XTEALoader;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import kotlin.text.Charsets;
import mgi.custom.*;
import mgi.custom.christmas.ChristmasMapPacker;
import mgi.custom.christmas.ChristmasObject;
import mgi.custom.easter.EasterMapPacker;
import mgi.tools.jagcached.ArchiveType;
import mgi.tools.jagcached.cache.Cache;
import mgi.tools.jagcached.cache.Group;
import mgi.types.Definitions;
import mgi.types.config.enums.EnumDefinitions;
import mgi.types.config.npcs.NPCDefinitions;
import mgi.types.sprite.SpriteGroupDefinitions;
import mgi.types.worldmap.WorldMapDefinitions;
import mgi.utilities.ByteBuffer;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.util.Strings;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tommeh | 16/01/2020 | 01:06
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
 */
public class TypeParser {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TypeParser.class);
    private static final Int2ObjectMap<List<String>> optionsMap = new Int2ObjectOpenHashMap<>(10 * 1024);
    private static final List<Definitions> definitions = new ArrayList<>();
    public static final Kryo KRYO = new Kryo();

    public static void main(final String[] args) throws IOException {
        String type = "";
        if (args.length > 0) {
            type = args[0];
        }
        final long startTime = System.nanoTime();
        if (type.equals("--unzip")) {
            FileUtils.cleanDirectory(new File("data/cache"));
            FileUtils.cleanDirectory(new File("data/cache-original"));
            final net.lingala.zip4j.ZipFile zipFile = new ZipFile("data/cache-original.zip");
            zipFile.extractAll("data/cache");
            zipFile.extractAll("data/cache-original");
        }
        Game.load();
        XTEALoader.load();
        Definitions.loadDefinitions(Definitions.LOW_PRIORITY_DEFINITIONS);
        initializeKryo();
        parse(new File("assets/types"));
        pack(NPCDefinitions.class);
        repackNPCOptions();
        packDynamicConfigs();
        packHighRevision();
        pack(ArrayUtils.addAll(Definitions.HIGH_PRIORITY_DEFINITIONS, Definitions.LOW_PRIORITY_DEFINITIONS));
        packClientBackground();
        packModels();
        packClientScripts();
        packInterfaces();
        packMaps();
        Game.getCacheMgi().close();
        log.info("Cache repack took " + Utils.nanoToMilli(System.nanoTime() - startTime) + " milliseconds!");
    }

    private static void initializeKryo() {
        for (final java.lang.Class<?> d : Definitions.LOW_PRIORITY_DEFINITIONS) {
            KRYO.register(d);
        }
        for (final java.lang.Class<?> d : Definitions.HIGH_PRIORITY_DEFINITIONS) {
            KRYO.register(d);
        }
        KRYO.register(int[].class);
        KRYO.register(short[].class);
        KRYO.register(String[].class);
        KRYO.register(Int2ObjectOpenHashMap.class);
    }

    private static void repackNPCOptions() {
        new NPCDefinitions().load();
        new Scanner().scan(NPCPlugin.class);
        Game.setCacheMgi(Cache.openCache("./data/cache-original"));
        new NPCDefinitions().load();
        for (final mgi.types.config.npcs.NPCDefinitions npc : NPCDefinitions.definitions) {
            if (npc == null) {
                continue;
            }
            final java.util.List<java.lang.String> list = optionsMap.computeIfAbsent(npc.getId(), n -> new ArrayList<>());
            final java.lang.String[] options = npc.getOptions();
            for (final java.lang.String option : options) {
                if (option == null) {
                    list.add(null);
                    continue;
                }
                final com.zenyte.game.world.entity.npc.actions.NPCPlugin.NPCPluginHandler plugin = NPCPlugin.getHandler(npc.getId(), option);
                list.add(plugin == null ? null : option);
            }
        }
        Game.setCacheMgi(Cache.openCache("./data/cache/"));
        new NPCDefinitions().load();
        for (final mgi.types.config.npcs.NPCDefinitions npc : NPCDefinitions.definitions) {
            if (npc == null) {
                continue;
            }
            final java.util.List<java.lang.String> options = optionsMap.get(npc.getId());
            if (options == null) {
                continue;
            }
            assert options.size() == 5;
            npc.setOptions(options.toArray(new String[0]));
            npc.pack();
        }
        log.info("Finished repacking npc options.");
    }

    private static void parse(final File folder) {
        File f = null;
        try {
            for (final java.io.File file : folder.listFiles()) {
                f = file;
                if (file.getPath().endsWith("exclude")) {
                    continue;
                }
                if (file.isDirectory()) {
                    parse(file);
                } else {
                    if (!Files.getFileExtension(file.getName()).equals("toml")) {
                        continue;
                    }

                    final String fileString = FileUtils.readFileToString(file, Charsets.UTF_8)
                            .replace("%SERVER_NAME%", GameConstants.SERVER_NAME);

                    final com.moandjiezana.toml.Toml toml = new Toml().read(fileString);
                    if (file.getPath().startsWith(Paths.get("assets", "types", "component").toString())) {
                        final mgi.tools.parser.TypeReader reader = TypeReader.readers.get("component");
                        definitions.addAll(reader.read(toml));
                    } else {
                        for (final java.util.Map.Entry<java.lang.String, java.lang.Object> entry : toml.entrySet()) {
                            final mgi.tools.parser.TypeReader reader = TypeReader.readers.get(entry.getKey());
                            if (reader == null) {
                                System.err.println(TypeReader.readers);
                                throw new RuntimeException("Could not find a reader for: " + entry.getKey());
                            }
                            final java.lang.Object value = entry.getValue();
                            final java.util.ArrayList<com.moandjiezana.toml.Toml> types = new ArrayList<Toml>();
                            if (value instanceof Toml) {
                                types.add((Toml) value);
                            } else {
                                types.addAll((ArrayList<Toml>) value);
                            }
                            for (final com.moandjiezana.toml.Toml type : types) {
                                final java.util.Map<java.lang.String, java.lang.Object> properties = type.toMap();
                                definitions.addAll(reader.read(properties));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Something went wrong in " + f.getPath());
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void pack(final Class<?>... types) {
        final java.util.ArrayList<mgi.types.Definitions> filtered = definitions.stream().filter(d -> ArrayUtils.contains(types, d.getClass())).collect(Collectors.toCollection(ArrayList::new));
        filtered.forEach(Definitions::pack);
        if (!filtered.isEmpty()) {
            log.info("Finished packing " + filtered.size() + " type" + (filtered.size() == 1 ? "" : "s."));
        }
    }

    private static void packClientBackground() throws IOException {
        final byte[] desktop = java.nio.file.Files.readAllBytes(Paths.get("assets/sprites/background/background_desktop.png"));
        final byte[] mobile = java.nio.file.Files.readAllBytes(Paths.get("assets/sprites/background/background_mobile.png"));
        final byte[] logo = java.nio.file.Files.readAllBytes(Paths.get("assets/sprites/background/background_logo.png"));
        final mgi.tools.jagcached.cache.Cache cache = Game.getCacheMgi();
        final mgi.tools.jagcached.cache.Archive desktopArchive = cache.getArchive(ArchiveType.BINARY);
        desktopArchive.findGroupByID(0).findFileByID(0).setData(new ByteBuffer(desktop));
        final mgi.tools.jagcached.cache.Group mobileArhive = desktopArchive.findGroupByID(2);
        mobileArhive.findFileByID(0).setData(new ByteBuffer(mobile));
        mobileArhive.addFile(new mgi.tools.jagcached.cache.File(new ByteBuffer(logo)));
        mgi.types.sprite.SpriteGroupDefinitions sprite;

        sprite = SpriteGroupDefinitions.get(499);
        sprite.setImage(0, ImageIO.read(new File("./assets/sprites/inferno_loginscreen/sprite_499_0.png")));
        sprite.pack();

        sprite = SpriteGroupDefinitions.get(500);
        sprite.setImage(0, ImageIO.read(new File("./assets/sprites/inferno_loginscreen/sprite_500_0.png")));
        sprite.pack();

        sprite = SpriteGroupDefinitions.get(811);
        sprite.setImage(0, ImageIO.read(new File("./assets/sprites/inferno_loginscreen/sprite_811_0.png")));
        sprite.setImage(1, ImageIO.read(new File("./assets/sprites/inferno_loginscreen/sprite_811_1.png")));
        sprite.pack();

        sprite = SpriteGroupDefinitions.get(818);
        sprite.setImage(0, ImageIO.read(new File("./assets/sprites/inferno_loginscreen/sprite_818_0.png")));
        sprite.pack();
    }

    private static void packHighRevision() throws IOException {
        //new DiceBagPacker().pack();
        //new TrickPacker().pack();
        new ThanksgivingPacker().pack();
        new ChristmasMapPacker().pack();
        new HighDefinitionPets().packFull();
        new EasterMapPacker().packAll();
        new CustomTeleport().packAll();
        new TrickEmote().packAll();
        new DiceBag().packAll();
        //new MusicEnumPacker().pack();
        FramePacker.write();
        AnimationBase.pack();
        //new HalloweenMapPacker().pack();
    }

    private static void packDynamicConfigs() {
        EnumDefinitions enumDef;
        enumDef = new EnumDefinitions();
        enumDef.setId(1974);
        enumDef.setKeyType("int");
        enumDef.setValueType("namedobj");
        enumDef.setDefaultInt(-1);
        enumDef.setValues(new HashMap<>());
        int id = 0;
        for (final com.zenyte.game.ui.testinterfaces.BountyHunterStoreInterface.Reward reward : BountyHunterStoreInterface.Reward.values()) {
            enumDef.getValues().put(id++, reward.getId());
        }
        definitions.add(enumDef);
        final com.zenyte.game.content.achievementdiary.Diary[][] diaries = AchievementDiaries.ALL_DIARIES;
        for (final com.zenyte.game.content.achievementdiary.Diary[] diaryEnum : diaries) {
            final java.util.HashMap<java.lang.Integer, java.lang.Object> values = new HashMap<Integer, Object>();
            DiaryArea area = null;
            for (final com.zenyte.game.content.achievementdiary.Diary diary : diaryEnum) {
                if (diary.autoCompleted()) {
                    continue;
                }
                final com.zenyte.game.content.achievementdiary.Diary.DiaryComplexity complexity = diary.type();
                area = diary.area();
                values.put(complexity.ordinal(), (int) (values.get(complexity.ordinal()) == null ? 0 : values.get(complexity.ordinal())) + 1);
            }
            enumDef = new EnumDefinitions();
            enumDef.setId(2501 + area.getIndex());
            enumDef.setKeyType("int");
            enumDef.setValueType("int");
            enumDef.setDefaultInt(-1);
            enumDef.setValues(values);
            definitions.add(enumDef);
        }
    }

    private static void packModels() {
        try {
            packModel(38000, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte_portal_model.dat")));
            packModel(38001, java.nio.file.Files.readAllBytes(Paths.get("assets/models/tournament_supplies.dat")));
            packModel(38002, java.nio.file.Files.readAllBytes(Paths.get("assets/models/pets/cute_creature.dat")));
            packModel(38003, java.nio.file.Files.readAllBytes(Paths.get("assets/models/pets/stray_dog.dat")));
            packModel(38004, java.nio.file.Files.readAllBytes(Paths.get("assets/models/pets/evil_creature.dat")));
            packModel(38005, java.nio.file.Files.readAllBytes(Paths.get("assets/models/bonds/cyan_bond.dat")));
            packModel(38006, java.nio.file.Files.readAllBytes(Paths.get("assets/models/bonds/red_bond.dat")));
            packModel(50000, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte_teletab_50000.dat")));
            packModel(50001, java.nio.file.Files.readAllBytes(Paths.get("assets/models/healing fountain.dat")));
            packModel(52505, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte armour/Zenyte Boots(drop)b.dat")));
            packModel(52506, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte armour/Zenyte Bootsb.dat")));
            packModel(52507, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte armour/Zenyte Cape(drop)b.dat")));
            packModel(52508, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte armour/Zenyte Capeb.dat")));
            packModel(52509, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte armour/Zenyte Gloves(drop)b.dat")));
            packModel(52510, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte armour/Zenyte Glovesb.dat")));
            packModel(52511, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte armour/Zenyte Helmet(drop)b.dat")));
            packModel(52512, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte armour/Zenyte Helmetb.dat")));
            packModel(52513, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte armour/Zenyte Platebody(drop)b.dat")));
            packModel(52514, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte armour/Zenyte Platebodyb.dat")));
            packModel(52515, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte armour/Zenyte Platelegs(drop)b.dat")));
            packModel(52516, java.nio.file.Files.readAllBytes(Paths.get("assets/models/zenyte armour/Zenyte Platelegsb.dat")));
            packModel(52517, java.nio.file.Files.readAllBytes(Paths.get("assets/models/starter/Starter bow ground.dat")));
            packModel(52518, java.nio.file.Files.readAllBytes(Paths.get("assets/models/starter/Starter bow.dat")));
            packModel(52519, java.nio.file.Files.readAllBytes(Paths.get("assets/models/starter/Starter staff ground.dat")));
            packModel(52520, java.nio.file.Files.readAllBytes(Paths.get("assets/models/starter/Starter staff.dat")));
            packModel(52521, java.nio.file.Files.readAllBytes(Paths.get("assets/models/starter/Starter sword ground.dat")));
            packModel(52522, java.nio.file.Files.readAllBytes(Paths.get("assets/models/starter/Starter sword.dat")));
            packModel(52523, java.nio.file.Files.readAllBytes(Paths.get("assets/models/Rare drop table.dat")));
            //god tbow
            packModel(65266, java.nio.file.Files.readAllBytes(Paths.get("assets/models/65266.dat")));
            packModel(65267, java.nio.file.Files.readAllBytes(Paths.get("assets/models/65267.dat")));
            //Jonas
            packModel(52524, java.nio.file.Files.readAllBytes(Paths.get("assets/halloween/staticmodels/jonas/34041.dat")));
            packModel(52525, java.nio.file.Files.readAllBytes(Paths.get("assets/halloween/staticmodels/jonas/34044.dat")));
            packModel(52526, java.nio.file.Files.readAllBytes(Paths.get("assets/halloween/staticmodels/jonas/34046.dat")));
            packModel(52527, java.nio.file.Files.readAllBytes(Paths.get("assets/halloween/staticmodels/jonas/34047.dat")));
            //Grim reaper
            packModel(52528, java.nio.file.Files.readAllBytes(Paths.get("assets/halloween/staticmodels/grim reaper/28985.dat")));
            packModel(52529, java.nio.file.Files.readAllBytes(Paths.get("assets/halloween/staticmodels/grim reaper/34166.dat")));
            packModel(52530, java.nio.file.Files.readAllBytes(Paths.get("assets/halloween/staticmodels/grim reaper/34167.dat")));
            //Thanksgiving
            packModel(52531, java.nio.file.Files.readAllBytes(Paths.get("assets/models/thanksgiving/thanksgiving turkey model.dat")));
            packModel(52532, java.nio.file.Files.readAllBytes(Paths.get("assets/models/thanksgiving/thanksgiving poof model.dat")));
            //Christmas scythe
            packModel(52533, java.nio.file.Files.readAllBytes(Paths.get("assets/models/christmas scythe inv.dat")));
            packModel(52534, java.nio.file.Files.readAllBytes(Paths.get("assets/models/christmas scythe wield.dat")));
            packModel(2450, java.nio.file.Files.readAllBytes(Paths.get("assets/models/Treasure trails reward casket.dat")));
            if (Constants.CHRISTMAS) {
                Iterator<File> it = FileUtils.iterateFiles(new File("assets/christmas/christmas-y entities models/"), null, false);
                final it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap<java.io.File> sortedMap = new Int2ObjectAVLTreeMap<File>();
                while (it.hasNext()) {
                    final java.io.File file = it.next();
                    final int originalId = Integer.parseInt(file.getName().replace(".dat", ""));
                    sortedMap.put(originalId, file);
                }
                for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<java.io.File> entry : sortedMap.int2ObjectEntrySet()) {
                    final java.io.File file = entry.getValue();
                    final byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
                    packModel(Integer.parseInt(file.getName().replace(".dat", "")), bytes);
                }
            }
            //Scroll boxes
            packModel(53000, java.nio.file.Files.readAllBytes(Paths.get("assets/models/scroll boxes/39028.dat")));
            packModel(53001, java.nio.file.Files.readAllBytes(Paths.get("assets/models/scroll boxes/39029.dat")));
            packModel(53002, java.nio.file.Files.readAllBytes(Paths.get("assets/models/scroll boxes/39030.dat")));
            packModel(53003, java.nio.file.Files.readAllBytes(Paths.get("assets/models/scroll boxes/39031.dat")));
            packModel(53004, java.nio.file.Files.readAllBytes(Paths.get("assets/models/scroll boxes/39032.dat")));
            packModel(53005, java.nio.file.Files.readAllBytes(Paths.get("assets/models/scroll boxes/39033.dat")));
            packModel(57577, java.nio.file.Files.readAllBytes(Paths.get("assets/models/clue progresser/1.dat")));
            packModel(57578, java.nio.file.Files.readAllBytes(Paths.get("assets/models/clue progresser/2.dat")));
            packModel(57579, java.nio.file.Files.readAllBytes(Paths.get("assets/models/clue progresser/3.dat")));
            packModel(57580, java.nio.file.Files.readAllBytes(Paths.get("assets/models/clue progresser/4.dat")));
            packModel(57581, java.nio.file.Files.readAllBytes(Paths.get("assets/models/clue progresser/5.dat")));
            TypeParser.packModel(57576, org.apache.commons.compress.utils.IOUtils.toByteArray(new FileInputStream(new File("assets/dice bag/item_model.dat"))));
        } catch (IOException e) {
            log.error(Strings.EMPTY, e);
        }
    }

    public static void packModel(final int id, final byte[] bytes) {
        Game.getCacheMgi().getArchive(ArchiveType.MODELS).addGroup(new Group(id, new mgi.tools.jagcached.cache.File(new ByteBuffer(bytes))));
    }

    public static void packSound(final int id, final byte[] bytes) {
        Game.getCacheMgi().getArchive(ArchiveType.SYNTHS).addGroup(new Group(id, new mgi.tools.jagcached.cache.File(new ByteBuffer(bytes))));
    }

    private static void packClientScripts() throws IOException {
        packClientScript(73, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/bank_command/73.cs2")));
        packClientScript(386, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/tourny_fog/386.cs2")));
        packClientScript(393, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/skill_tab_construction/393.cs2")));
        packClientScript(395, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/skill_tab_construction/395.cs2")));
        packClientScript(687, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/ironman_setup/687.cs2")));
        packClientScript(1004, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/experience_drops_multiplier.cs2")));
        packClientScript(1261, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/tourny_fog/1261.cs2")));
        packClientScript(1705, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/edgeville_map_link/1705.cs2")));
        packClientScript(2066, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/broadcast_custom_links/2066.cs2")));
        packClientScript(2094, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/ironman_setup/2094.cs2")));
        packClientScript(2096, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/ironman_setup/2096.cs2")));
        packClientScript(2186, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/tourny_viewer/2186.cs2")));
        packClientScript(2200, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/achievement_diary_sizes/2200.cs2")));
        packClientScript(699, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/emote_tab/699.cs2")));
        packClientScript(701, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/emote_tab/701.cs2")));
        packClientScript(702, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/emote_tab/702.cs2")));
        for (int id = 3500; id <= 3505; id++) {
            packClientScript(id, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/game_noticeboard/" + id + ".cs2")));
        }
        for (int id = 0; id < 16; id++) {
            packClientScript(10000 + id, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/teleport_menu/new/" + (10000 + id))));
        }
        packClientScript(10100, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/ironman_setup/10100.cs2")));
        for (int i = 10034; i <= 10048; i++) {
            packClientScript(i, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/wheel_of_fortune/" + i + ".cs2")));
        }
        for (int id = 10102; id <= 10121; id++) {
            packClientScript(id, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/drop_viewer/" + id + ".cs2")));
        }
        for (int id = 10200; id <= 10202; id++) {
            packClientScript(id, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/game_settings/" + id + ".cs2")));
        }
        for (int id = 10300; id <= 10306; id++) {
            packClientScript(id, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/daily_challenges/" + id + ".cs2")));
        }
        for (int id = 10400; id <= 10405; id++) {
            packClientScript(id, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/tourny_info/" + id + ".cs2")));
        }
        for (int id = 10500; id <= 10518; id++) {
            packClientScript(id, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/tourny_presets/" + id + ".cs2")));
        }
        packClientScript(10600, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/tourny_viewer/10600.cs2")));
        packClientScript(10700, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/hide_roofs/10700.cs2")));
        for (int i = 10800; i <= 10810; i++) {
            packClientScript(i, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/ge_offers/" + i + ".cs2")));
        }
        packClientScript(336, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/godwars_dungeon/336.cs2")));
        packClientScript(342, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/godwars_dungeon/342.cs2")));
        for (int i = 10900; i <= 10912; i++) {
            packClientScript(i, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/eco_presets/" + i + ".cs2")));
        }
        packClientScript(1311, java.nio.file.Files.readAllBytes(Paths.get("assets/cs2/tog_sidepanel_timer.cs2")));
    }

    public static void packClientScript(final int id, final byte[] bytes) {
        Game.getCacheMgi().getArchive(ArchiveType.CLIENTSCRIPTS).addGroup(new Group(id, new mgi.tools.jagcached.cache.File(new ByteBuffer(bytes))));
    }

    private static void packInterfaces() throws IOException {
        final mgi.tools.jagcached.cache.Cache cache = Game.getCacheMgi();
        final mgi.tools.jagcached.cache.Group group = new Group(700);
        for (int i = 0; i < 38; i++) {
            group.addFile(new mgi.tools.jagcached.cache.File(new ByteBuffer(java.nio.file.Files.readAllBytes(Paths.get("assets/interfaces/700/" + i)))));
        }
        cache.getArchive(ArchiveType.INTERFACES).addGroup(group);
    }

    public static void packMap(final int id, final byte[] landscape, final byte[] map) {
        final mgi.tools.jagcached.cache.Cache cache = Game.getCacheMgi();
        final mgi.tools.jagcached.cache.Archive archive = cache.getArchive(ArchiveType.MAPS);
        final int[] xteas = XTEALoader.getXTEAs(id);
        final int regionX = id >> 8;
        final int regionY = id & 255;
        final mgi.tools.jagcached.cache.Group mapGroup = archive.findGroupByName("m" + regionX + "_" + regionY);
        final mgi.tools.jagcached.cache.Group landGroup = archive.findGroupByName("l" + regionX + "_" + regionY, xteas);
        if (map != null) {
            if (landGroup != null) {
                landGroup.findFileByID(0).setData(new ByteBuffer(map));
            } else {
                final mgi.tools.jagcached.cache.Group newLandGroup = new Group(archive.getFreeGroupID(), new mgi.tools.jagcached.cache.File(new ByteBuffer(map)));
                newLandGroup.setName("l" + regionX + "_" + regionY);
                archive.addGroup(newLandGroup);
            }
        }
        if (landscape != null) {
            if (mapGroup != null) {
                mapGroup.findFileByID(0).setData(new ByteBuffer(landscape));
            } else {
                final mgi.tools.jagcached.cache.Group newMapGroup = new Group(archive.getFreeGroupID() + 1, new mgi.tools.jagcached.cache.File(new ByteBuffer(landscape)));
                newMapGroup.setName("m" + regionX + "_" + regionY);
                newMapGroup.setXTEA(xteas);
                archive.addGroup(newMapGroup);
            }
        }
    }

    private static void packMaps() throws IOException {
        packMap(9261, java.nio.file.Files.readAllBytes(Paths.get("assets/map/island_l_regular.dat")), MapUtils.inject(java.nio.file.Files.readAllBytes(Paths.get("assets/map/island_m_regular.dat")), o -> {
            if (o.getId() == 46087) {
                o.setId(46089);
            }
            return false;
        }));
        packMap(10388, java.nio.file.Files.readAllBytes(Paths.get("assets/map/yanille/328.dat")), java.nio.file.Files.readAllBytes(Paths.get("assets/map/yanille/329.dat")));
        packMap(11567, null, MapUtils.inject(11567, null, new WorldObject(187, 10, 1, new Location(2919, 3054, 0))));
        packMap(11595, null, MapUtils.inject(11595, null, new WorldObject(26254, 10, 0, new Location(2931, 4822, 0)), new WorldObject(26254, 10, 0, new Location(2896, 4821, 0)), new WorldObject(26254, 10, 1, new Location(2900, 4845, 0)), new WorldObject(26254, 10, 3, new Location(2920, 4848, 0))));
        packMap(12085, java.nio.file.Files.readAllBytes(Paths.get("assets/map/edge/110.dat")), java.nio.file.Files.readAllBytes(Paths.get("assets/map/edge/111.dat")));
        packMap(12086, java.nio.file.Files.readAllBytes(Paths.get("assets/map/edge/112.dat")), java.nio.file.Files.readAllBytes(Paths.get("assets/map/edge/113.dat")));
        packMap(12341, java.nio.file.Files.readAllBytes(Paths.get("assets/map/edge/622.dat")), java.nio.file.Files.readAllBytes(Paths.get("assets/map/edge/623.dat")));
        packMap(12342, java.nio.file.Files.readAllBytes(Paths.get("assets/map/edge/624.dat")), java.nio.file.Files.readAllBytes(Paths.get("assets/map/edge/625.dat")));
        packMap(14477, java.nio.file.Files.readAllBytes(Paths.get("assets/map/dmm_tourny/m56_141.dat")), java.nio.file.Files.readAllBytes(Paths.get("assets/map/dmm_tourny/l56_141.dat")));
        packMap(14478, java.nio.file.Files.readAllBytes(Paths.get("assets/map/dmm_tourny/m56_142.dat")), java.nio.file.Files.readAllBytes(Paths.get("assets/map/dmm_tourny/l56_142.dat")));
        packMap(14733, java.nio.file.Files.readAllBytes(Paths.get("assets/map/dmm_tourny/m57_141.dat")), java.nio.file.Files.readAllBytes(Paths.get("assets/map/dmm_tourny/l57_141.dat")));
        packMap(14734, java.nio.file.Files.readAllBytes(Paths.get("assets/map/dmm_tourny/m57_142.dat")), java.nio.file.Files.readAllBytes(Paths.get("assets/map/dmm_tourny/l57_142.dat")));
        packMap(15245, java.nio.file.Files.readAllBytes(Paths.get("assets/map/tournament/2.dat")), java.nio.file.Files.readAllBytes(Paths.get("assets/map/tournament/3.dat")));
        packMap(15248, java.nio.file.Files.readAllBytes(Paths.get("assets/map/tournament/0.dat")), MapUtils.inject(java.nio.file.Files.readAllBytes(Paths.get("assets/map/tournament/1.dat")), null, new WorldObject(35005, 10, 3, new Location(3806, 9245, 0)), new WorldObject(35006, 10, 1, new Location(3813, 9256, 0)), new WorldObject(35007, 10, 0, new Location(3799, 9256, 0))));
        packMap(13139, null, MapUtils.inject(13139, null, new WorldObject(35020, 10, 0, new Location(3279, 5345, 2)), new WorldObject(35020, 10, 0, new Location(3312, 5344, 2))));
        packMap(13395, null, MapUtils.inject(13395, null, new WorldObject(35020, 10, 0, new Location(3343, 5346, 2))));
        packMap(4674, java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Armadyl/0.dat")), MapUtils.inject(java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Armadyl/1.dat")), o -> {
            if (o.getId() == 20843) {
                o.setId(35016);
            } else if (o.getId() == 26769) {
                o.setId(35013);
            } else if (o.getId() == 23708) {
                o.setId(35019);
            }
            return false;
        }));
        packMap(4675, java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Bandos/0.dat")), MapUtils.inject(java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Bandos/1.dat")), o -> {
            if (o.getId() == 9368) {
                o.setId(35014);
            } else if (o.getId() == 26769) {
                o.setId(35013);
            } else if (o.getId() == 23708) {
                o.setId(35019);
            }
            return o.hashInRegion() == new Location(1191, 4306, 0).hashInRegion();
        }, new WorldObject(35019, 10, 0, new Location(1189, 4313, 0))));
        packMap(4676, java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Zamorak/0.dat")), MapUtils.inject(java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Zamorak/1.dat")), o -> {
            if (o.getId() == 14845) {
                o.setId(35015);
            } else if (o.getId() == 26769) {
                o.setId(35013);
            } else if (o.getId() == 23708) {
                o.setId(35019);
            }
            //Removes object which produces ambient waterfall sound and the stash unit.
            return o.getId() == 16399 || o.getId() == 29054;
        }));
        packMap(4677, java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Saradomin/0.dat")), MapUtils.inject(java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Saradomin/1.dat")), o -> {
            if (o.getId() == 26740) {
                o.setId(35017);
            } else if (o.getId() == 21120) {
                o.setId(35018);
            } else if (o.getId() == 23708) {
                o.setId(35019);
            }
            return o.getId() == 26375 || (o.getXInRegion() == (1203 & 63) && o.getYInRegion() == (4422 & 63));
        }, new WorldObject(17030, 22, 0, 1195, 4440, 0)));
        packMap(11346, java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Armadyl/1858.dat")), MapUtils.inject(java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Armadyl/1859.dat")), o -> {
            if (o.getId() == 20843) {
                o.setId(35016);
            } else if (o.getId() == 26769) {
                o.setId(35013);
            } else if (o.getId() == 23708) {
                o.setId(35019);
            }
            return false;
        }, new WorldObject(26502, 10, 3, 2839, 5295, 2), new WorldObject(0, 10, 0, 2840, 5294, 2), new WorldObject(0, 10, 0, 2838, 5294, 2)));
        packMap(11347, java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Bandos/1860.dat")), MapUtils.inject(java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Bandos/1861.dat")), o -> {
            if (o.getId() == 9368) {
                o.setId(35014);
            } else if (o.getId() == 26769) {
                o.setId(35013);
            } else if (o.getId() == 23708) {
                o.setId(35019);
            }
            return o.hashInRegion() == new Location(2856, 5357, 2).hashInRegion();
        }, new WorldObject(35019, 10, 0, new Location(2854, 5364, 2))));
        packMap(11602, java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Saradomin/1862.dat")), MapUtils.inject(java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Saradomin/1863.dat")), o -> {
            if (o.getId() == 26740) {
                o.setId(35017);
            } else if (o.getId() == 21120) {
                o.setId(35018);
            } else if (o.getId() == 23708) {
                o.setId(35019);
            }
            return false;
        }, new WorldObject(17030, 22, 0, 2923, 5272, 0)));
        packMap(11603, MapUtils.processTiles(new ByteBuffer(java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Zamorak/1856.dat"))), tile -> {
            if (tile.getUnderlayId() == 23) {
                tile.setUnderlayId((byte) 0);
            }
            if (tile.getOverlayId() == 33) {
                tile.setOverlayId((byte) 0);
            }
        }).getBuffer(), MapUtils.inject(java.nio.file.Files.readAllBytes(Paths.get("assets/map/godwars-instances/Zamorak/1857.dat")), o -> {
            if (o.getId() == 14845) {
                o.setId(35015);
            } else if (o.getId() == 26769) {
                o.setId(35013);
            } else if (o.getId() == 23708) {
                o.setId(35019);
            }
            return false;
        }));
        final mgi.types.worldmap.WorldMapDefinitions godwarsDefs = WorldMapDefinitions.decode("godwars");
        godwarsDefs.updateFullChunks(11602, 11601, 0, 1, 4);
        godwarsDefs.updateFullChunks(11603, 2);
        godwarsDefs.updateFullChunks(11346, 2);
        godwarsDefs.updateFullChunks(11347, 2);
        godwarsDefs.encode("godwars");
        final mgi.types.worldmap.WorldMapDefinitions defs = WorldMapDefinitions.decode("main");
        defs.update(9261, 0);
        defs.update(12342, 0);
        defs.encode("main");
    }

    public static List<Definitions> getDefinitions() {
        return TypeParser.definitions;
    }
}
