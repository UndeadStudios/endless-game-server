package com.zenyte.plugins.events;

import com.zenyte.game.content.multicannon.Multicannon;
import com.zenyte.plugins.Event;

/**
 * @author Kris | 09/10/2019
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class CannonRemoveEvent implements Event {
    private final Multicannon cannon;

    public CannonRemoveEvent(final Multicannon cannon) {
        this.cannon = cannon;
    }

    public Multicannon getCannon() {
        return this.cannon;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof CannonRemoveEvent)) return false;
        final CannonRemoveEvent other = (CannonRemoveEvent) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$cannon = this.getCannon();
        final Object other$cannon = other.getCannon();
        if (this$cannon == null ? other$cannon != null : !this$cannon.equals(other$cannon)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof CannonRemoveEvent;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $cannon = this.getCannon();
        result = result * PRIME + ($cannon == null ? 43 : $cannon.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "CannonRemoveEvent(cannon=" + this.getCannon() + ")";
    }
}
