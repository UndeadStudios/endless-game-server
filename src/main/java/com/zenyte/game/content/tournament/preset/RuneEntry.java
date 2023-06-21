package com.zenyte.game.content.tournament.preset;

import com.zenyte.game.content.skills.magic.Rune;

/**
 * @author Tommeh | 22/07/2019 | 22:11
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
 */
public class RuneEntry {
    private final Rune rune;
    private final int amount;

    public static RuneEntry of(final Rune rune, final int amount) {
        return new RuneEntry(rune, amount);
    }

    public RuneEntry(final Rune rune, final int amount) {
        this.rune = rune;
        this.amount = amount;
    }

    public Rune getRune() {
        return this.rune;
    }

    public int getAmount() {
        return this.amount;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof RuneEntry)) return false;
        final RuneEntry other = (RuneEntry) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getAmount() != other.getAmount()) return false;
        final Object this$rune = this.getRune();
        final Object other$rune = other.getRune();
        if (this$rune == null ? other$rune != null : !this$rune.equals(other$rune)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof RuneEntry;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getAmount();
        final Object $rune = this.getRune();
        result = result * PRIME + ($rune == null ? 43 : $rune.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "RuneEntry(rune=" + this.getRune() + ", amount=" + this.getAmount() + ")";
    }
}
