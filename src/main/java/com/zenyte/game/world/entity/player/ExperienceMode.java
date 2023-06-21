package com.zenyte.game.world.entity.player;

/**
 * @author Tommeh | 26-1-2019 | 22:07
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public enum ExperienceMode {
    TIMES_10(10), TIMES_50(50);
    private final int rate;

    @Override
    public String toString() {
        final java.lang.String base = name();
        return base.substring(base.indexOf("_") + 1) + "x rate";
    }

    public int getRate() {
        return this.rate;
    }

    private ExperienceMode(final int rate) {
        this.rate = rate;
    }
}
