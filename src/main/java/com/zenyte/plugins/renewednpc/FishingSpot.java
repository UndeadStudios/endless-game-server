package com.zenyte.plugins.renewednpc;

import com.zenyte.game.content.skills.fishing.Fishing;
import com.zenyte.game.content.skills.fishing.SpotDefinitions;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;

import java.util.ArrayList;

/**
 * @author Kris | 26/11/2018 18:30
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class FishingSpot extends NPCPlugin {
    @Override
    public void handle() {
        final java.util.ArrayList<java.lang.String> list = new ArrayList<String>();
        for (final com.zenyte.game.content.skills.fishing.SpotDefinitions def : SpotDefinitions.values) {
            for (final java.lang.String action : def.getActions()) {
                if (!list.contains(action)) list.add(action);
            }
        }
        for (final java.lang.String op : list) {
            bind(op, (player, npc) -> Fishing.init(player, npc, op));
        }
    }

    @Override
    public int[] getNPCs() {
        return SpotDefinitions.getNpcs().toIntArray();
    }
}
