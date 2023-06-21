package com.zenyte.game.content.tournament;

import com.zenyte.game.content.tournament.plugins.TournamentLobby;
import com.zenyte.game.tasks.WorldTasksManager;
import com.zenyte.game.ui.InterfacePosition;
import com.zenyte.game.util.Colour;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.masks.Graphics;
import com.zenyte.game.world.entity.player.MessageType;
import com.zenyte.game.world.entity.player.Player;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Kris | 26/05/2019 20:11
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public final class Tournament {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Tournament.class);
    public static final List<Tournament> tournaments = new ArrayList<>();

    public Tournament(@NotNull final TournamentLobby lobby) {
        this.lobby = lobby;
    }

    @NotNull
    private final TournamentLobby lobby;
    @NotNull
    private final Queue<Pair<Player, Player>> pairs = new LinkedList<>();
    @NotNull
    private final List<Player> participants = new ArrayList<>();
    @NotNull
    private final List<Player> contestants = new LinkedList<>();
    private int round;
    private boolean finished;

    public final void start(@NotNull final List<Player> participants) {
        //assert this.participants.isEmpty();
        this.participants.clear();
        this.participants.addAll(participants);
        ++round;
        createPairs();
        lobby.refreshRound();
    }

    private void createPairs() {
        assert participants.size() >= 2;
        assert participants.size() <= 2048;
        pairs.clear();
        final double size = Math.floor(participants.size() / 2.0F);
        final java.util.ArrayList<com.zenyte.game.world.entity.player.Player> modifiablePlayersList = new ArrayList<>(participants);
        contestants.addAll(modifiablePlayersList);
        for (int i = 0; i < size; i++) {
            pairs.add(getPairAndRemoveMembers(modifiablePlayersList));
        }
        for (final com.zenyte.game.world.entity.player.Player remainingMember : modifiablePlayersList) {
            lobby.win(remainingMember, "You have been moved on to the next round as your opponent has left the tournament.");
        }
    }

    @NotNull
    private <T> Pair<T, T> getPairAndRemoveMembers(@NotNull final List<T> members) {
        return Pair.of(members.remove(Utils.random(members.size() - 1)), members.remove(Utils.random(members.size() - 1)));
    }

    public void remove(@NotNull final Player participant) {
        final boolean state = participants.remove(participant) && contestants.remove(participant);
        if (!state) {
            return;
        }
        final org.apache.commons.lang3.tuple.Pair<com.zenyte.game.world.entity.player.Player, com.zenyte.game.world.entity.player.Player> pair = findPair(participant);
        if (pair == null) {
            return;
        }
        final boolean success = pairs.remove(pair);
        assert success : "Failure to remove the pair - it does not exist.";
        final com.zenyte.game.world.entity.player.Player winner = pair.getLeft() == participant ? pair.getRight() : pair.getLeft();
        lobby.win(winner, "You have been moved on to the next round.");
        participant.getInterfaceHandler().closeInterface(InterfacePosition.WILDERNESS_OVERLAY);
        final java.util.List<com.zenyte.game.world.entity.player.Player> spectatorsList = lobby.getFight().getSpectatorMap().get(pair);
        if (spectatorsList != null) {
            final java.util.ArrayList<com.zenyte.game.world.entity.player.Player> entityList = new ArrayList<>(spectatorsList);
            for (final com.zenyte.game.world.entity.player.Player spectator : entityList) {
                spectator.getInterfaceHandler().closeInterfaces();
            }
        }
        if (pairs.isEmpty()) {
            if (lobby.getLobbyPlayers().size() <= 1) {
                win(winner, pair);
            } else {
                log.info("Tournament round " + round + " has finished; starting next round.");
                WorldTasksManager.schedule(() -> lobby.schedule(60, lobby::beginFight));
            }
        }
    }

    public void win(@NotNull final Player winner, @Nullable Pair<Player, Player> pair) {
        log.info("Tournament round " + round + " has finished; The winner is " + winner.getName() + "!");
        winner.getInterfaceHandler().closeInterface(InterfacePosition.MINIGAME_OVERLAY);
        final com.zenyte.game.world.entity.Location tile = lobby.getLocation(TournamentLobby.WINNER_LOCATION);
        winner.setLocation(tile);
        World.sendMessage(MessageType.GLOBAL_BROADCAST, winner.getName() + " has won the 1v1 " + this.getLobby().getPreset().toString() + " tournament!");
        winner.sendMessage("Congratulations, you won the tournament!");
        WorldTasksManager.schedule(() -> {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    World.sendGraphics(new Graphics(1388, Utils.random(60), 90), tile.transform(x, y, 0));
                }
            }
        });
        for (final com.zenyte.game.world.entity.player.Player player : lobby.getPlayers()) {
            player.getInterfaceHandler().closeInterface(InterfacePosition.MINIGAME_OVERLAY);
        }
        finished = true;
        //TODO give rewards/possibly reward interface?
    }

    final Pair<Player, Player> findPair(@NotNull final Player player) {
        for (final org.apache.commons.lang3.tuple.Pair<com.zenyte.game.world.entity.player.Player, com.zenyte.game.world.entity.player.Player> contestantPair : pairs) {
            if (contestantPair.getLeft() == player || contestantPair.getRight() == player) {
                return contestantPair;
            }
        }
        return null;
    }

    @NotNull
    public Queue<Pair<Player, Player>> getPairs() {
        return pairs;
    }

    public boolean expired() {
        return lobby.getDate().before(new Date());
    }

    @Override
    public String toString() {
        return "Preset: " + lobby.getPreset().toString() + "; Date: " + lobby.getDate() + "; " + (expired() ? "<col=ff0000>Inactive" : Colour.RS_GREEN.wrap("Active"));
    }

    @NotNull
    public TournamentLobby getLobby() {
        return this.lobby;
    }

    @NotNull
    public List<Player> getParticipants() {
        return this.participants;
    }

    public int getRound() {
        return this.round;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void setFinished(final boolean finished) {
        this.finished = finished;
    }
}
