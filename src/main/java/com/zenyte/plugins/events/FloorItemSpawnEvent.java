package com.zenyte.plugins.events;

import com.zenyte.game.world.flooritem.FloorItem;
import com.zenyte.plugins.Event;

/**
 * @author Kris | 21/03/2019 16:19
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class FloorItemSpawnEvent implements Event {
    private final FloorItem item;

    public FloorItemSpawnEvent(final FloorItem item) {
        this.item = item;
    }

    public FloorItem getItem() {
        return this.item;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof FloorItemSpawnEvent)) return false;
        final FloorItemSpawnEvent other = (FloorItemSpawnEvent) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$item = this.getItem();
        final Object other$item = other.getItem();
        if (this$item == null ? other$item != null : !this$item.equals(other$item)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof FloorItemSpawnEvent;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $item = this.getItem();
        result = result * PRIME + ($item == null ? 43 : $item.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "FloorItemSpawnEvent(item=" + this.getItem() + ")";
    }
}
