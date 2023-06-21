package com.zenyte.game.content.treasuretrails.challenges;

import com.zenyte.game.content.treasuretrails.ClueLevel;
import com.zenyte.game.content.treasuretrails.clues.emote.ItemRequirement;
import com.zenyte.game.world.entity.player.Emote;
import com.zenyte.game.world.region.RSPolygon;

import java.util.List;

/**
 * @author Kris | 23/11/2019
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public final class EmoteRequest implements ClueChallenge {
    private final List<Emote> emotes;
    private final boolean agents;
    private final ItemRequirement[] requirements;
    private final RSPolygon polygon;
    private final ClueLevel level;

    public EmoteRequest(final List<Emote> emotes, final boolean agents, final ItemRequirement[] requirements, final RSPolygon polygon, final ClueLevel level) {
        this.emotes = emotes;
        this.agents = agents;
        this.requirements = requirements;
        this.polygon = polygon;
        this.level = level;
    }

    public List<Emote> getEmotes() {
        return this.emotes;
    }

    public boolean isAgents() {
        return this.agents;
    }

    public ItemRequirement[] getRequirements() {
        return this.requirements;
    }

    public RSPolygon getPolygon() {
        return this.polygon;
    }

    public ClueLevel getLevel() {
        return this.level;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof EmoteRequest)) return false;
        final EmoteRequest other = (EmoteRequest) o;
        if (this.isAgents() != other.isAgents()) return false;
        final Object this$emotes = this.getEmotes();
        final Object other$emotes = other.getEmotes();
        if (this$emotes == null ? other$emotes != null : !this$emotes.equals(other$emotes)) return false;
        if (!java.util.Arrays.deepEquals(this.getRequirements(), other.getRequirements())) return false;
        final Object this$polygon = this.getPolygon();
        final Object other$polygon = other.getPolygon();
        if (this$polygon == null ? other$polygon != null : !this$polygon.equals(other$polygon)) return false;
        final Object this$level = this.getLevel();
        final Object other$level = other.getLevel();
        if (this$level == null ? other$level != null : !this$level.equals(other$level)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isAgents() ? 79 : 97);
        final Object $emotes = this.getEmotes();
        result = result * PRIME + ($emotes == null ? 43 : $emotes.hashCode());
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getRequirements());
        final Object $polygon = this.getPolygon();
        result = result * PRIME + ($polygon == null ? 43 : $polygon.hashCode());
        final Object $level = this.getLevel();
        result = result * PRIME + ($level == null ? 43 : $level.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "EmoteRequest(emotes=" + this.getEmotes() + ", agents=" + this.isAgents() + ", requirements=" + java.util.Arrays.deepToString(this.getRequirements()) + ", polygon=" + this.getPolygon() + ", level=" + this.getLevel() + ")";
    }
}
