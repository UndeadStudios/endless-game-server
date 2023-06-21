package com.zenyte.game.content.treasuretrails;

import com.google.common.base.Preconditions;
import com.zenyte.game.LineSpacingType;
import com.zenyte.game.constants.GameInterface;
import com.zenyte.game.content.treasuretrails.clues.CharlieTask;
import com.zenyte.game.content.treasuretrails.clues.CrypticClue;
import com.zenyte.game.content.treasuretrails.clues.FaloTheBardClue;
import com.zenyte.game.content.treasuretrails.clues.SherlockTask;
import com.zenyte.game.item.Item;
import com.zenyte.game.ui.Interface;
import com.zenyte.game.world.entity.player.Player;

/**
 * @author Kris | 06/04/2019 17:18
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class ClueInterface extends Interface {
    @Override
    protected void attach() {
        put(2, "Information");
    }

    @Override
    public void open(Player player) {
        final java.lang.Object clue = player.getTemporaryAttributes().get("Clue scroll item");
        Preconditions.checkArgument(clue instanceof Item);
        final com.zenyte.game.item.Item item = (Item) clue;
        final java.util.List<java.lang.String> list = TreasureTrail.getCluesList(item);
        assert list != null;
        assert list.size() >= 1;
        final java.lang.String constantName = list.get(0);
        final com.zenyte.game.content.treasuretrails.clues.Clue clueScroll = TreasureTrail.getClues().get(constantName);
        String text = clueScroll.getText();
        if (clueScroll instanceof SherlockTask) {
            if (item.getNumericAttribute("Sherlock Stage").intValue() == 0) {
                text = "Show this to Sherlock.";
            } else if (item.getNumericAttribute("Sherlock Stage").intValue() == 2) {
                text = "<str>" + text;
            }
        } else if (clueScroll instanceof CharlieTask) {
            if (item.getNumericAttribute("Charlie Stage").intValue() == 0) {
                text = "Show this to Charlie the Tramp.";
            } else if (item.getNumericAttribute("Charlie Stage").intValue() == 2) {
                text = "<str>" + text;
            }
        } else if (clueScroll instanceof FaloTheBardClue) {
            if (item.getNumericAttribute("Falo the Bard Stage").intValue() == 0) {
                text = "Falo the bard wants to see you.";
            }
        } else if (clueScroll instanceof CrypticClue && clueScroll.level() == ClueLevel.MASTER) {
            final java.util.List<com.zenyte.game.content.treasuretrails.clues.CrypticClue> clues = TreasureTrail.getCrypticClues(item);
            final int stage = TreasureTrail.getCrypticCluesStage(item);
            final java.lang.StringBuilder builder = new StringBuilder();
            assert clues.size() == 3;
            for (int i = 0; i < 3; i++) {
                final com.zenyte.game.content.treasuretrails.clues.CrypticClue crypticClue = clues.get(i);
                final boolean completed = ((stage >> i) & 1) == 1;
                if (completed) {
                    builder.append("<str>");
                }
                builder.append(crypticClue.getText());
                if (completed) {
                    builder.append("</str>");
                }
                builder.append("<br><br>");
                player.getPacketDispatcher().sendLineSpacing(LineSpacingType.CENTER, LineSpacingType.CENTER, getInterface().getId(), getComponent("Information"));
            }
            text = builder.toString();
        }
        player.getInterfaceHandler().sendInterface(this);
        player.getPacketDispatcher().sendComponentText(getInterface(), getComponent("Information"), text);
    }

    @Override
    protected void build() {
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.CLUE_SCROLL;
    }
}
