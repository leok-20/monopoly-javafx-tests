package cz.cvut.fel.pvj.nejedly.monopoly.model.decks.cards;

import cz.cvut.fel.pvj.nejedly.monopoly.model.player.Player;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayMoneyCardTest {

    // Tests paying a fixed amount of money
    @Test
    void payFixedAmount() {
        Player player = new Player(
            null, 0, 1500, false, false,
            FXCollections.observableArrayList(), 0
        );

        PayMoneyCard card = new PayMoneyCard(
            CardType.CHANCE, "Pay 100", 100
        );

        ArrayList<Player> players = new ArrayList<>();
        players.add(player);

        card.execute(player, players);
        assertEquals(1400, player.getMoney().get());
    }

    // Tests paying every other player in the game
    @Test
    void payEveryPlayer() {
        Player player1 = new Player(
            null, 0, 1500, false, false,
            FXCollections.observableArrayList(), 0
        );

        Player player2 = new Player(
            null, 0, 1500, false, false,
            FXCollections.observableArrayList(), 1
        );

        Player player3 = new Player(
            null, 0, 1500, false, false,
            FXCollections.observableArrayList(), 2
        );

        ArrayList<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        players.add(player3);

        PayMoneyCard card = new PayMoneyCard(
            CardType.CHANCE, "Pay every player 50", 50, true
        );

        card.execute(player1, players);
        assertEquals(1400, player1.getMoney().get());
        assertEquals(1550, player2.getMoney().get());
        assertEquals(1550, player3.getMoney().get());
    }

    // Tests that bankrupt players do not receive money
    @Test
    void bankruptPlayersDoNotReceiveMoney() {
        Player player1 = new Player(
            null, 0, 1500, false, false,
            FXCollections.observableArrayList(), 0
        );

        Player player2 = new Player(
            null, 0, 1500, true, false,
            FXCollections.observableArrayList(), 1
        );

        Player player3 = new Player(
            null, 0, 1500, false, false,
            FXCollections.observableArrayList(), 2
        );

        ArrayList<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        players.add(player3);

        PayMoneyCard card = new PayMoneyCard(
            CardType.CHANCE, "Pay every player 100", 100, true
        );

        card.execute(player1, players);
        assertEquals(1400, player1.getMoney().get());
        assertEquals(1500, player2.getMoney().get());
        assertEquals(1600, player3.getMoney().get());
    }

    // Tests paying every player when there are no other players
    @Test
    void payEveryPlayerWithNoOtherPlayers() {
        Player player = new Player(
            null, 0, 1500, false, false,
            FXCollections.observableArrayList(), 0
        );

        ArrayList<Player> players = new ArrayList<>();
        players.add(player);

        PayMoneyCard card = new PayMoneyCard(
            CardType.CHANCE, "Pay every player 100", 100, true
        );

        card.execute(player, players);
        assertEquals(1500, player.getMoney().get());
    }
}
