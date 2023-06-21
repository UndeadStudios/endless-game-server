package com.zenyte.game.parser.scheduled;

import com.google.gson.Gson;
import com.zenyte.GameEngine;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Kris | 16. juuni 2018 : 16:18:21
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public interface ScheduledExternalizable {
	/**
	 * The formatted gson we use to read and write with.
	 */
	Gson gson = World.getGson();
	/**
	 * The log object, used to output information.
	 */
	Logger log = LogManager.getLogger("Externalizable");

	/**
	 * The save frequency in minutes. If negative or zero, {@link #write()} method will never be called.
	 */
	int writeInterval();

	/**
	 * The abstract read method.
	 */
	void read(final BufferedReader reader);

	/**
	 * The abstract write method.
	 */
	void write();

	/**
	 * The path to the file from which it is read, and to which it is saved.
	 */
	String path();

	/**
	 * Reads the file from the defined {@link #path()}
	 */
	default void read() {
		final String pathString = path();
		final Path path = Path.of(pathString);
		if (Files.notExists(path)) {
			try {
				Files.createFile(path);
			} catch (final IOException e) {
				log.error("Failed to create file at path \"" + pathString + "\"", e);
			}
		}

		final File file = path.toFile();
		try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
			read(reader);
		} catch (final IOException e) {
			ifFileNotFoundOnRead();
		}
	}

	/**
	 * Writes the file and outputs the duration of the writing process.
	 */
	default void writeAndOutput() {
		final long time = System.nanoTime();
		write();
		if (output()) {
			log.info("Writing " + getClass().getSimpleName() + " took " + Utils.nanoToMilli(System.nanoTime() - time) + " milliseconds.");
		}
	}

	/**
	 * Whether to output the duration of the saving process.
	 */
	default boolean output() {
		return false;
	}

	/**
	 * If the file can't be found when trying to read it, this method is executed.
	 */
	default void ifFileNotFoundOnRead() {
		log.error("File not found: " + getClass().getName() + ": " + path());
	}

	/**
	 * Outputs the requested json string to the requested path.
	 */
	default void out(final String json) {
		try (java.io.PrintWriter pw = new PrintWriter(path(), "UTF-8")) {
			pw.println(json);
		} catch (final Exception e) {
			GameEngine.logger.error(Strings.EMPTY, e);
		}
	}
}
