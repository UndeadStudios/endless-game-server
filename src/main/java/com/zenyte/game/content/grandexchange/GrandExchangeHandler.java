package com.zenyte.game.content.grandexchange;

import com.zenyte.cores.CoresManager;
import com.zenyte.game.parser.Parse;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.player.container.Container;
import com.zenyte.game.world.entity.player.container.ContainerPolicy;
import com.zenyte.game.world.entity.player.container.impl.ContainerType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Tommeh | 26 nov. 2017 : 21:36:11
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server
 *      profile</a>}
 */
public class GrandExchangeHandler implements Parse {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GrandExchangeHandler.class);
	private static Map<String, Int2ObjectOpenHashMap<ExchangeOffer>> offers;
	public static final String OFFERS_FILE_DIRECTORY = "./data/grandexchange/offers.json";
	public static final String PRICES_FILE_DIRECTORY = "./data/grandexchange/prices.json";
	private static boolean loaded;
	public static final MutableBoolean status = new MutableBoolean();

	public static void init() {
		try {
			new GrandExchangeHandler().parse();
			new JSONGEItemDefinitionsLoader().parse();
			loaded = true;
		} catch (final Throwable e) {
			log.error(Strings.EMPTY, e);
		}
	}

	public static void save() {
		try {
			if (!loaded) return;
			//Wait until backup manager finishes saving the file(s).
			while (CoresManager.getBackupManager().getStatus().isTrue()) {
				Thread.sleep(1);
			}
			status.setTrue();
			//synchronized (GrandExchange.LOCK) {
			final com.google.gson.Gson gson = World.getGson();
			final java.util.ArrayList<com.zenyte.game.content.grandexchange.ExchangeOffer> list = new ArrayList<ExchangeOffer>(offers.size());
			for (final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<com.zenyte.game.content.grandexchange.ExchangeOffer> map : offers.values()) {
				if (map.isEmpty()) {
					continue;
				}
				list.addAll(map.values());
			}
			final java.lang.String toJson = gson.toJson(list);
			try {
				final PrintWriter pw = new PrintWriter(OFFERS_FILE_DIRECTORY);
				pw.println(toJson);
				pw.close();
			} catch (final Exception e) {
				log.error(Strings.EMPTY, e);
			}
			status.setFalse();
		} catch (
		//}
		Exception e) {
			log.error(Strings.EMPTY, e);
		}
	}

	@NotNull
	static final Int2ObjectOpenHashMap<ExchangeOffer> getOffers(final String username) {
		//synchronized(GrandExchange.LOCK) {
		Int2ObjectOpenHashMap<ExchangeOffer> offers = GrandExchangeHandler.offers.get(username);
		if (offers == null) {
			offers = new Int2ObjectOpenHashMap<>(8);
			GrandExchangeHandler.offers.put(username, offers);
		}
		return offers;
		//}
	}

	@NotNull
	static final Map<String, Int2ObjectOpenHashMap<ExchangeOffer>> getAllOffers() {
		//synchronized(GrandExchange.LOCK) {
		return offers;
		//}
	}

	static void addOffer(final String username, final ExchangeOffer offer) {
		//synchronized(GrandExchange.LOCK) {
		getOffers(username).put(offer.getSlot(), offer);
		//}
	}

	static void remove(final String username, final int slot) {
		//synchronized(GrandExchange.LOCK) {
		getOffers(username).remove(slot);
		//}
	}

	@Override
	public void parse() throws Throwable {
		final File file = new File(OFFERS_FILE_DIRECTORY);
		if (!file.exists()) {
			offers = new HashMap<>();
			return;
		}
		final BufferedReader br = new BufferedReader(new FileReader(file));
		final com.zenyte.game.content.grandexchange.ExchangeOffer[] loadedOffers = World.getGson().fromJson(br, ExchangeOffer[].class);
		offers = new HashMap<>(loadedOffers.length);
		for (final com.zenyte.game.content.grandexchange.ExchangeOffer offer : loadedOffers) {
			Int2ObjectOpenHashMap<ExchangeOffer> currentMap = offers.get(offer.getUsername());
			if (currentMap == null) {
				currentMap = new Int2ObjectOpenHashMap<>(8);
				offers.put(offer.getUsername(), currentMap);
			}
			final com.zenyte.game.world.entity.player.container.Container container = offer.getContainer();
			offer.setContainer(new Container(ContainerPolicy.ALWAYS_STACK, ContainerType.GE_COLLECTABLES_CONTAINERS[offer.getSlot()], Optional.empty()));
			offer.getContainer().setContainer(container);
			offer.getContainer().setFullUpdate(true);
			if (offer.getLastUpdateTime() <= 0) {
				offer.refreshUpdateTime();
			}
			currentMap.put(offer.getSlot(), offer);
		}
	}

	public static Map<String, Int2ObjectOpenHashMap<ExchangeOffer>> getOffers() {
		return GrandExchangeHandler.offers;
	}
}
