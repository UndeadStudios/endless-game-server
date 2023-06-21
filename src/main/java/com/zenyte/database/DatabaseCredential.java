package com.zenyte.database;

public enum DatabaseCredential {
	LOCAL("localhost", "exilius", "Crm561996!"), BETA("localhost", "exilius", "Crm561996!");
	private final String host;
	private final String user;
	private final String pass;

	DatabaseCredential(String host, String user, String pass) {
		this.host = host;
		this.user = user;
		this.pass = pass;
	}

	public String getHost() {
		return this.host;
	}

	public String getUser() {
		return this.user;
	}

	public String getPass() {
		return this.pass;
	}
}
