package com.zenyte.database;

import org.apache.logging.log4j.util.Strings;

public class DatabaseThread extends Thread {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseThread.class);
	public static volatile boolean ENABLED = true;

	@Override
	public void run() {
		try {
			try {
				Database.preload();
				Database.pool = new DatabasePool();
			} catch (final Exception e) {
				log.error(Strings.EMPTY, e);
			}
			while (ENABLED) {
				QueryExecutor.process();
				try {
					Thread.sleep(600);
				} catch (final Exception e) {
					log.error(Strings.EMPTY, e);
				}
			}
		} catch (final Exception e) {
			log.error(Strings.EMPTY, e);
		}
	}
}
