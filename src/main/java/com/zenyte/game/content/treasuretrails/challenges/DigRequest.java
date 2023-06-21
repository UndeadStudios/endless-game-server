package com.zenyte.game.content.treasuretrails.challenges;

import com.zenyte.game.content.treasuretrails.TreasureGuardianNPC;
import com.zenyte.game.world.entity.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Kris | 07/04/2019 13:46
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class DigRequest implements ClueChallenge {
    public DigRequest(@NotNull final Location location) {
        this(location, null);
    }

    public DigRequest(@NotNull final Location location, @Nullable final TreasureGuardianNPC guardianNPC) {
        this.location = location;
        this.guardianNPC = guardianNPC;
    }

    @NotNull
    private final Location location;
    @Nullable
    private final TreasureGuardianNPC guardianNPC;

    @NotNull
    public Location getLocation() {
        return this.location;
    }

    @Nullable
    public TreasureGuardianNPC getGuardianNPC() {
        return this.guardianNPC;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof DigRequest)) return false;
        final DigRequest other = (DigRequest) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$location = this.getLocation();
        final Object other$location = other.getLocation();
        if (this$location == null ? other$location != null : !this$location.equals(other$location)) return false;
        final Object this$guardianNPC = this.getGuardianNPC();
        final Object other$guardianNPC = other.getGuardianNPC();
        if (this$guardianNPC == null ? other$guardianNPC != null : !this$guardianNPC.equals(other$guardianNPC)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof DigRequest;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $location = this.getLocation();
        result = result * PRIME + ($location == null ? 43 : $location.hashCode());
        final Object $guardianNPC = this.getGuardianNPC();
        result = result * PRIME + ($guardianNPC == null ? 43 : $guardianNPC.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "DigRequest(location=" + this.getLocation() + ", guardianNPC=" + this.getGuardianNPC() + ")";
    }
}
