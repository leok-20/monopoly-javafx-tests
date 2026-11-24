package cz.cvut.fel.pvj.nejedly.monopoly.model;

import cz.cvut.fel.pvj.nejedly.monopoly.controller.GameController;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class GameControllerFxTest {

    // Starting a game changes scenes, and ending a turn enables rolling again.
    @Test
    void startsGameAndEnablesNextTurn() {
        FxTestSupport.runOnFxThread(() -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(new VBox()));
            try {
                Scene menuScene = stage.getScene();
                GameController controller = new GameController();
                controller.startNewGameButtonPressed(menuScene, 2);

                Scene gameScene = stage.getScene();
                assertNotSame(menuScene, gameScene);
                VBox controls = (VBox) ((BorderPane) gameScene.getRoot()).getRight();
                Button rollButton = (Button) controls.getChildren().get(2);
                rollButton.setDisable(true);

                controller.endTurnButtonPressed(gameScene);

                assertFalse(rollButton.isDisabled());
            } finally {
                stage.close();
            }
        });
    }
}
