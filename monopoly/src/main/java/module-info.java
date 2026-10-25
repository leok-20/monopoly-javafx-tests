module cz.cvut.fel.pvj.nejedly.monopoly {
    requires javafx.controls;
    requires json.simple;
    requires java.logging;
    requires org.junit.jupiter.api;

    exports cz.cvut.fel.pvj.nejedly.monopoly;

    opens images;
    opens sprites;
    opens stylesheets;
}