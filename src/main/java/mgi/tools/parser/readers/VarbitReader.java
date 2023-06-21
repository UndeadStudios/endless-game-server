package mgi.tools.parser.readers;

import mgi.tools.parser.TypeReader;
import mgi.types.Definitions;
import mgi.types.config.VarbitDefinitions;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Kris | 16/08/2020
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class VarbitReader implements TypeReader {
    @Override
    public ArrayList<Definitions> read(final Map<String, Object> properties) throws NoSuchFieldException, IllegalAccessException {
        final java.util.ArrayList<mgi.types.Definitions> defs = new ArrayList<Definitions>();
        if (properties.containsKey("inherit")) {
            final java.lang.Object inherit = properties.get("inherit");
            defs.add(VarbitDefinitions.get(((Long) inherit).intValue()));
        } else {
            defs.add(new VarbitDefinitions());
        }
        for (final mgi.types.Definitions definition : defs) {
            final mgi.types.config.VarbitDefinitions varbit = (VarbitDefinitions) definition;
            TypeReader.setFields(varbit, properties);
        }
        return defs;
    }

    @Override
    public String getType() {
        return "varbit";
    }
}
