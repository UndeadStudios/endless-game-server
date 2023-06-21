package com.zenyte.game.content.pyramidplunder;

/**
 * @author Christopher
 * @since 4/4/2020
 */
public class WeightedPlunderRewardTier {
    private final PlunderRewardTier tier;
    private final int weight;

    public WeightedPlunderRewardTier(final PlunderRewardTier tier, final int weight) {
        this.tier = tier;
        this.weight = weight;
    }

    public PlunderRewardTier getTier() {
        return this.tier;
    }

    public int getWeight() {
        return this.weight;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof WeightedPlunderRewardTier)) return false;
        final WeightedPlunderRewardTier other = (WeightedPlunderRewardTier) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getWeight() != other.getWeight()) return false;
        final Object this$tier = this.getTier();
        final Object other$tier = other.getTier();
        if (this$tier == null ? other$tier != null : !this$tier.equals(other$tier)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof WeightedPlunderRewardTier;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getWeight();
        final Object $tier = this.getTier();
        result = result * PRIME + ($tier == null ? 43 : $tier.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "WeightedPlunderRewardTier(tier=" + this.getTier() + ", weight=" + this.getWeight() + ")";
    }
}
