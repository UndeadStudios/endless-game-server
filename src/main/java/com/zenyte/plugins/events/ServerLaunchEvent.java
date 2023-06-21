package com.zenyte.plugins.events;

import com.zenyte.plugins.Event;

/**
 * @author Kris | 21/03/2019 23:46
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class ServerLaunchEvent implements Event {
	public ServerLaunchEvent() {
	}

	@Override
	public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
		if (o == this) return true;
		if (!(o instanceof ServerLaunchEvent)) return false;
		final ServerLaunchEvent other = (ServerLaunchEvent) o;
		if (!other.canEqual((Object) this)) return false;
		return true;
	}

	protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
		return other instanceof ServerLaunchEvent;
	}

	@Override
	public int hashCode() {
		final int result = 1;
		return result;
	}

	@org.jetbrains.annotations.NotNull
	@Override
	public String toString() {
		return "ServerLaunchEvent()";
	}
}
