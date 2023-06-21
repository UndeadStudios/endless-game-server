package com.zenyte.game.packet.in.event;

import com.zenyte.game.constants.GameInterface;
import com.zenyte.game.packet.in.ClientProtEvent;
import com.zenyte.game.ui.ButtonAction;
import com.zenyte.game.world.entity.player.LogLevel;
import com.zenyte.game.world.entity.player.Player;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Tommeh | 25-1-2019 | 20:19
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class If3ButtonEvent implements ClientProtEvent {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(If3ButtonEvent.class);
    private int interfaceId;
    private int componentId;
    private int slotId;
    private int itemId;
    private int option;

    @Override
    public void handle(Player player) {
        if (itemId == 65535) {
            itemId = -1;
        }
        if (slotId == 65535) {
            slotId = -1;
        }
        ButtonAction.handleComponentAction(player, interfaceId, componentId, slotId, itemId, option, 3);
    }

    @Override
    public void log(@NotNull final Player player) {
        final java.util.Optional<com.zenyte.game.constants.GameInterface> interfaceName = GameInterface.get(interfaceId);
        final java.lang.String name = interfaceName.isPresent() ? interfaceName.get().toString() : Strings.EMPTY;
        log(player, "Interface: " + name + ", id: " + interfaceId + ", component: " + componentId + ", slot: " + slotId + ", item: " + itemId + ", option: " + option);
    }

    @Override
    public LogLevel level() {
        return LogLevel.HIGH_PACKET;
    }

    public If3ButtonEvent(final int interfaceId, final int componentId, final int slotId, final int itemId, final int option) {
        this.interfaceId = interfaceId;
        this.componentId = componentId;
        this.slotId = slotId;
        this.itemId = itemId;
        this.option = option;
    }
}
