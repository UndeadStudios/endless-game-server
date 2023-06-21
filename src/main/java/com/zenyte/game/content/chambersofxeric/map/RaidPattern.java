package com.zenyte.game.content.chambersofxeric.map;

import com.zenyte.game.util.Direction;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.World;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Kris | 8. mai 2018 : 20:35:37
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class RaidPattern {
    /**
     * Do NOT change the order of the constants - serialized to players.
     */
    private static final String[] layoutCodes = {"#FSCCP*#PCSCF* - #WNWSWN#ESEENW", "#FSCCS*#PCPSF* - #WSEEEN#WSWNWS", "#FSCPC*#CSCPF* - #WNWWSE#EENWWW", "#SCCFC*#PSCSF* - #EEENWW#WSEEEN", "#SCCFP*#CCSPF* - #NESEEN#WSWNWS", "#SCFCP*#CCSPF* - #ESEENW#ESWWNW", "#SCFCP*#CSCFS* - #ENEESW#ENWWSW", "#SCFPC*#CSPCF* - #WSWWNE#WSEENE", "#SCFPC*#PCCSF* - #WSEENE#WWWSEE", "#SCFPC*#SCPCF* - #NESENE#WSWWNE", "#SCPFC*#CCPSF* - #NWWWSE#WNEESE", "#SCPFC*#CSPCF* - #NEEESW#WWNEEE", "#SCPFC*#CSPSF* - #WWSEEE#NWSWWN", "#SCSPF*#CCSPF* - #ESWWNW#ESENES", "#SFCCP*#CSCPF* - #WNEESE#NWSWWN", "#SFCCS*#PCPSF* - #ENWWSW#ENESEN", "#SPCFC*#CSPCF* - #WWNEEE#WSWNWS", "#SPCFC*#SCCPF* - #ESENES#WWWNEE", "#SPSFP*#CCCSF* - #NWSWWN#ESEENW", "#SCFCP*#CSCPF* - #ENESEN#WWWSEE", "#SCPFC*#PCSCF* - #WNEEES#NWSWNW", "#FSPCC*#PSCCF* - #WWWSEE#ENWWSW", "#FSCCP*#PCSCF* - #ENWWWS#NEESEN", "#SCPFC*#CCSSF* - #NEESEN#WSWWNE", "#SFCCPC*#PCSCPF* - #WSEENES#WWWNEEE", "#SCFCPC*#CSPCSF* - #ESWWNWS#NESENES"};
    private static final RaidPattern[] values;

    static {
        final it.unimi.dsi.fastutil.objects.ObjectArrayList<com.zenyte.game.content.chambersofxeric.map.RaidPattern> patternList = new ObjectArrayList<RaidPattern>();
        for (final java.lang.String code : layoutCodes) {
            final java.lang.String[] split = code.split(" - ");
            final java.lang.String rooms = split[0];
            final java.lang.String directions = split[1];
            final it.unimi.dsi.fastutil.objects.ObjectArrayList<com.zenyte.game.content.chambersofxeric.map.LayoutTypeRoom> l = new ObjectArrayList<LayoutTypeRoom>(16);
            for (int i = 0; i < rooms.length(); i++) {
                final char c = rooms.charAt(i);
                final com.zenyte.game.content.chambersofxeric.map.RoomType type = c == '#' ? RoomType.START : c == '*' ? RoomType.END : c == 'S' ? RoomType.SCAVENGERS : c == 'C' ? RoomType.COMBAT : c == 'F' ? RoomType.FARMING : RoomType.PUZZLE;
                final char d = directions.charAt(i);
                final com.zenyte.game.util.Direction direction = d == '#' ? null : d == 'N' ? Direction.NORTH : d == 'E' ? Direction.EAST : d == 'S' ? Direction.SOUTH : Direction.WEST;
                l.add(new LayoutTypeRoom(type, direction));
            }
            patternList.add(new RaidPattern(l, code));
        }
        values = patternList.toArray(new RaidPattern[0]);
    }

    private final List<LayoutTypeRoom> pattern;
    private final String layout;
    private final int combatRooms;

    private RaidPattern(@NotNull final List<LayoutTypeRoom> pattern, final String layout) {
        this.pattern = pattern;
        this.layout = layout;
        this.combatRooms = (int) pattern.stream().filter(type -> type.getType() == RoomType.COMBAT).count();
    }

    /**
     * Picks a random raid pattern out of the 26 available options.
     * @return a random raid pattern.
     */
    @NotNull
    public static final RaidPattern random(@NotNull final String partyOwner) {
        final java.util.Optional<com.zenyte.game.world.entity.player.Player> owner = World.getPlayer(partyOwner);
        if (owner.isPresent()) {
            final com.zenyte.game.world.entity.player.Player player = owner.get();
            final int disabledSettings = player.getNumericAttribute("disabled raids layouts").intValue();
            ;
            if (disabledSettings != 0) {
                final it.unimi.dsi.fastutil.objects.ObjectArrayList<com.zenyte.game.content.chambersofxeric.map.RaidPattern> listOfAvailablePatterns = new ObjectArrayList<RaidPattern>();
                for (int i = 0; i < values.length; i++) {
                    final boolean isEnabled = ((disabledSettings >> i) & 1) == 0;
                    if (isEnabled) {
                        listOfAvailablePatterns.add(values[i]);
                    }
                }
                if (!listOfAvailablePatterns.isEmpty()) {
                    return listOfAvailablePatterns.get(Utils.random(listOfAvailablePatterns.size() - 1));
                }
            }
        }
        return values[Utils.random(values.length - 1)];
    }

    /**
     * Do NOT change the order of the constants - serialized to players.
     */
    public static String[] getLayoutCodes() {
        return RaidPattern.layoutCodes;
    }

    public List<LayoutTypeRoom> getPattern() {
        return this.pattern;
    }

    public String getLayout() {
        return this.layout;
    }

    public int getCombatRooms() {
        return this.combatRooms;
    }
}
