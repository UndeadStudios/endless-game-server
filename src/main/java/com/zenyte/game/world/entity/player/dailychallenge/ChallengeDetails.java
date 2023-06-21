package com.zenyte.game.world.entity.player.dailychallenge;

/**
 * @author Tommeh | 03/05/2019 | 22:16
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
 */
public class ChallengeDetails {
    private final ChallengeDifficulty difficulty;
    private final Object[] additionalInformation;

    public ChallengeDetails(final ChallengeDifficulty difficulty, final Object... additionalInformation) {
        this.difficulty = difficulty;
        this.additionalInformation = additionalInformation;
    }

    public ChallengeDifficulty getDifficulty() {
        return this.difficulty;
    }

    public Object[] getAdditionalInformation() {
        return this.additionalInformation;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof ChallengeDetails)) return false;
        final ChallengeDetails other = (ChallengeDetails) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$difficulty = this.getDifficulty();
        final Object other$difficulty = other.getDifficulty();
        if (this$difficulty == null ? other$difficulty != null : !this$difficulty.equals(other$difficulty)) return false;
        if (!java.util.Arrays.deepEquals(this.getAdditionalInformation(), other.getAdditionalInformation())) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof ChallengeDetails;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $difficulty = this.getDifficulty();
        result = result * PRIME + ($difficulty == null ? 43 : $difficulty.hashCode());
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getAdditionalInformation());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "ChallengeDetails(difficulty=" + this.getDifficulty() + ", additionalInformation=" + java.util.Arrays.deepToString(this.getAdditionalInformation()) + ")";
    }
}
