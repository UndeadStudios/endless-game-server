package com.zenyte.game.content.kebos.alchemicalhydra.model;

import com.zenyte.game.util.Direction;
import com.zenyte.game.world.entity.Location;

import java.util.Set;

/**
 * @author Kris | 10/11/2019
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class FireWallBlock {
    private final Set<Location> tiles;
    private final Direction direction;
    private final Location movingFireLocation;

    public FireWallBlock(final Set<Location> tiles, final Direction direction, final Location movingFireLocation) {
        this.tiles = tiles;
        this.direction = direction;
        this.movingFireLocation = movingFireLocation;
    }

    public Set<Location> getTiles() {
        return this.tiles;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public Location getMovingFireLocation() {
        return this.movingFireLocation;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof FireWallBlock)) return false;
        final FireWallBlock other = (FireWallBlock) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$tiles = this.getTiles();
        final Object other$tiles = other.getTiles();
        if (this$tiles == null ? other$tiles != null : !this$tiles.equals(other$tiles)) return false;
        final Object this$direction = this.getDirection();
        final Object other$direction = other.getDirection();
        if (this$direction == null ? other$direction != null : !this$direction.equals(other$direction)) return false;
        final Object this$movingFireLocation = this.getMovingFireLocation();
        final Object other$movingFireLocation = other.getMovingFireLocation();
        if (this$movingFireLocation == null ? other$movingFireLocation != null : !this$movingFireLocation.equals(other$movingFireLocation)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof FireWallBlock;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $tiles = this.getTiles();
        result = result * PRIME + ($tiles == null ? 43 : $tiles.hashCode());
        final Object $direction = this.getDirection();
        result = result * PRIME + ($direction == null ? 43 : $direction.hashCode());
        final Object $movingFireLocation = this.getMovingFireLocation();
        result = result * PRIME + ($movingFireLocation == null ? 43 : $movingFireLocation.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "FireWallBlock(tiles=" + this.getTiles() + ", direction=" + this.getDirection() + ", movingFireLocation=" + this.getMovingFireLocation() + ")";
    }
}
