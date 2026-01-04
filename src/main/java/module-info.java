module com.matti.battleship {
  requires javafx.controls;
  requires javafx.fxml;
  requires org.controlsfx.controls;
  requires com.dlsc.formsfx;
  requires org.kordamp.ikonli.javafx;
  requires org.kordamp.bootstrapfx.core;
  requires com.almasb.fxgl.all;
  requires java.desktop;
  requires annotations;
  requires javafx.graphics;
  requires jdk.jshell;
  requires org.apache.logging.log4j;
  requires jdk.dynalink;
  requires org.apache.logging.log4j.core;
  requires java.management;
  requires com.fasterxml.jackson.databind;

  opens com.matti.battleship to
      javafx.fxml;

  exports com.matti.battleship;
}
