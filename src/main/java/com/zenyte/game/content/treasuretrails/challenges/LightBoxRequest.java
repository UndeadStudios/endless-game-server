package com.zenyte.game.content.treasuretrails.challenges;

/**
 * @author Kris | 08/04/2019 20:53
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public final class LightBoxRequest implements ClueChallenge {
    private final int[] validNPCs;

    public LightBoxRequest(final int[] validNPCs) {
        this.validNPCs = validNPCs;
    }

    public int[] getValidNPCs() {
        return this.validNPCs;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof LightBoxRequest)) return false;
        final LightBoxRequest other = (LightBoxRequest) o;
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
        return "LightBoxRequest(validNPCs=" + java.util.Arrays.toString(this.getValidNPCs()) + ")";
    }
}
