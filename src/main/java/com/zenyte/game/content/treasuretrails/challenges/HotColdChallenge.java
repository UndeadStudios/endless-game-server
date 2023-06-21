package com.zenyte.game.content.treasuretrails.challenges;

import com.zenyte.game.world.entity.Location;

/**
 * @author Kris | 10/04/2019 16:53
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class HotColdChallenge implements ClueChallenge {
    private final Location center;

    public HotColdChallenge(final Location center) {
        this.center = center;
    }

    public Location getCenter() {
        return this.center;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof HotColdChallenge)) return false;
        final HotColdChallenge other = (HotColdChallenge) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$center = this.getCenter();
        final Object other$center = other.getCenter();
        if (this$center == null ? other$center != null : !this$center.equals(other$center)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof HotColdChallenge;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $center = this.getCenter();
        result = result * PRIME + ($center == null ? 43 : $center.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "HotColdChallenge(center=" + this.getCenter() + ")";
    }
}
