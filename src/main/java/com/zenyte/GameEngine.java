package com.zenyte;

import com.zenyte.api.client.APIClient;
import com.zenyte.api.client.query.ApiPing;
import com.zenyte.cores.CoresManager;
import com.zenyte.game.constants.GameConstants;
import com.zenyte.game.content.consumables.Consumable;
import com.zenyte.game.content.grandexchange.GrandExchangeHandler;
import com.zenyte.game.content.multicannon.DwarfMulticannon;
import com.zenyte.game.content.skills.agility.AgilityManager;
import com.zenyte.game.content.skills.mining.MiningDefinitions;
import com.zenyte.game.parser.impl.NPCExamineLoader;
import com.zenyte.game.shop.Shop;
import com.zenyte.game.ui.testinterfaces.DropViewerInterface;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.npc.combatdefs.NPCCDLoader;
import com.zenyte.game.world.entity.npc.drop.matrix.NPCDrops;
import com.zenyte.game.world.entity.npc.spawns.NPCSpawnLoader;
import com.zenyte.game.world.flooritem.GlobalItem;
import com.zenyte.game.world.info.WorldProfile;
import com.zenyte.game.world.object.Door;
import com.zenyte.game.world.object.ObjectExamineLoader;
import com.zenyte.game.world.region.GlobalAreaManager;
import com.zenyte.game.world.region.XTEALoader;
import com.zenyte.plugins.PluginManager;
import com.zenyte.plugins.events.ServerLaunchEvent;
import com.zenyte.utils.Discord;
import com.zenyte.utils.MultiwayArea;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.SneakyThrows;
import mgi.types.Definitions;
import mgi.types.config.items.ItemDefinitions;
import mgi.types.config.npcs.NPCDefinitions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.openhft.chronicle.core.Jvm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 * A class to start both the file and game servers.
 *
 * @author Tom
 */
