package mgi.types.config.items;

import com.zenyte.game.world.entity.masks.RenderAnimation;

public class WieldableDefinition {
	private boolean twoHanded;
	private int blockAnimation;
	private int standAnimation = RenderAnimation.STAND;
	private int walkAnimation = RenderAnimation.WALK;
	private int runAnimation = RenderAnimation.RUN;
	private int standTurnAnimation = RenderAnimation.STAND_TURN;
	private int rotate90Animation = RenderAnimation.ROTATE90;
	private int rotate180Animation = RenderAnimation.ROTATE180;
	private int rotate270Animation = RenderAnimation.ROTATE270;
	private int accurateAnimation;
	private int aggressiveAnimation;
	private int controlledAnimation;
	private int defensiveAnimation;
	private int attackSpeed;
	private int interfaceVarbit;
	private int normalAttackDistance;
	private int longAttackDistance;

	public boolean isTwoHanded() {
		return this.twoHanded;
	}

	public void setTwoHanded(final boolean twoHanded) {
		this.twoHanded = twoHanded;
	}

	public int getBlockAnimation() {
		return this.blockAnimation;
	}

	public void setBlockAnimation(final int blockAnimation) {
		this.blockAnimation = blockAnimation;
	}

	public int getStandAnimation() {
		return this.standAnimation;
	}

	public void setStandAnimation(final int standAnimation) {
		this.standAnimation = standAnimation;
	}

	public int getWalkAnimation() {
		return this.walkAnimation;
	}

	public void setWalkAnimation(final int walkAnimation) {
		this.walkAnimation = walkAnimation;
	}

	public int getRunAnimation() {
		return this.runAnimation;
	}

	public void setRunAnimation(final int runAnimation) {
		this.runAnimation = runAnimation;
	}

	public int getStandTurnAnimation() {
		return this.standTurnAnimation;
	}

	public void setStandTurnAnimation(final int standTurnAnimation) {
		this.standTurnAnimation = standTurnAnimation;
	}

	public int getRotate90Animation() {
		return this.rotate90Animation;
	}

	public void setRotate90Animation(final int rotate90Animation) {
		this.rotate90Animation = rotate90Animation;
	}

	public int getRotate180Animation() {
		return this.rotate180Animation;
	}

	public void setRotate180Animation(final int rotate180Animation) {
		this.rotate180Animation = rotate180Animation;
	}

	public int getRotate270Animation() {
		return this.rotate270Animation;
	}

	public void setRotate270Animation(final int rotate270Animation) {
		this.rotate270Animation = rotate270Animation;
	}

	public int getAccurateAnimation() {
		return this.accurateAnimation;
	}

	public void setAccurateAnimation(final int accurateAnimation) {
		this.accurateAnimation = accurateAnimation;
	}

	public int getAggressiveAnimation() {
		return this.aggressiveAnimation;
	}

	public void setAggressiveAnimation(final int aggressiveAnimation) {
		this.aggressiveAnimation = aggressiveAnimation;
	}

	public int getControlledAnimation() {
		return this.controlledAnimation;
	}

	public void setControlledAnimation(final int controlledAnimation) {
		this.controlledAnimation = controlledAnimation;
	}

	public int getDefensiveAnimation() {
		return this.defensiveAnimation;
	}

	public void setDefensiveAnimation(final int defensiveAnimation) {
		this.defensiveAnimation = defensiveAnimation;
	}

	public int getAttackSpeed() {
		return this.attackSpeed;
	}

	public void setAttackSpeed(final int attackSpeed) {
		this.attackSpeed = attackSpeed;
	}

	public int getInterfaceVarbit() {
		return this.interfaceVarbit;
	}

	public void setInterfaceVarbit(final int interfaceVarbit) {
		this.interfaceVarbit = interfaceVarbit;
	}

	public int getNormalAttackDistance() {
		return this.normalAttackDistance;
	}

	public void setNormalAttackDistance(final int normalAttackDistance) {
		this.normalAttackDistance = normalAttackDistance;
	}

	public int getLongAttackDistance() {
		return this.longAttackDistance;
	}

	public void setLongAttackDistance(final int longAttackDistance) {
		this.longAttackDistance = longAttackDistance;
	}
}
