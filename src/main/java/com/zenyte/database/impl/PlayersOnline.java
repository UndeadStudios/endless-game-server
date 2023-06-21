package com.zenyte.database.impl;

import com.zenyte.Constants;
import com.zenyte.database.DatabaseCredential;
import com.zenyte.database.DatabasePool;
import com.zenyte.database.SQLRunnable;
import org.apache.logging.log4j.util.Strings;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PlayersOnline extends SQLRunnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlayersOnline.class);
    private int amount;

    public PlayersOnline(final int amount) {
        this.amount = amount;
    }

    @Override
    public void execute(final DatabaseCredential auth) {
        try (
            Connection con = DatabasePool.getConnection(auth, "zenyte_main");
            PreparedStatement stmt = con.prepareStatement("UPDATE players_online SET online = ? WHERE world = ?")) {
            stmt.setInt(1, amount);
            stmt.setString(2, Constants.WORLD_PROFILE.getKey());
            stmt.execute();
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }
}
