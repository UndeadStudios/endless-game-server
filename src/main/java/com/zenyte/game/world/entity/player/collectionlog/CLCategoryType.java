package com.zenyte.game.world.entity.player.collectionlog;

import com.zenyte.game.util.Utils;
import com.zenyte.utils.Ordinal;

/**
 * @author Kris | 13/03/2019 14:22
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
@Ordinal
enum CLCategoryType {
    BOSS(471), RAIDS(472), CLUES(473), MINIGAMES(474), OTHER(475);
    private final int struct;
    private final String toString = Utils.formatString(name());
    private final String category = toString() + " category";

    private CLCategoryType(final int struct) {
        this.struct = struct;
    }

    public int struct() {
        return this.struct;
    }

    public String toString() {
        return this.toString;
    }

    public String category() {
        return this.category;
    }
}
