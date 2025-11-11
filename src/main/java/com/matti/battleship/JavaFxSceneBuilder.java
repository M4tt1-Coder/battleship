package com.matti.battleship;
// Definiert das Package, in dem diese Klasse liegt. Packages helfen, Klassen zu organisieren und Namenskonflikte zu vermeiden.

import javafx.application.Application;
// Importiert die Application-Klasse von JavaFX, die die Grundlage für jede JavaFX-Anwendung bildet.

import javafx.event.ActionEvent;
// Importiert die Klasse für ActionEvents, z.B. wenn ein Button geklickt wird.

import javafx.event.EventHandler;
// Importiert die Schnittstelle, um Event-Handler zu definieren (z.B. Reaktionen auf Button-Klicks).

import javafx.scene.Scene;
// Importiert die Scene-Klasse. Eine Scene ist wie ein Container für alle GUI-Elemente, die angezeigt werden.

import javafx.scene.control.Button;
// Importiert die Button-Klasse, um Buttons in der GUI zu erstellen.

// Importiert den StackPane, einen Layout-Container. (In deinem aktuellen Code wird er nicht verwendet.)

import javafx.scene.layout.VBox;
// Importiert den VBox-Layout-Container, der Kinder-Elemente vertikal anordnet.

import javafx.stage.Stage;
// Importiert die Stage-Klasse. Stage ist das Hauptfenster deiner JavaFX-Anwendung.

public class LamdaExpression4 extends Application {
// Deklariert die Hauptklasse deiner Anwendung. Sie **erbt von Application**, um JavaFX-Funktionalität zu nutzen.

public static void main(String[] args) {
    // Standardmain-Methode, die als Einstiegspunkt für die JVM dient.
    launch(args);
    // Startet die JavaFX-Anwendung. Ruft intern die start(Stage primaryStage)-Methode auf.
}

@Override
public void start(Stage primaryStage) throws Exception {
    // Die zentrale Methode von JavaFX. Wird automatisch aufgerufen, wenn launch() ausgeführt wird.
    // primaryStage ist das Hauptfenster der Anwendung.

    Button btn = new Button("Click me");
    // Erstellt einen Button mit der Beschriftung "Click me".

    Button exit = new Button("Exit");
    // Erstellt einen weiteren Button mit der Beschriftung "Exit".

    exit.setOnAction(e -> {
        // Definiert einen Lambda-Ausdruck, der ausgeführt wird, wenn der Exit-Button geklickt wird.
        System.out.println("exit this App");
        // Gibt "exit this App" in der Konsole aus.
        System.exit(0);
        // Beendet die gesamte Anwendung.
    });

    btn.setOnAction(new EventHandler<ActionEvent>() {
        // Fügt dem "Click me"-Button einen EventHandler hinzu. Hier wird die klassische Methode (nicht Lambda) verwendet.

        @Override
        public void handle(ActionEvent event) {
            // Diese Methode wird aufgerufen, wenn der Button geklickt wird.
            System.out.println("hello world");
            // Gibt "hello world" in der Konsole aus.
        }
    });

    VBox root = new VBox();
    // Erstellt einen VBox-Container. VBox ordnet seine Kinder (Buttons, Labels, etc.) vertikal an.

    root.getChildren().addAll(btn, exit);
    // Fügt beide Buttons zum VBox-Container hinzu. Buttons werden also vertikal angezeigt.

    Scene scene = new Scene(root, 500, 300);
    // Erstellt eine Scene mit dem VBox-Container als Root-Element. Die Größe der Scene ist 500x300 Pixel.

    primaryStage.setTitle("My title");
    // Setzt den Titel des Fensters auf "My title".

    primaryStage.setScene(scene);
    // Verknüpft die Scene mit dem Stage (Fenster). Jetzt weiß das Fenster, was angezeigt werden soll.

    primaryStage.show();
    // Macht das Fenster sichtbar. Ohne diesen Aufruf würde nichts angezeigt werden.
}
}