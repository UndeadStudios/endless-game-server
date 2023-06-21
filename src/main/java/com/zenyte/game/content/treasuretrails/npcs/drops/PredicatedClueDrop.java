package com.zenyte.game.content.treasuretrails.npcs.drops;

import mgi.types.config.npcs.NPCDefinitions;

import java.util.function.Predicate;

/**
 * @author Kris | 22/11/2019
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class PredicatedClueDrop {
    private final double rate;
    private final Predicate<NPCDefinitions> predicate;

    public PredicatedClueDrop(final double rate, final Predicate<NPCDefinitions> predicate) {
        this.rate = rate;
        this.predicate = predicate;
    }

    public double getRate() {
        return this.rate;
    }

    public Predicate<NPCDefinitions> getPredicate() {
        return this.predicate;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof PredicatedClueDrop)) return false;
        final PredicatedClueDrop other = (PredicatedClueDrop) o;
        if (!other.canEqual((Object) this)) return false;
        if (Double.compare(this.getRate(), other.getRate()) != 0) return false;
        final Object this$predicate = this.getPredicate();
        final Object other$predicate = other.getPredicate();
        if (this$predicate == null ? other$predicate != null : !this$predicate.equals(other$predicate)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof PredicatedClueDrop;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $rate = Double.doubleToLongBits(this.getRate());
        result = result * PRIME + (int) ($rate >>> 32 ^ $rate);
        final Object $predicate = this.getPredicate();
        result = result * PRIME + ($predicate == null ? 43 : $predicate.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "PredicatedClueDrop(rate=" + this.getRate() + ", predicate=" + this.getPredicate() + ")";
    }
}
