package com.zenyte.game.content.treasuretrails.challenges;

import com.zenyte.game.content.treasuretrails.clues.CharlieTask;

/**
 * @author Kris | 04/01/2020
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class CharlieRequest implements ClueChallenge {
    private final CharlieTask task;

    public CharlieRequest(final CharlieTask task) {
        this.task = task;
    }

    public CharlieTask getTask() {
        return this.task;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof CharlieRequest)) return false;
        final CharlieRequest other = (CharlieRequest) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$task = this.getTask();
        final Object other$task = other.getTask();
        if (this$task == null ? other$task != null : !this$task.equals(other$task)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof CharlieRequest;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $task = this.getTask();
        result = result * PRIME + ($task == null ? 43 : $task.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "CharlieRequest(task=" + this.getTask() + ")";
    }
}
