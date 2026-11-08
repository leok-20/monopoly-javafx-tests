package cz.cvut.fel.pvj.nejedly.monopoly.model.decks.cards;

import cz.cvut.fel.pvj.nejedly.monopoly.model.player.Player;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceiveMoneyCardTest {

    // Tests receiving a fixed amount of money
    @Test
    void receiveFixedAmount() {
        Player player = new Player(
            null, 0, 1500, false, false,
            FXCollections.observableArrayList(), 0
        );

        ReceiveMoneyCard card = new ReceiveMoneyCard(
            CardType.CHANCE, "Receive 100", 100
        );

        ArrayList<Player> players = new ArrayList<>();
        players.add(player);

        card.execute(player, players);
        assertEquals(1600, player.getMoney().get());
    }

    // Tests receiving money from every other player
    @Test
    void receiveMoneyFromEveryPlayer() {
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

        ReceiveMoneyCard card = new ReceiveMoneyCard(
            CardType.CHANCE, "Receive 50 from every player", 50, true
        );

        card.execute(player1, players);
        assertEquals(1600, player1.getMoney().get());
        assertEquals(1450, player2.getMoney().get());
        assertEquals(1450, player3.getMoney().get());
    }

    // Tests that bankrupt players do not pay money
    @Test
    void bankruptPlayersDoNotPay() {
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

        ReceiveMoneyCard card = new ReceiveMoneyCard(
            CardType.CHANCE, "Receive 100 from every player", 100, true
        );

        card.execute(player1, players);
        assertEquals(1600, player1.getMoney().get());
        assertEquals(1500, player2.getMoney().get());
        assertEquals(1400, player3.getMoney().get());
    }

    // Tests receiving money when there are no other players
    @Test
    void receiveMoneyWithNoOtherPlayers() {
        Player player = new Player(
            null, 0, 1500, false, false,
            FXCollections.observableArrayList(), 0
        );

        ArrayList<Player> players = new ArrayList<>();
        players.add(player);

        ReceiveMoneyCard card = new ReceiveMoneyCard(
            CardType.CHANCE, "Receive 100 from every player", 100, true
        );

        card.execute(player, players);
        assertEquals(1500, player.getMoney().get());
    }
}