public class GameEngine {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GameEngine.class);
    public static final long SERVER_START_TIME = System.nanoTime();
    private static boolean loaded;
    private static final List<Runnable> postServerLoadTasks = new ArrayList<>();

    public static void appendPostLoadTask(final Runnable task) {
        if (loaded) {
            throw new IllegalStateException();
        }
        postServerLoadTasks.add(task);
    }

    public static final Logger logger = LoggerFactory.getLogger("Default logger");

    static {
        Jvm.init();
    }

    /**
     * The entry point of the application.
     *
     * @param args The command line arguments.
     */
    //@SneakyThrows
   // @SneakyThrows
    public static void main(final String[] args) {
        final long serverStartTime = System.nanoTime();
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        log.info("Starting " + GameConstants.SERVER_NAME + "...");
        try {
            if (args.length > 0) {
                Constants.WORLD_PROFILE = new WorldProfile(args[0]);
            } else {
                Constants.WORLD_PROFILE = new WorldProfile("offline_dev");
            }
            log.info("Loaded world: \'" + Constants.WORLD_PROFILE.getKey() + "\'");
            log.info("  name: " + Constants.WORLD_PROFILE.getKey());
            log.info("  host: " + Constants.WORLD_PROFILE.getHost());
            log.info("  port: " + Constants.WORLD_PROFILE.getPort());
            log.info("  activity: \"" + Constants.WORLD_PROFILE.getActivity() + "\"");
            log.info("  private: " + Constants.WORLD_PROFILE.isPrivate());
            log.info("  development: " + Constants.WORLD_PROFILE.isDevelopment());
            log.info("  verify passwords: " + Constants.WORLD_PROFILE.isVerifyPasswords());
            log.info("  location: " + Constants.WORLD_PROFILE.getLocation());
            log.info("  flags: " + Constants.WORLD_PROFILE.getFlags());
            log.info("  api:");
            log.info("    enabled: " + Constants.WORLD_PROFILE.getApi().isEnabled());
            log.info("    scheme: " + Constants.WORLD_PROFILE.getApi().getScheme());
            log.info("    host: " + Constants.WORLD_PROFILE.getApi().getHost());
            log.info("    port: " + Constants.WORLD_PROFILE.getApi().getPort());
            log.info("    token: " + Constants.WORLD_PROFILE.getApi().getToken().substring(0, 7) + "...");
        } catch (IOException e) {
            log.error("Failed to load world profile!", e);
            return;
        }
        final java.util.concurrent.ForkJoinPool fork = ForkJoinPool.commonPool();
        final java.util.ArrayList<java.util.concurrent.Callable<java.lang.Void>> list = new ArrayList<Callable<Void>>();
        Server.PORT = Constants.WORLD_PROFILE.getPort();
        fork.submit(CoresManager::init);
        try {
            fork.submit(Game::load).get();
            fork.submit((Runnable) ItemDefinitions::loadDefinitions).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failure submitting cache loading.", e);
            System.exit(-1);
        }
        log.info("Creating game engine...");
        list.add(callable(World::init));
        list.add(callable(Consumable::initialize));
        list.add(callable(GlobalItem::load));
        fork.invokeAll(list);
        list.clear();
        log.info("Loading game.");
        load();
        PluginManager.post(new ServerLaunchEvent());
        loaded = true;
        for (final java.lang.Runnable task : postServerLoadTasks) {
            task.run();
        }
        if (Constants.WORLD_PROFILE.getApi().isEnabled()) {
            final boolean successful = new ApiPing().execute();
            if (successful) {
                log.info("Received ping response from api server.");
                log.info("Starting api tasks...");
                APIClient.startTasks();
            } else {
                log.error("Failed to ping api server!");
                System.exit(-1);
            }
        }
        log.info("Server took " + (Utils.nanoToMilli(System.nanoTime() - serverStartTime)) + " milliseconds to launch.");
        log.info("Ready. Server is listening on " + Server.PORT + ".");
        //Discord.writeServerStatus("__** Endless-Os Server Started up in " +  + (Utils.nanoToMilli(System.nanoTime() - serverStartTime)) + " ms **__ ");

        log.info("attempting to start JDA: ");
        /*try {
            EmbedBuilder db = new EmbedBuilder();
            db.setTitle("Endless Server Status");
            db.setDescription("@here Server is now online!");
            db.setImage("https://endless-os.com/logo.png");
            db.setColor(new java.awt.Color(0xB00D03));
            Discord.getJDA().getTextChannelById("1064974101611040829").sendMessageEmbeds(db.build()).queue();
        } catch (final Exception e) {
            log.error("Error starting " + GameConstants.SERVER_NAME + " Discord Bot.", e);
            System.exit(1);
        }*/

        fork.submit(() -> {
            final java.io.File file = new File("data/logs/error.log");
            if (!file.exists()) {
                return;
            }
            if (file.length() > 0) {
                System.err.println("Some errors from previous session(s) are logged at /data/logs/error.log; review and delete them.");
            }
        });
    }

    private static void load() {
        try {
            final java.util.concurrent.ForkJoinPool pool = ForkJoinPool.commonPool();
            log.info("Submitting binding task; Binding to port: " + Server.PORT);
            final java.util.concurrent.ForkJoinTask<?> bindTask = pool.submit(() -> {
                try {
                    Server.bind(Server.PORT);
                    log.info("Bound to port: " + Server.PORT);
                } catch (final Exception e) {
                    log.error("Error starting " + GameConstants.SERVER_NAME + ".", e);
                    System.exit(1);
                }
            });
            final java.util.ArrayList<java.util.concurrent.Callable<java.lang.Void>> list = new ArrayList<Callable<Void>>();
            for (final java.lang.Class<?> clazz : Definitions.LOW_PRIORITY_DEFINITIONS) {
                list.add(callable(Definitions.load(clazz)));
            }
            pool.invokeAll(list);
            list.clear();
            log.info("Loading npc combat definitions.");
            pool.invokeAll(Arrays.asList(callable(NPCCDLoader::parse), callable(MiningDefinitions::load), callable(DwarfMulticannon::init), callable(AgilityManager::init), callable(NPCSpawnLoader::parseDefinitions)));
            log.info("Loading common world tasks.");
            pool.submit(World::initTasks).get();
            log.info("Loading shops, examines, drops and grand exchange.");
            pool.invokeAll(Arrays.asList(callable(Shop::load), callable(NPCExamineLoader::loadExamines), callable(ObjectExamineLoader::loadExamines), callable(NPCDrops::init), callable(GrandExchangeHandler::init), callable(Door::load)));
            pool.submit(() -> {
                try {
                    XTEALoader.load();
                } catch (Throwable throwable) {
                    log.error("Failure to load XTEAs.", throwable);
                }
            }).get();
            log.info("Loading plugins.");
            pool.submit(() -> new Scanner().scan()).get();
            log.info("Loading area inheritance.");
            pool.submit(GlobalAreaManager::setInheritance).get();
            log.info("Loading NPC spawns.");
            pool.invokeAll(Arrays.asList(callable(NPCDefinitions::filter), callable(NPCSpawnLoader::loadNPCSpawns)));
            pool.submit(DropViewerInterface::populateDropViewerData);
            log.info("Submitting multiway area mapping and area intersection verification.");
            pool.submit(MultiwayArea::loadAndMap);
            pool.submit(GlobalAreaManager::checkIntersections);
            pool.submit(GlobalAreaManager::map);
            log.info("Launching login manager.");
            CoresManager.getBackupManager().launch();
            CoresManager.getLoginManager().launch();
            if (!bindTask.isDone()) {
                try {
                    bindTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Failure binding the port", e);
                    System.exit(-1);
                    return;
                }
            }
        } catch (final Exception e) {
            log.error("Exception loading game", e);
        }
        if (Constants.CYCLE_DEBUG) {
            //TODO
            final it.unimi.dsi.fastutil.ints.IntOpenHashSet regionList = new IntOpenHashSet(2000);
            /*val index = Game.getLibrary().getIndex(5);
            for (int rx = 0; rx < 100; rx++) {
                for (int ry = 0; ry < 256; ry++) {
                    val id = index.getArchiveId("m" + (rx) + "_" + ry);
                    if (id != -1) {
                        regionList.add(rx << 8 | ry);
                    }
                }
            }*/
            final java.util.ArrayList<java.util.concurrent.Callable<java.lang.Void>> taskList = new ArrayList<Callable<Void>>(2000);
            for (final java.lang.Integer region : regionList) {
                taskList.add(() -> {
                    World.loadRegion(region);
                    return null;
                });
            }
            ForkJoinPool.commonPool().invokeAll(taskList);
        }
    }

    private static Callable<Void> callable(final Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("Failure loading callable: ", e);
            }
            return null;
        };
    }
}
