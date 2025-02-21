package com.zenyte.tools;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.zenyte.Constants;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Tommeh | 18/11/2019 | 18:11
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
 */
public class ItemExamineExtractor {

    public static class ItemExamine {
        private final int id;
        private final String examine;

        public int getId() {
            return this.id;
        }

        public String getExamine() {
            return this.examine;
        }

        public ItemExamine(final int id, final String examine) {
            this.id = id;
            this.examine = examine;
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ItemExamine[] customExamines = {new ItemExamine(30050, "A pair of ten-sided dice, for results between 1 and 100.")};

    public static void main(String[] args) throws Exception {
        final java.io.InputStream is = new URL("https://www.osrsbox.com/osrsbox-db/items-complete.json").openStream();
        final java.io.BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        final Map<String, Object> map = new Gson().fromJson(br, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        final java.util.ArrayList<com.zenyte.tools.ItemExamineExtractor.ItemExamine> list = new ArrayList<ItemExamine>();
        map.forEach((k, v) -> {
            final int id = Integer.parseInt(k);
            final com.google.gson.internal.LinkedTreeMap<java.lang.String, java.lang.Object> innerMap = (LinkedTreeMap<String, Object>) v;
            list.add(new ItemExamine(id, (String) innerMap.get("examine")));
        });
        Collections.addAll(list, customExamines);
        list.removeIf(Objects::isNull);
        list.removeIf(summary -> summary.getExamine() == null || summary.getExamine().isEmpty());
        list.sort(Comparator.comparingInt(summary -> summary.getId()));
        final java.io.FileWriter bw = new FileWriter("data/examines/#" + Constants.REVISION + "-item-examines.json");
        GSON.toJson(list, bw);
        bw.close();
    }
}
