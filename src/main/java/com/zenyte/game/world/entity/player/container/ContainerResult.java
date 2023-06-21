package com.zenyte.game.world.entity.player.container;

import com.zenyte.game.item.Item;

/**
 * @author Kris | 3. mai 2018 : 19:22:19
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>}
 */
public final class ContainerResult {
	public ContainerResult(final Item item, final ContainerState state) {
		this.item = item == null ? null : new Item(item.getId(), item.getAmount(), item.getAttributesCopy());
		this.state = state;
	}

	private final Item item;
	private final ContainerState state;
	private int succeededAmount;
	private RequestResult result;

	public void onFailure(final ContainerFailure runnable) {
		if (result == RequestResult.SUCCESS || item.getAmount() == succeededAmount) {
			return;
		}
		runnable.execute(new Item(item.getId(), item.getAmount() - succeededAmount, item.getAttributesCopy()));
	}

	public boolean isFailure() {
		return !result.equals(RequestResult.SUCCESS) || item.getAmount() != succeededAmount;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("State: ").append(state.toString()).append(", ");
		builder.append("Item: ").append(item.toString()).append(", ");
		builder.append("Succeeded amount: ").append(succeededAmount).append(", ");
		builder.append("Result: ").append(result.toString()).append(".");
		return builder.toString();
	}

	public Item getItem() {
		return this.item;
	}

	public ContainerState getState() {
		return this.state;
	}

	public int getSucceededAmount() {
		return this.succeededAmount;
	}

	public void setSucceededAmount(final int succeededAmount) {
		this.succeededAmount = succeededAmount;
	}

	public RequestResult getResult() {
		return this.result;
	}

	public void setResult(final RequestResult result) {
		this.result = result;
	}
}
