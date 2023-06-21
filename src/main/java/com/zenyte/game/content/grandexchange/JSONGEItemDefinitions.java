package com.zenyte.game.content.grandexchange;

import java.time.Instant;

public class JSONGEItemDefinitions {
	//All of id, name and price are used!
	private int id;
	private String name;
	private int price;
	private Instant time;//Unused entirely

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public int getPrice() {
		return this.price;
	}

	public Instant getTime() {
		return this.time;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setPrice(final int price) {
		this.price = price;
	}

	public void setTime(final Instant time) {
		this.time = time;
	}

	public JSONGEItemDefinitions() {
	}

	public JSONGEItemDefinitions(final int id, final String name, final int price, final Instant time) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.time = time;
	}
}
