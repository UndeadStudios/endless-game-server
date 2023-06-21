package com.zenyte.game.content.chambersofxeric.greatolm.scripts;

import com.zenyte.game.content.chambersofxeric.greatolm.CrystalCluster;
import com.zenyte.game.content.chambersofxeric.greatolm.GreatOlm;
import com.zenyte.game.content.chambersofxeric.greatolm.LeftClaw;
import com.zenyte.game.content.chambersofxeric.greatolm.OlmCombatScript;
import com.zenyte.game.world.entity.Location;

/**
 * @author Kris | 16. jaan 2018 : 1:10.37
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public final class CrystalBurst implements OlmCombatScript {
    @Override
    public void handle(final GreatOlm olm) {
        final com.zenyte.game.content.chambersofxeric.greatolm.OlmRoom room = olm.getRoom();
        final com.zenyte.game.content.chambersofxeric.greatolm.LeftClaw leftClaw = room.getLeftClaw();
        if (leftClaw != null) {
            leftClaw.displaySign(LeftClaw.CRYSTAL_SIGN);
        }
        if (room.getRaid().isDestroyed()) {
            return;
        }
        for (final com.zenyte.game.world.entity.player.Player player : olm.everyone(GreatOlm.ENTIRE_CHAMBER)) {
            final com.zenyte.game.world.entity.Location tile = new Location(player.getLocation());
            if (room.containsCrystalCluster(tile)) {
                continue;
            }
            final com.zenyte.game.content.chambersofxeric.greatolm.CrystalCluster cluster = new CrystalCluster(room, tile);
            cluster.process();
        }
    }
}
