package com.zenyte.game.item;

import com.zenyte.game.parser.Parse;
import com.zenyte.game.util.Examine;
import com.zenyte.game.util.LabelledExamine;
import com.zenyte.game.world.World;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ItemExamineLoader implements Parse {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ItemExamineLoader.class);
    public static final Map<Integer, Examine> DEFINITIONS = new HashMap<>();

    @Override
    public void parse() throws Throwable {
        final BufferedReader br = new BufferedReader(new FileReader("data/examines/Item examines.json"));
        final Examine[] examines = World.getGson().fromJson(br, Examine[].class);
        for (final Examine def : examines) {
            if (def != null) DEFINITIONS.put(def.getId(), def);
        }
        parseOverrides();
    }

    private void parseOverrides() throws Throwable {
        final BufferedReader br = new BufferedReader(new FileReader("data/examines/Forced item examines.json"));
        final LabelledExamine[] examines = World.getGson().fromJson(br, LabelledExamine[].class);
        for (final LabelledExamine def : examines) {
            DEFINITIONS.put(def.getId(), def);
        }
    }

    public static final void loadExamines() {
        try {
            new ItemExamineLoader().parse();
        } catch (final Throwable e) {
            log.error(Strings.EMPTY, e);
        }
    }
}
