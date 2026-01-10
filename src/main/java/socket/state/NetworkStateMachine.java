package socket.state;

import socket.protocol.Message;
import socket.protocol.MessageType;

public class NetworkStateMachine {

    private GameState state;

    public NetworkStateMachine(boolean isServer) {
        // Startzustand hängt von der Rolle ab
        this.state = isServer
                ? GameState.WAIT_FOR_DONE
                : GameState.WAIT_FOR_SIZE;
    }

    public GameState getState() {
        return state;
    }

    /**
     * Wird aufgerufen, wenn eine Nachricht empfangen wird.
     * Prüft, ob sie im aktuellen Zustand erlaubt ist
     * und wechselt ggf. den Zustand.
     */
    public void onMessageReceived(Message msg) {

        switch (state) {

            case WAIT_FOR_SIZE -> {
                if (msg.getType() == MessageType.SIZE) {
                    state = GameState.WAIT_FOR_READY;
                }
            }

            case WAIT_FOR_DONE -> {
                if (msg.getType() == MessageType.DONE) {
                    state = GameState.WAIT_FOR_READY;
                }
            }

            case WAIT_FOR_READY -> {
                if (msg.getType() == MessageType.READY) {
                    state = GameState.MY_TURN;
                }
            }

            case OPPONENT_TURN -> {
                if (msg.getType() == MessageType.SHOT) {
                    state = GameState.MY_TURN;
                }
            }

            case WAIT_FOR_ANSWER -> {
                if (msg.getType() == MessageType.ANSWER) {
                    state = GameState.OPPONENT_TURN;
                }
            }

            default -> {
                // GAME_OVER oder INIT → nichts tun
            }
        }
    }

    /**
     * Prüft, ob eine Nachricht gesendet werden darf
     */
    public boolean canSend(MessageType type) {

        return switch (state) {

            case MY_TURN -> type == MessageType.SHOT;

            case WAIT_FOR_READY -> type == MessageType.READY;

            case WAIT_FOR_DONE -> type == MessageType.DONE;

            default -> false;
        };
    }

    /**
     * Muss nach dem Senden aufgerufen werden,
     * um den Zustand korrekt zu wechseln
     */
    public void onMessageSent(MessageType type) {

        if (state == GameState.MY_TURN && type == MessageType.SHOT) {
            state = GameState.WAIT_FOR_ANSWER;
        }
    }
}

