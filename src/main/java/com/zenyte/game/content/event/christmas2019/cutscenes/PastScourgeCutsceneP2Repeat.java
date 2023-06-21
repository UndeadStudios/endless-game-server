package com.zenyte.game.content.event.christmas2019.cutscenes;

import com.google.common.base.Preconditions;
import com.zenyte.game.content.event.christmas2019.ChristmasConstants;
import com.zenyte.game.content.event.christmas2019.ChristmasUtils;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.masks.Animation;
import com.zenyte.game.world.entity.masks.Graphics;
import com.zenyte.game.world.entity.player.cutscene.Cutscene;
import com.zenyte.game.world.entity.player.cutscene.actions.CameraLookAction;
import com.zenyte.game.world.entity.player.cutscene.actions.CameraPositionAction;
import com.zenyte.game.world.entity.player.cutscene.actions.CameraResetAction;
import mgi.types.config.ObjectDefinitions;

import static com.zenyte.game.content.event.christmas2019.ChristmasConstants.UNFROZEN_GUESTS_HASH_KEY;

/**
 * @author Kris | 21/12/2019
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class PastScourgeCutsceneP2Repeat extends Cutscene {
    @Override
    public void build() {
        addActions(0, () -> player.lock(), () -> player.getVarManager().sendBit(4606, 1), new CameraPositionAction(player, new Location(2079, 5398, 0), 1600, 100, 100), new CameraLookAction(player, new Location(2079, 5401, 0), 200, 100, 100), () -> player.sendMessage("You\'ve broken the curse in the wrong order!"));
        addActions(1, () -> player.sendMessage("The curse re-establishes itself!"), () -> {
            final int hash = player.getNumericAttribute(UNFROZEN_GUESTS_HASH_KEY).intValue();
            final java.lang.String guests = ChristmasUtils.getFrozenGuestOrder(player);
            final char[] chars = guests.toCharArray();
            for (int i = chars.length - 1; i >= 0; i--) {
                final char character = chars[i];
                final com.zenyte.game.content.event.christmas2019.cutscenes.FrozenGuest respectiveGuest = Utils.findMatching(FrozenGuest.getValues(), g -> g.getConstant() == character);
                Preconditions.checkArgument(respectiveGuest != null);
                final boolean isUnfrozen = ((hash >> respectiveGuest.ordinal()) & 1) == 1;
                if (isUnfrozen) {
                    final com.zenyte.game.world.entity.npc.NPC npc = World.findNPC(respectiveGuest.getBaseNPC(), respectiveGuest.getTile(), 1).orElseThrow(RuntimeException::new);
                    npc.setInvalidAnimation(respectiveGuest.getPreFreezeAnimation());
                    player.getPacketDispatcher().sendGraphics(new Graphics(2507), npc.getLocation());
                }
            }
            player.addAttribute(UNFROZEN_GUESTS_HASH_KEY, 0);
        });
        addActions(2, () -> {
            for (final com.zenyte.game.content.event.christmas2019.cutscenes.FrozenGuest guest : FrozenGuest.getValues()) {
                player.getVarManager().sendBit(ObjectDefinitions.getOrThrow(guest.getBaseObject()).getVarbitId(), 1);
            }
        });
        final java.lang.String chars = ChristmasUtils.getFrozenGuestOrder(player);
        final int length = chars.length();
        for (int i = 0; i < length; i++) {
            final char character = chars.charAt(i);
            addActions(3 + (i * 2), () -> {
                final com.zenyte.game.content.event.christmas2019.cutscenes.FrozenGuest guest = Utils.findMatching(FrozenGuest.getValues(), g -> g.getConstant() == character);
                Preconditions.checkArgument(guest != null);
                final com.zenyte.game.world.entity.Location tile = guest.getTile();
                final com.zenyte.game.world.object.WorldObject object = World.getObjectWithId(tile, guest.getBaseObject());
                World.sendObjectAnimation(object, new Animation(15108));
                player.getPacketDispatcher().sendGraphics(new Graphics(1010, 0, 150), tile);
            });
        }
        addActions(15, player::unlock, new CameraResetAction(player), () -> player.getVarManager().sendBit(4606, 0), () -> ChristmasConstants.refreshAllVarbits(player));
    }
}
