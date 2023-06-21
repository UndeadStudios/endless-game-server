package com.zenyte.game.content.treasuretrails.challenges;

import com.zenyte.game.content.treasuretrails.clues.ChallengeScroll;

/**
 * @author Kris | 07/04/2019 13:50
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public final class TalkChallengeRequest implements ClueChallenge {
    private final ChallengeScroll challengeScroll;
    private final int[] validNPCs;

    public TalkChallengeRequest(final ChallengeScroll challengeScroll, final int[] validNPCs) {
        this.challengeScroll = challengeScroll;
        this.validNPCs = validNPCs;
    }

    public ChallengeScroll getChallengeScroll() {
        return this.challengeScroll;
    }

    public int[] getValidNPCs() {
        return this.validNPCs;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof TalkChallengeRequest)) return false;
        final TalkChallengeRequest other = (TalkChallengeRequest) o;
        final Object this$challengeScroll = this.getChallengeScroll();
        final Object other$challengeScroll = other.getChallengeScroll();
        if (this$challengeScroll == null ? other$challengeScroll != null : !this$challengeScroll.equals(other$challengeScroll)) return false;
        if (!java.util.Arrays.equals(this.getValidNPCs(), other.getValidNPCs())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $challengeScroll = this.getChallengeScroll();
        result = result * PRIME + ($challengeScroll == null ? 43 : $challengeScroll.hashCode());
        result = result * PRIME + java.util.Arrays.hashCode(this.getValidNPCs());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "TalkChallengeRequest(challengeScroll=" + this.getChallengeScroll() + ", validNPCs=" + java.util.Arrays.toString(this.getValidNPCs()) + ")";
    }
}
