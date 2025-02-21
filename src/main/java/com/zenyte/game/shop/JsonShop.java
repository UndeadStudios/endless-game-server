package com.zenyte.game.shop;

import java.util.List;

/**
 * @author Kris | 26. sept 2018 : 02:32:39
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
class JsonShop {
	private final String shopName;
	private final ShopCurrency currency;
	private final ShopPolicy sellPolicy;
	private final float sellMultiplier;
	private final List<Item> items;


	static class Item {
		int id;
		int amount;
		int sellPrice;
		int buyPrice;
		int restockTimer;
		boolean ironmanRestricted;
	}

	public JsonShop(final String shopName, final ShopCurrency currency, final ShopPolicy sellPolicy, final float sellMultiplier, final List<Item> items) {
		this.shopName = shopName;
		this.currency = currency;
		this.sellPolicy = sellPolicy;
		this.sellMultiplier = sellMultiplier;
		this.items = items;
	}

	public String getShopName() {
		return this.shopName;
	}

	public ShopCurrency getCurrency() {
		return this.currency;
	}

	public ShopPolicy getSellPolicy() {
		return this.sellPolicy;
	}

	public float getSellMultiplier() {
		return this.sellMultiplier;
	}

	public List<Item> getItems() {
		return this.items;
	}

	@Override
	public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
		if (o == this) return true;
		if (!(o instanceof JsonShop)) return false;
		final JsonShop other = (JsonShop) o;
		if (!other.canEqual((Object) this)) return false;
		if (Float.compare(this.getSellMultiplier(), other.getSellMultiplier()) != 0) return false;
		final Object this$shopName = this.getShopName();
		final Object other$shopName = other.getShopName();
		if (this$shopName == null ? other$shopName != null : !this$shopName.equals(other$shopName)) return false;
		final Object this$currency = this.getCurrency();
		final Object other$currency = other.getCurrency();
		if (this$currency == null ? other$currency != null : !this$currency.equals(other$currency)) return false;
		final Object this$sellPolicy = this.getSellPolicy();
		final Object other$sellPolicy = other.getSellPolicy();
		if (this$sellPolicy == null ? other$sellPolicy != null : !this$sellPolicy.equals(other$sellPolicy)) return false;
		final Object this$items = this.getItems();
		final Object other$items = other.getItems();
		if (this$items == null ? other$items != null : !this$items.equals(other$items)) return false;
		return true;
	}

	protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
		return other instanceof JsonShop;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + Float.floatToIntBits(this.getSellMultiplier());
		final Object $shopName = this.getShopName();
		result = result * PRIME + ($shopName == null ? 43 : $shopName.hashCode());
		final Object $currency = this.getCurrency();
		result = result * PRIME + ($currency == null ? 43 : $currency.hashCode());
		final Object $sellPolicy = this.getSellPolicy();
		result = result * PRIME + ($sellPolicy == null ? 43 : $sellPolicy.hashCode());
		final Object $items = this.getItems();
		result = result * PRIME + ($items == null ? 43 : $items.hashCode());
		return result;
	}

	@org.jetbrains.annotations.NotNull
	@Override
	public String toString() {
		return "JsonShop(shopName=" + this.getShopName() + ", currency=" + this.getCurrency() + ", sellPolicy=" + this.getSellPolicy() + ", sellMultiplier=" + this.getSellMultiplier() + ", items=" + this.getItems() + ")";
	}
}
