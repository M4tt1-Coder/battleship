module com.matti.battleship {
  requires javafx.controls;
  requires java.desktop;
  requires javafx.graphics;
  requires jdk.jshell;
  requires org.apache.logging.log4j;
  requires jdk.dynalink;
  requires org.apache.logging.log4j.core;
  requires java.management;
  requires java.xml.crypto;
  requires annotations;
  requires jdk.sctp;
  requires jdk.security.auth;
  requires org.checkerframework.checker.qual;
  requires com.google.common;
  requires jdk.jfr;

  exports com.matti.battleship;
  exports com.matti.battleship.IO;
  exports com.matti.battleship.enums;
  exports com.matti.battleship.types;
  exports com.matti.battleship.utils;
  exports com.matti.battleship.socket;
  exports com.matti.battleship.computer;
}
