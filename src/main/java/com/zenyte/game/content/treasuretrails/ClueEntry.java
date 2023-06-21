package com.zenyte.game.content.treasuretrails;

import com.zenyte.game.content.treasuretrails.challenges.ClueChallenge;
import com.zenyte.game.content.treasuretrails.clues.Clue;
import com.zenyte.game.item.Item;

/**
 * @author Kris | 07/12/2019
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class ClueEntry {
    private final int slot;
    private final Item item;
    private final Clue clueScroll;
    private final ClueChallenge challenge;

    public ClueEntry(final int slot, final Item item, final Clue clueScroll, final ClueChallenge challenge) {
        this.slot = slot;
        this.item = item;
        this.clueScroll = clueScroll;
        this.challenge = challenge;
    }

    public int getSlot() {
        return this.slot;
    }

    public Item getItem() {
        return this.item;
    }

    public Clue getClueScroll() {
        return this.clueScroll;
    }

    public ClueChallenge getChallenge() {
        return this.challenge;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof ClueEntry)) return false;
        final ClueEntry other = (ClueEntry) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getSlot() != other.getSlot()) return false;
        final Object this$item = this.getItem();
        final Object other$item = other.getItem();
        if (this$item == null ? other$item != null : !this$item.equals(other$item)) return false;
        final Object this$clueScroll = this.getClueScroll();
        final Object other$clueScroll = other.getClueScroll();
        if (this$clueScroll == null ? other$clueScroll != null : !this$clueScroll.equals(other$clueScroll)) return false;
        final Object this$challenge = this.getChallenge();
        final Object other$challenge = other.getChallenge();
        if (this$challenge == null ? other$challenge != null : !this$challenge.equals(other$challenge)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof ClueEntry;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getSlot();
        final Object $item = this.getItem();
        result = result * PRIME + ($item == null ? 43 : $item.hashCode());
        final Object $clueScroll = this.getClueScroll();
        result = result * PRIME + ($clueScroll == null ? 43 : $clueScroll.hashCode());
        final Object $challenge = this.getChallenge();
        result = result * PRIME + ($challenge == null ? 43 : $challenge.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "ClueEntry(slot=" + this.getSlot() + ", item=" + this.getItem() + ", clueScroll=" + this.getClueScroll() + ", challenge=" + this.getChallenge() + ")";
    }
}
