package com.zenyte.game.content.treasuretrails.challenges;

/**
 * @author Kris | 04/12/2019
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class SongRequest implements ClueChallenge {
    private final String song;

    public SongRequest(final String song) {
        this.song = song;
    }

    public String getSong() {
        return this.song;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof SongRequest)) return false;
        final SongRequest other = (SongRequest) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$song = this.getSong();
        final Object other$song = other.getSong();
        if (this$song == null ? other$song != null : !this$song.equals(other$song)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof SongRequest;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $song = this.getSong();
        result = result * PRIME + ($song == null ? 43 : $song.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "SongRequest(song=" + this.getSong() + ")";
    }
}
