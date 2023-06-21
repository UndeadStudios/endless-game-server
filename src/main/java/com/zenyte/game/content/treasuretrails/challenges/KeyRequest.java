package com.zenyte.game.content.treasuretrails.challenges;

/**
 * @author Kris | 07/04/2019 13:50
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public final class KeyRequest implements ClueChallenge {
    private final int keyId;
    private final int[] validNPCs;
    private final GameObject[] validObjects;

    public KeyRequest(final int keyId, final int[] validNPCs, final GameObject[] validObjects) {
        this.keyId = keyId;
        this.validNPCs = validNPCs;
        this.validObjects = validObjects;
    }

    public int getKeyId() {
        return this.keyId;
    }

    public int[] getValidNPCs() {
        return this.validNPCs;
    }

    public GameObject[] getValidObjects() {
        return this.validObjects;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof KeyRequest)) return false;
        final KeyRequest other = (KeyRequest) o;
        if (this.getKeyId() != other.getKeyId()) return false;
        if (!java.util.Arrays.equals(this.getValidNPCs(), other.getValidNPCs())) return false;
        if (!java.util.Arrays.deepEquals(this.getValidObjects(), other.getValidObjects())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getKeyId();
        result = result * PRIME + java.util.Arrays.hashCode(this.getValidNPCs());
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getValidObjects());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "KeyRequest(keyId=" + this.getKeyId() + ", validNPCs=" + java.util.Arrays.toString(this.getValidNPCs()) + ", validObjects=" + java.util.Arrays.deepToString(this.getValidObjects()) + ")";
    }
}
