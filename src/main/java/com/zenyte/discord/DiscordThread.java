package com.zenyte.discord;

import com.zenyte.game.util.Utils;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;
import org.apache.logging.log4j.util.Strings;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class DiscordThread extends Thread {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DiscordThread.class);
	public static volatile boolean ENABLED = true;
	private static String auth = "OTA5NjE3MjgzMDcxMTA3MDcy.GUyIFi.GgNvnfGN0UY_aR7YVIE_vpGwwl2_wRUC3mJe_w";
	public static DiscordApi api;
	public static CommandHandler handler;

	@Override
	public void run() {
		try {
			this.init();
		} catch (final Exception e) {
			log.error(Strings.EMPTY, e);
		}
	}

	public void init() {
		final DiscordApi api = new DiscordApiBuilder().setToken(auth).login().join();

        handler = new JavacordHandler(api);
		this.commands();
	}

	public void commands() {
		try {
			final Class<?>[] classes = Utils.getClasses("com.zenyte.discord.commands");
			for (final Class<?> c : classes) {
				if (c.isAnonymousClass() || c.isMemberClass()) continue;
				final Object o = c.newInstance();
				if (!(o instanceof CommandExecutor)) continue;
				final CommandExecutor command = (CommandExecutor) o;
				handler.registerCommand(command);
			}
		} catch (final Exception e) {
			log.error(Strings.EMPTY, e);
		}
	}
}
