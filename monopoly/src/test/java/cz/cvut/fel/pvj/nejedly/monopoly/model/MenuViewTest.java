package cz.cvut.fel.pvj.nejedly.monopoly.model;

import cz.cvut.fel.pvj.nejedly.monopoly.controller.GameController;
import cz.cvut.fel.pvj.nejedly.monopoly.view.MenuView;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuViewTest {

    // Menu shows all player choices and sends selected actions.
    @Test
    void initMenuForButtonActions() {
        FxTestSupport.runOnFxThread(() -> {
            RecordingController controller = new RecordingController();
            MenuView menuView = new MenuView(controller);
            menuView.init();

            VBox root = (VBox) menuView.getScene().getRoot();
            assertEquals(7, root.getChildren().size());

            List<Button> newGameButtons = new ArrayList<>();
            for (int groupIndex : new int[]{2, 3}) {
                HBox group = (HBox) root.getChildren().get(groupIndex);
                for (javafx.scene.Node node : group.getChildren()) {
                    newGameButtons.add((Button) node);
                }
            }
            assertEquals(List.of("2 PLAYERS", "3 PLAYERS", "4 PLAYERS", "5 PLAYERS", "6 PLAYERS"),
                newGameButtons.stream().map(Button::getText).toList());

            for (Button button : newGameButtons) {
                button.fire();
            }
            assertEquals(List.of(2, 3, 4, 5, 6), controller.startedGames);

            Button loadButton = (Button) root.getChildren().get(5);
            loadButton.fire();
            assertSame(menuView.getScene(), controller.loadedScene);

            Stage stage = new Stage();
            stage.setScene(menuView.getScene());
            stage.show();
            assertTrue(stage.isShowing());
            ((Button) root.getChildren().get(6)).fire();
            assertFalse(stage.isShowing());
        });
    }

    private static class RecordingController extends GameController {
        private final List<Integer> startedGames = new ArrayList<>();
        private Scene loadedScene;

        @Override
        public void startNewGameButtonPressed(Scene oldScene, int numberOfPlayers) {
            startedGames.add(numberOfPlayers);
        }

        @Override
        public void loadGameButtonPressed(Scene oldScene) {
            loadedScene = oldScene;
        }
    }
}
