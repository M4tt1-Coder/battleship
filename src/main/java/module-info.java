module com.matti.battleship {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.base;
    //requires java.desktop;

    opens com.matti.battleship to javafx.fxml;
    exports com.matti.battleship;
}