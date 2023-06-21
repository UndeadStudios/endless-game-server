package com.zenyte.plugins.events;

import com.zenyte.game.world.entity.player.Player;
import com.zenyte.plugins.Event;

/**
 * @author Kris | 23/03/2019 23:15
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class LogoutEvent implements Event {
    private final Player player;

    public LogoutEvent(final Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof LogoutEvent)) return false;
        final LogoutEvent other = (LogoutEvent) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$player = this.getPlayer();
        final Object other$player = other.getPlayer();
        if (this$player == null ? other$player != null : !this$player.equals(other$player)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof LogoutEvent;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $player = this.getPlayer();
        result = result * PRIME + ($player == null ? 43 : $player.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "LogoutEvent(player=" + this.getPlayer() + ")";
    }
}
