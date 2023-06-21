package com.zenyte.game.world.entity.player.login;

import com.zenyte.Constants;
import com.zenyte.cores.CoresManager;
import com.zenyte.game.content.clans.ClanManager;
import com.zenyte.game.content.grandexchange.GrandExchangeHandler;
import com.zenyte.game.parser.scheduled.ScheduledExternalizableManager;
import com.zenyte.game.world.entity.player.punishments.PunishmentManager;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Kris | 15/05/2019 15:24
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class BackupManager {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BackupManager.class);
    /**
     * The directory for the backups folder.
     */
    static final String BACKUPS_DIR = "./data/backups/";
    /**
     * Whether or not to upload the backups to the AWS storage; by default, only uploaded outside of development.
     */
    private static final boolean UPLOAD = !Constants.WORLD_PROFILE.isDevelopment();
    /**
     * Whether or not to make backups of all the files; By default, enabled for all.
     */
    private static final boolean BACKUP = true;
    /**
     * A dynamic variable that determines whether backups should be created or not; This does not fully cancel backups, but rather halt the process and keep the thread still
     * executing in the background.
     */
    public static boolean PAUSE_BACKUPS = false;
    /**
     * Interval in minutes how frequently backups should be executed, in {@link BackupManager#BACKUPS_TIMEUNIT}.
     */
    private static final int INTERVAL = 5;
    /**
     * The timeunit used for the interval for backups process.
     */
    private static final TimeUnit BACKUPS_TIMEUNIT = TimeUnit.MINUTES;
    /**
     * The directory for the temporary backups location to where all the backed-up files are going to be moved before they're backed up. The files only exist for the duration of the
     * backup process.
     */
    private static final String PENDING_BACKUPS_SAVE_DIRECTORY = BACKUPS_DIR + "pending/";
    /**
     * The player save directory that all serialized backup files are placed.
     */
    private static final String CHARACTER_BACKUPS_SAVE_DIRECTORY = BACKUPS_DIR + "pending/characters/";
    /**
     * The player save directory that all archived backup files are placed.
     */
    private static final String OTHER_BACKUPS_SAVE_DIRECTORY = BACKUPS_DIR + "pending/other/";

    static {
        new File(CHARACTER_BACKUPS_SAVE_DIRECTORY).mkdirs();
        new File(OTHER_BACKUPS_SAVE_DIRECTORY).mkdirs();
    }

    /**
     * Status of the backups - whether or not a backup is being written at the given time.
     */
    final MutableBoolean status = new MutableBoolean();
    /**
     * Status of the async deletion and upload process. If this is still on-going, the next backup will be skipped.
     * Once it reaches this stage, logins and logouts will be re-enabled.
     */
    private final MutableBoolean archivingStatus = new MutableBoolean();
    /**
     * The Zpaq tool instance that backs up the files.
     */
    private final Zpaq zpaq = new Zpaq();
    /**
     * The AWS instance that uploads all our backups.
     */
    private final AWS aws = new AWS();
    /**
     * The thread that executes the loading and saving of the accounts.
     */
    private Thread thread;
    private Future<?> uploadFuture;
    private boolean first;

    /**
     * Launches the backups process.
     */
    public void launch() {
        if (!BACKUP) {
            return;
        }
        thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(BACKUPS_TIMEUNIT.toMillis(!first ? 1 : INTERVAL));
                    first = true;
                    if (PAUSE_BACKUPS || status.isTrue()) {
                        continue;
                    }
                    status.setTrue();
                    //Sleep until login manager/grand exchange/scheduled externalizables finish loading/saving.
                    while (CoresManager.getLoginManager().status.isTrue() || GrandExchangeHandler.status.isTrue() || ScheduledExternalizableManager.status.isTrue()) {
                        Thread.sleep(1);
                    }
                    run();
                    status.setFalse();
                } catch (Exception e) {
                    log.error(Strings.EMPTY, e);
                }
            }
        });
        thread.start();
    }

    public void shutdown() {
        try {
            while (status.isTrue()) {
                Thread.sleep(1);
            }
            run();
            if (uploadFuture != null) {
                uploadFuture.get(10, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    /**
     * Launches the backups process; Backs up only the files that have changed since the last backup. Files are also uploaded to AWS is the option is enabled.
     */
    private void run() {
        if (!Constants.WORLD_PROFILE.isDevelopment()) {
        }
//            runAwsBackupShellScript();
//        checkIfInitialBackup();
//        val modifiedCharacters = CoresManager.getLoginManager().modifiedCharacters;
//        if (modifiedCharacters.isEmpty()) {
//            return;
//        }
//        if (archivingStatus.isTrue()) {
//            log.info("Unable to produce another backup as last archive process is still on-going.");
//            return;
//        }
//        archivingStatus.setTrue();
//        log.info("Backing up " + modifiedCharacters.size() + " character" + (modifiedCharacters.size() == 1 ?
//                                                                             Strings.EMPTY :
//                                                                             "s") + ".");
//        val taskList = new ArrayList<Callable<Void>>(modifiedCharacters.size());
//        modifiedCharacters.forEach(name -> taskList.add(() -> {
//            val character = new File(LoginManager.PLAYER_SAVE_DIRECTORY + name + LoginManager.EXTENSION);
//            if (!character.exists()) {
//                return null;
//            }
//            Files.copy(character.toPath(), new File(CHARACTER_BACKUPS_SAVE_DIRECTORY + name + LoginManager.EXTENSION).toPath(), StandardCopyOption.COPY_ATTRIBUTES);
//            return null;
//        }));
//        modifiedCharacters.clear();
//        copyOthers(taskList);
//        ForkJoinPool.commonPool().invokeAll(taskList);
//        try {
//            uploadFuture = ForkJoinPool.commonPool().submit(() -> {
//                val backedFiles = zpaq.backup(new File(PENDING_BACKUPS_SAVE_DIRECTORY), new File("data/backups/"));
//                if (UPLOAD) {
//                    aws.upload(backedFiles);
//                }
//                deleteFilesInside(new File(PENDING_BACKUPS_SAVE_DIRECTORY));
//                archivingStatus.setFalse();
//            });
//        } catch (Exception e) {
//            log.error(Strings.EMPTY, e);
//        }
    }

    private void runAwsBackupShellScript() {
        final java.lang.ProcessBuilder builder = new ProcessBuilder("sh", "./scripts/backup.sh");
        try {
            final java.lang.Process process = builder.start();
            try (java.io.BufferedReader is = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                boolean check = true;
                while ((line = is.readLine()) != null) {
                    log.info(line);
                }
            } catch (Exception e) {
                log.error(Strings.EMPTY, e);
            }
            try (java.io.BufferedReader is = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = is.readLine()) != null) {
                    log.info(line);
                }
            } catch (Exception e) {
                log.error(Strings.EMPTY, e);
            }
            final int returnCode = process.waitFor();
            log.info("Backup process has finished execution. Return code: " + returnCode);
        } catch (Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    /**
     * Deletes all the files inside of the requested directory -and subdirectories.
     *
     * @param directory the directory in which the files will be deleted.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteFilesInside(final File directory) {
        final java.io.File[] contents = directory.listFiles();
        if (contents != null) {
            final java.util.ArrayList<java.util.concurrent.Callable<java.lang.Void>> list = new ArrayList<Callable<Void>>();
            for (final java.io.File file : contents) {
                if (file.isDirectory()) {
                    deleteFilesInside(file);
                } else {
                    list.add(() -> {
                        file.delete();
                        return null;
                    });
                }
            }
            ForkJoinPool.commonPool().invokeAll(list);
        }
    }

    /**
     * Appends async tasks to the task list to copy alternative other files besides the character files, to then execute async.
     *
     * @param taskList the list of callable tasks that will be executed async.
     */
    private void copyOthers(final List<Callable<Void>> taskList) {
        //Copy grand exchange offers
        taskList.add(() -> {
            final java.io.File offers = new File(GrandExchangeHandler.OFFERS_FILE_DIRECTORY);
            if (!offers.exists()) {
                return null;
            }
            Files.copy(offers.toPath(), new File(OTHER_BACKUPS_SAVE_DIRECTORY + offers.getName()).toPath());
            return null;
        });
        //Copy grand exchange prices
        taskList.add(() -> {
            final java.io.File prices = new File(GrandExchangeHandler.PRICES_FILE_DIRECTORY);
            if (!prices.exists()) {
                return null;
            }
            Files.copy(prices.toPath(), new File(OTHER_BACKUPS_SAVE_DIRECTORY + prices.getName()).toPath());
            return null;
        });
        //Copy clans
        taskList.add(() -> {
            final java.io.File clans = new File(ClanManager.CLANS_FILE_DIRECTORY);
            if (!clans.exists()) {
                return null;
            }
            Files.copy(clans.toPath(), new File(OTHER_BACKUPS_SAVE_DIRECTORY + clans.getName()).toPath());
            return null;
        });
        //Copy punishments
        taskList.add(() -> {
            final java.io.File punishments = new File(PunishmentManager.PATH);
            if (!punishments.exists()) {
                return null;
            }
            Files.copy(punishments.toPath(), new File(OTHER_BACKUPS_SAVE_DIRECTORY + punishments.getName()).toPath());
            return null;
        });
    }

    /**
     * Extracts all of the files in the backup up until the date specified.
     *
     * @param until the date until the backups will be extracted for.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void extract(final Date until) {
        final java.io.File destination = new File("data/backups/extraction/");
        destination.mkdirs();
        zpaq.extract(new File("data/backups/"), destination, until);
    }

    /**
     * Status of the backups - whether or not a backup is being written at the given time.
     */
    public MutableBoolean getStatus() {
        return this.status;
    }

    /**
     * The thread that executes the loading and saving of the accounts.
     */
    public Thread getThread() {
        return this.thread;
    }
}
