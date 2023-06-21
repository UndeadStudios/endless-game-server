package com.zenyte.game.world.entity.masks;

import com.zenyte.Constants;
import mgi.types.config.AnimationDefinitions;

/**
 * @author Kris | 6. nov 2017 : 14:25.26
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class Animation {
	private final int id;
	private final int delay;
	public static final Animation STOP = new Animation(-1);
	public static final Animation JUMP = new Animation(741);
	public static final Animation LEAP = new Animation(2586);
	public static final Animation LAND = new Animation(2588);
	public static final Animation CUT_WATERMELON = new Animation(2269);
	public static final Animation SMITH = new Animation(3243);
	public static final Animation LADDER_UP = new Animation(828);
	public static final Animation LADDER_DOWN = new Animation(827);
	public static final Animation GRAB = new Animation(832);

	public Animation(final int id) {
		this(id, 0);
	}

	public Animation(final int id, final int delay) {
		this.id = id;
		this.delay = delay;
	}

	public final int getDuration() {
		final AnimationDefinitions defs = AnimationDefinitions.get(id);
		if (defs == null) {
			return 0;
		}
		return (int) (defs.getDuration() + (Constants.CLIENT_CYCLE * delay));
	}

	public final int getCeiledDuration() {
		final AnimationDefinitions defs = AnimationDefinitions.get(id);
		if (defs == null) {
			return 0;
		}
		final int duration = (int) (defs.getDuration() + (Constants.CLIENT_CYCLE * delay));
		final float remainder = duration % Constants.TICK;
		if (remainder > 0) {
			return (int) (duration + (Constants.TICK - remainder));
		}
		return duration;
	}

	/**
	 * Calculates the additional delay of the animation so it will be synchronized within game ticks, which means the animation ends exactly
	 * when the next game cycle is initiated. The delay is always ceiled, so if animation duration is for example 620ms, the delay returned
	 * will push the animation's "duration" to 1200ms.
	 * 
	 * @param id
	 *            the id of the animation.
	 * @return the additional delay of the animation to synchronize it with game ticks.
	 */
	public static final int getSynchronizedAnimationDelay(final int id) {
		final mgi.types.config.AnimationDefinitions definitions = AnimationDefinitions.get(id);
		if (definitions == null) {
			return 0;
		}
		final int duration = definitions.getDuration();
		final float remainderCycles = (duration - ((int) (duration / Constants.TICK) * Constants.TICK)) / Constants.CLIENT_CYCLE;
		return (int) (Constants.CYCLES_PER_TICK - remainderCycles);
	}

	@org.jetbrains.annotations.NotNull
	@Override
	public String toString() {
		return "Animation(id=" + this.getId() + ", delay=" + this.getDelay() + ")";
	}

	public int getId() {
		return this.id;
	}

	public int getDelay() {
		return this.delay;
	}
}
