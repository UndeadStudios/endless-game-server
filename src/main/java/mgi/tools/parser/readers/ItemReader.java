package mgi.tools.parser.readers;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mgi.tools.parser.TypeParser;
import mgi.tools.parser.TypeProperty;
import mgi.tools.parser.TypeReader;
import mgi.types.Definitions;
import mgi.types.config.items.ItemDefinitions;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Tommeh | 22/01/2020 | 18:56
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
 */
public class ItemReader implements TypeReader {
    @Override
    public ArrayList<Definitions> read(final Map<String, Object> properties) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        final java.util.ArrayList<mgi.types.Definitions> defs = new ArrayList<Definitions>();
        if (properties.containsKey("inherit")) {
            final java.lang.Object inherit = properties.get("inherit");
            if (inherit instanceof ArrayList) {
                final java.util.ArrayList<java.lang.Long> ids = (ArrayList<Long>) inherit;
                for (final java.lang.Long id : ids) {
                    final mgi.types.config.items.ItemDefinitions def = ItemDefinitions.get(id.intValue());
                    defs.add(TypeParser.KRYO.copy(def));
                }
            } else {
                final mgi.types.config.items.ItemDefinitions def = ItemDefinitions.get(((Long) inherit).intValue());
                defs.add(TypeParser.KRYO.copy(def));
            }
        } else {
            defs.add(new ItemDefinitions());
        }
        for (final mgi.types.Definitions definition : defs) {
            final mgi.types.config.items.ItemDefinitions item = (ItemDefinitions) definition;
            TypeReader.setFields(item, properties);
            for (final mgi.tools.parser.TypeProperty property : TypeProperty.values) {
                final java.lang.String identifier = property.getIdentifier();
                if (!properties.containsKey(identifier)) {
                    continue;
                }
                if (property.toString().startsWith("OP_")) {
                    final int index = Integer.parseInt(identifier.substring(2)) - 1;
                    item.setOption(index, Objects.toString(properties.get(identifier)));
                } else if (property.equals(TypeProperty.PARAMETERS)) {
                    final java.util.Map<java.lang.String, java.lang.Object> map = (Map<String, Object>) properties.get(identifier);
                    boolean clear = false;
                    if (map.containsKey("clear")) {
                        clear = (Boolean) map.get("clear");
                    }
                    map.remove("clear");
                    final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<java.lang.Object> parameters = new Int2ObjectOpenHashMap<Object>(map.entrySet().stream().collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), Map.Entry::getValue)));
                    if (clear || item.getParameters() == null) {
                        item.setParameters(parameters);
                    } else {
                        for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<java.lang.Object> entry : parameters.int2ObjectEntrySet()) {
                            item.getParameters().put(entry.getIntKey(), entry.getValue());
                        }
                    }
                }
            }
        }
        return defs;
    }

    @Override
    public String getType() {
        return "item";
    }
}
