package com.zenyte.game.content.chambersofxeric.greatolm.scripts;

import com.zenyte.game.content.chambersofxeric.greatolm.GreatOlm;
import com.zenyte.game.content.chambersofxeric.greatolm.OlmCombatScript;

/**
 * @author Kris | 30/07/2019 15:19
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class LeftClawProtection implements OlmCombatScript {
    @Override
    public void handle(final GreatOlm olm) {
        final com.zenyte.game.content.chambersofxeric.greatolm.LeftClaw leftClaw = olm.getRoom().getLeftClaw();
        if (leftClaw == null) {
            return;
        }
        final com.zenyte.game.content.chambersofxeric.greatolm.RightClaw rightClaw = olm.getRoom().getRightClaw();
        if (leftClaw.getClenchTicks() > 0 || rightClaw == null || rightClaw.isFinished()) {
            return;
        }
        leftClaw.setProtected();
    }
}
