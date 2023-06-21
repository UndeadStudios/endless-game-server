package com.zenyte.plugins.events;

import com.zenyte.game.content.skills.magic.Spellbook;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.plugins.Event;

/**
 * @author Kris | 26/04/2019 19:46
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class SpellbookChangeEvent implements Event {
    private final Player player;
    private final Spellbook oldSpellbook;

    public SpellbookChangeEvent(final Player player, final Spellbook oldSpellbook) {
        this.player = player;
        this.oldSpellbook = oldSpellbook;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Spellbook getOldSpellbook() {
        return this.oldSpellbook;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof SpellbookChangeEvent)) return false;
        final SpellbookChangeEvent other = (SpellbookChangeEvent) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$player = this.getPlayer();
        final Object other$player = other.getPlayer();
        if (this$player == null ? other$player != null : !this$player.equals(other$player)) return false;
        final Object this$oldSpellbook = this.getOldSpellbook();
        final Object other$oldSpellbook = other.getOldSpellbook();
        if (this$oldSpellbook == null ? other$oldSpellbook != null : !this$oldSpellbook.equals(other$oldSpellbook)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof SpellbookChangeEvent;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $player = this.getPlayer();
        result = result * PRIME + ($player == null ? 43 : $player.hashCode());
        final Object $oldSpellbook = this.getOldSpellbook();
        result = result * PRIME + ($oldSpellbook == null ? 43 : $oldSpellbook.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "SpellbookChangeEvent(player=" + this.getPlayer() + ", oldSpellbook=" + this.getOldSpellbook() + ")";
    }
}
