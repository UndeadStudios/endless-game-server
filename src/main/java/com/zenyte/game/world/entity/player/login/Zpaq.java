package com.zenyte.game.world.entity.player.login;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Kris | 17/05/2019 14:22
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
final class Zpaq {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Zpaq.class);
    /**
     * The string path of the index file of the backups process where all the pointer-information is stored.
     */
    static final String INDEX_FILE = BackupManager.BACKUPS_DIR + "backup000000.zpaq";
    /**
     * The command prompt header for OS-dependent execution - goes between windows and linux OS.
     */
    private static final String HEADER = SystemUtils.IS_OS_WINDOWS ? "cmd.exe" : "bash";
    /**
     * The executor line for command prompt, OS-dependent.
     */
    private static final String COMMAND_EXECUTOR = SystemUtils.IS_OS_WINDOWS ? "/c" : "-c";
    /**
     * The prefix used before calling the program, differs per OS.
     */
    private static final String PREFIX = SystemUtils.IS_OS_WINDOWS ? Strings.EMPTY : "./";
    /**
     * The 32-byte key used in the SHA-256 encryption process.
     */
    private static final String ENCRYPTION_KEY = "Q3cmzPa9rNK5FuD8ZcR3vN9C";

    static {
        //Invalidate that both versions of the backup tool exist.
        if (SystemUtils.IS_OS_WINDOWS) {
            Preconditions.checkArgument(new File("zpaq.exe").exists());
        } else if (SystemUtils.IS_OS_LINUX) {
            Preconditions.checkArgument(new File("zpaq").exists());
        } else {
            throw new IllegalStateException("Unhandled operating system detected.");
        }
    }

    //Package-constructor.
    Zpaq() {
    }

    /**
     * Backs up all the files inside the specified directory, and saves the backed up sectors to the destination directory.
     *
     * @param directory            the directory whose contents to back up.
     * @param destinationDirectory the destination directory to where the backed up sectors will be moved to.
     * @return a list of the index file and the new sector file.
     */
    @NotNull
    List<File> backup(final File directory, final File destinationDirectory) {
        Preconditions.checkArgument(directory.isDirectory());
        Preconditions.checkArgument(destinationDirectory.isDirectory());
        final java.util.ArrayList<java.io.File> files = new ArrayList<File>(2);
        final java.lang.ProcessBuilder builder = new ProcessBuilder(HEADER, COMMAND_EXECUTOR, PREFIX + "zpaq add " + destinationDirectory.getPath() + "/backup??????.zpaq " + directory.getPath() + "/* -not *.exe *.zpaq files -index " + destinationDirectory.getPath() + "/backup000000.zpaq -key " + ENCRYPTION_KEY);
        try {
            final java.lang.Process process = builder.start();
            files.add(new File(destinationDirectory.getPath() + "/backup000000.zpaq"));
            try (java.io.BufferedReader is = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                boolean check = true;
                while ((line = is.readLine()) != null) {
                    if (check && line.startsWith("Creating")) {
                        final java.lang.String fileName = line.substring(line.indexOf("Creating "), line.indexOf(".zpaq ")).substring("Creating ".length()) + ".zpaq";
                        final java.io.File file = new File(fileName);
                        files.add(file);
                        check = false;
                    }
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
            assert files.size() == 2;
            assert filesExist(files);
        } catch (Exception e) {
            log.error(Strings.EMPTY, e);
        }
        return files;
    }

    /**
     * Extracts the files from the backups in the specified directory to the destination directory, up until the specified date.
     *
     * @param directory            the directory in which the backup files are.
     * @param destinationDirectory the directory to which the destination files will be extracted to.
     * @param until                the date until when to extract the backups - GMT/UTC 0.
     */
    void extract(final File directory, final File destinationDirectory, final Date until) {
        Preconditions.checkArgument(directory.isDirectory());
        Preconditions.checkArgument(destinationDirectory.isDirectory());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm aa");
        final java.lang.ProcessBuilder builder = new ProcessBuilder(HEADER, COMMAND_EXECUTOR, PREFIX + "zpaq extract \"" + directory.getPath() + "\\backup??????\" -to " + destinationDirectory.getPath() + " -until " + format.format(until) + " UTC -key " + ENCRYPTION_KEY);
        try {
            final java.lang.Process process = builder.start();
            final int returnCode = process.waitFor();
            log.info("Extraction process has finished execution. Return code: " + returnCode);
        } catch (IOException | InterruptedException e) {
            log.error(Strings.EMPTY, e);
        }
    }

    /**
     * Checks whether or not the files listed exist in their specified directories.
     *
     * @param files the list of files to check.
     * @return whether or not all the files exist.
     */
    private boolean filesExist(final List<File> files) {
        for (final java.io.File file : files) {
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }
}
