package hotel.gui.controllers;

import hotel.network.ChatClient;
import hotel.network.ChatServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatController {

    @FXML private TextArea chatArea;
    @FXML private TextField inputField;

    private boolean isServer;
    private String username;

    public void initializeChat(boolean isServer, String username) {
        this.isServer = isServer;
        this.username = username;

        if (isServer) {
            ChatServer.getInstance().setOnMessageReceived(this::appendMessage);
            chatArea.appendText("System: Chat Server running. Waiting for guests...\n");
        } else {
            ChatClient.getInstance().setOnMessageReceived(this::appendMessage);
            chatArea.appendText("System: Connected to Chat Server. Say hi!\n");
        }
    }

    @FXML
    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (msg.isEmpty()) return;

        if (isServer) {
            ChatServer.getInstance().broadcast(username + " (Receptionist): " + msg);
        } else {
            ChatClient.getInstance().sendMessage(username, msg);
        }
        inputField.clear();
    }

    private void appendMessage(String message) {
        Platform.runLater(() -> {
            chatArea.appendText(message + "\n");
        });
    }
}
