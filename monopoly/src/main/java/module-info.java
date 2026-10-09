module cz.cvut.fel.pvj.nejedly.monopoly {
    requires javafx.controls;
    requires json.simple;
    requires java.logging;

    exports cz.cvut.fel.pvj.nejedly.monopoly;

    opens cz.cvut.fel.pvj.nejedly.monopoly.model to org.junit.platform.commons;

    opens images;
    opens sprites;
    opens stylesheets;
}