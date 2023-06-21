package com.zenyte.game.content.treasuretrails.challenges;

/**
 * @author Kris | 07/04/2019 13:50
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public final class PuzzleRequest implements ClueChallenge {
    private final int[] validNPCs;

    public PuzzleRequest(final int[] validNPCs) {
        this.validNPCs = validNPCs;
    }

    public int[] getValidNPCs() {
        return this.validNPCs;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof PuzzleRequest)) return false;
        final PuzzleRequest other = (PuzzleRequest) o;
        if (!java.util.Arrays.equals(this.getValidNPCs(), other.getValidNPCs())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + java.util.Arrays.hashCode(this.getValidNPCs());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "PuzzleRequest(validNPCs=" + java.util.Arrays.toString(this.getValidNPCs()) + ")";
    }
}
