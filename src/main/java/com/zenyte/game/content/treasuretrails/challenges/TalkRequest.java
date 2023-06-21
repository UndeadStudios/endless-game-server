package com.zenyte.game.content.treasuretrails.challenges;

import com.zenyte.game.world.entity.player.Player;

import java.util.function.Predicate;

/**
 * @author Kris | 07/04/2019 13:46
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public final class TalkRequest implements ClueChallenge {
    private final int[] validNPCs;
    private Predicate<Player> predicate;

    public int[] getValidNPCs() {
        return this.validNPCs;
    }

    public Predicate<Player> getPredicate() {
        return this.predicate;
    }

    public void setPredicate(final Predicate<Player> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof TalkRequest)) return false;
        final TalkRequest other = (TalkRequest) o;
        if (!java.util.Arrays.equals(this.getValidNPCs(), other.getValidNPCs())) return false;
        final Object this$predicate = this.getPredicate();
        final Object other$predicate = other.getPredicate();
        if (this$predicate == null ? other$predicate != null : !this$predicate.equals(other$predicate)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + java.util.Arrays.hashCode(this.getValidNPCs());
        final Object $predicate = this.getPredicate();
        result = result * PRIME + ($predicate == null ? 43 : $predicate.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "TalkRequest(validNPCs=" + java.util.Arrays.toString(this.getValidNPCs()) + ", predicate=" + this.getPredicate() + ")";
    }

    public TalkRequest(final int[] validNPCs) {
        this.validNPCs = validNPCs;
    }

    public TalkRequest(final int[] validNPCs, final Predicate<Player> predicate) {
        this.validNPCs = validNPCs;
        this.predicate = predicate;
    }
}
