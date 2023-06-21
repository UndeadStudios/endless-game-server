package com.zenyte.game.content.treasuretrails.challenges;

/**
 * @author Kris | 07/04/2019 13:46
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public final class SearchRequest implements ClueChallenge {
    private final GameObject[] validObjects;

    public SearchRequest(final GameObject[] validObjects) {
        this.validObjects = validObjects;
    }

    public GameObject[] getValidObjects() {
        return this.validObjects;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof SearchRequest)) return false;
        final SearchRequest other = (SearchRequest) o;
        if (!java.util.Arrays.deepEquals(this.getValidObjects(), other.getValidObjects())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getValidObjects());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "SearchRequest(validObjects=" + java.util.Arrays.deepToString(this.getValidObjects()) + ")";
    }
}
