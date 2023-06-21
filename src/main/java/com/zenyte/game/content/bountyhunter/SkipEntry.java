package com.zenyte.game.content.bountyhunter;

/**
 * @author Kris | 26/03/2019 21:27
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class SkipEntry {
    private final String username;
    private final long time;

    public SkipEntry(final String username, final long time) {
        this.username = username;
        this.time = time;
    }

    public String getUsername() {
        return this.username;
    }

    public long getTime() {
        return this.time;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof SkipEntry)) return false;
        final SkipEntry other = (SkipEntry) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getTime() != other.getTime()) return false;
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (this$username == null ? other$username != null : !this$username.equals(other$username)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof SkipEntry;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $time = this.getTime();
        result = result * PRIME + (int) ($time >>> 32 ^ $time);
        final Object $username = this.getUsername();
        result = result * PRIME + ($username == null ? 43 : $username.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "SkipEntry(username=" + this.getUsername() + ", time=" + this.getTime() + ")";
    }
}
