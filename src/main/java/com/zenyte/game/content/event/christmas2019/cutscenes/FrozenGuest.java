package com.zenyte.game.content.event.christmas2019.cutscenes;

import com.zenyte.game.util.Direction;
import com.zenyte.game.world.entity.ImmutableLocation;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.masks.Animation;
import com.zenyte.utils.Ordinal;

/**
 * @author Kris | 20/12/2019
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
@Ordinal
public enum FrozenGuest {
    GENERAL_WARTFACE('w', new ImmutableLocation(2078, 5401, 0), Direction.EAST, 15044, 15050, 46114, new Animation(15109), new Animation(15110)), GENERAL_BENTNOZE('b', new ImmutableLocation(2080, 5401, 0), Direction.WEST, 15046, 15048, 46117, new Animation(15111), new Animation(15112)), SIR_AMIK_VARZE('a', new ImmutableLocation(2074, 5402, 0), Direction.WEST, 15033, 15052, 46120, new Animation(15113), new Animation(15114)), COOK('c', new ImmutableLocation(2074, 5405, 0), Direction.WEST, 15041, 15054, 46123, new Animation(15115), new Animation(15116)), PARTYGOER('p', new ImmutableLocation(2084, 5402, 0), Direction.EAST, 15020, 15056, 46126, new Animation(15117), new Animation(15118)), TINY_THOM('t', new ImmutableLocation(2080, 5405, 0), Direction.WEST, 15038, 15058, 46129, new Animation(15054), new Animation(15053));
    private static final FrozenGuest[] values = values();
    private final char constant;
    private final Location tile;
    private final Direction direction;
    private final int cutsceneBaseNPC;
    private final int baseNPC;
    private final int baseObject;
    private final Animation preFreezeAnimation;
    private final Animation preDefreezeAnimation;

    private FrozenGuest(final char constant, final Location tile, final Direction direction, final int cutsceneBaseNPC, final int baseNPC, final int baseObject, final Animation preFreezeAnimation, final Animation preDefreezeAnimation) {
        this.constant = constant;
        this.tile = tile;
        this.direction = direction;
        this.cutsceneBaseNPC = cutsceneBaseNPC;
        this.baseNPC = baseNPC;
        this.baseObject = baseObject;
        this.preFreezeAnimation = preFreezeAnimation;
        this.preDefreezeAnimation = preDefreezeAnimation;
    }

    public char getConstant() {
        return this.constant;
    }

    public Location getTile() {
        return this.tile;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public int getCutsceneBaseNPC() {
        return this.cutsceneBaseNPC;
    }

    public int getBaseNPC() {
        return this.baseNPC;
    }

    public int getBaseObject() {
        return this.baseObject;
    }

    public Animation getPreFreezeAnimation() {
        return this.preFreezeAnimation;
    }

    public Animation getPreDefreezeAnimation() {
        return this.preDefreezeAnimation;
    }

    public static FrozenGuest[] getValues() {
        return FrozenGuest.values;
    }
}
