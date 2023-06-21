package mgi.tools.parser.readers;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mgi.tools.parser.TypeParser;
import mgi.tools.parser.TypeProperty;
import mgi.tools.parser.TypeReader;
import mgi.types.Definitions;
import mgi.types.config.npcs.NPCDefinitions;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Tommeh | 22/01/2020 | 19:00
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
 */
public class NPCReader implements TypeReader {
    @Override
    public ArrayList<Definitions> read(final Map<String, Object> properties) throws NoSuchFieldException, IllegalAccessException {
        final java.util.ArrayList<mgi.types.Definitions> defs = new ArrayList<Definitions>();
        if (properties.containsKey("inherit")) {
            final java.lang.Object inherit = properties.get("inherit");
            if (inherit instanceof ArrayList) {
                final java.util.ArrayList<java.lang.Long> ids = (ArrayList<Long>) inherit;
                for (final java.lang.Long id : ids) {
                    final mgi.types.config.npcs.NPCDefinitions def = NPCDefinitions.get(id.intValue());
                    defs.add(TypeParser.KRYO.copy(def));
                }
            } else {
                final mgi.types.config.npcs.NPCDefinitions def = NPCDefinitions.get(((Long) inherit).intValue());
                defs.add(TypeParser.KRYO.copy(def));
            }
        } else {
            defs.add(new NPCDefinitions());
        }
        for (final mgi.types.Definitions definition : defs) {
            final mgi.types.config.npcs.NPCDefinitions npc = (NPCDefinitions) definition;
            TypeReader.setFields(npc, properties);
            for (final mgi.tools.parser.TypeProperty property : TypeProperty.values) {
                final java.lang.String identifier = property.getIdentifier();
                if (!properties.containsKey(identifier)) {
                    continue;
                }
                if (property.toString().startsWith("OP_")) {
                    final int index = Integer.parseInt(identifier.substring(2)) - 1;
                    npc.setOption(index, Objects.toString(properties.get(identifier)));
                } else if (property.toString().startsWith("FILTERED_OP_")) {
                    final int index = Integer.parseInt(identifier.substring(10)) - 1;
                    npc.setFilteredOption(index, Objects.toString(properties.get(identifier)));
                } else if (property.equals(TypeProperty.PARAMETERS)) {
                    final java.util.Map<java.lang.String, java.lang.Object> map = (Map<String, Object>) properties.get(identifier);
                    boolean clear = false;
                    if (map.containsKey("clear")) {
                        clear = (Boolean) map.get("clear");
                    }
                    map.remove("clear");
                    final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<java.lang.Object> parameters = (Int2ObjectOpenHashMap<Object>) map.entrySet().stream().collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), Map.Entry::getValue));
                    if (clear || npc.getParameters() == null) {
                        npc.setParameters(parameters);
                    } else {
                        for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<java.lang.Object> entry : parameters.int2ObjectEntrySet()) {
                            npc.getParameters().put(entry.getIntKey(), entry.getValue());
                        }
                    }
                }
            }
        }
        return defs;
    }

    @Override
    public String getType() {
        return "npc";
    }
}
