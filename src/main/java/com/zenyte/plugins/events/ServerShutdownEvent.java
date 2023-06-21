package com.zenyte.plugins.events;

import com.zenyte.plugins.Event;

/**
 * @author Kris | 21/03/2019 23:48
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class ServerShutdownEvent implements Event {
	public ServerShutdownEvent() {
	}

	@Override
	public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
		if (o == this) return true;
		if (!(o instanceof ServerShutdownEvent)) return false;
		final ServerShutdownEvent other = (ServerShutdownEvent) o;
		if (!other.canEqual((Object) this)) return false;
		return true;
	}

	protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
		return other instanceof ServerShutdownEvent;
	}

	@Override
	public int hashCode() {
		final int result = 1;
		return result;
	}

	@org.jetbrains.annotations.NotNull
	@Override
	public String toString() {
		return "ServerShutdownEvent()";
	}
}
