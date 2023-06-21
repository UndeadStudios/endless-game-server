package com.zenyte.plugins.events;

import com.zenyte.plugins.Event;

/**
 * @author Kris | 27/07/2019 06:57
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class ItemDefinitionsLoadedEvent implements Event {
	public ItemDefinitionsLoadedEvent() {
	}

	@Override
	public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
		if (o == this) return true;
		if (!(o instanceof ItemDefinitionsLoadedEvent)) return false;
		final ItemDefinitionsLoadedEvent other = (ItemDefinitionsLoadedEvent) o;
		if (!other.canEqual((Object) this)) return false;
		return true;
	}

	protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
		return other instanceof ItemDefinitionsLoadedEvent;
	}

	@Override
	public int hashCode() {
		final int result = 1;
		return result;
	}

	@org.jetbrains.annotations.NotNull
	@Override
	public String toString() {
		return "ItemDefinitionsLoadedEvent()";
	}
}
