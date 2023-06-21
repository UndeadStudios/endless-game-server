package com.zenyte.network.login.packet;

import com.zenyte.game.world.entity.player.Device;
import com.zenyte.network.ClientResponse;
import com.zenyte.network.PacketOut;

/**
 * @author Tommeh | 27 jul. 2018 | 19:47:44
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class LoginPacketOut implements PacketOut {
	private final ClientResponse response;
	private final Device device;

	public ClientResponse getResponse() {
		return this.response;
	}

	public Device getDevice() {
		return this.device;
	}

	public LoginPacketOut(final ClientResponse response, final Device device) {
		this.response = response;
		this.device = device;
	}
}
