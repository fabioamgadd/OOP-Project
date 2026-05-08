package hotel.network;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    
    private static ChatClient instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessageReceived;
    private boolean connected = false;

    private ChatClient() {}

    public static synchronized ChatClient getInstance() {
        if (instance == null) instance = new ChatClient();
        return instance;
    }

    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }

    public void connect(String username) {
        if (connected) return;
        
        new Thread(() -> {
            try {
                socket = new Socket(HOST, PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                connected = true;
                
                String message;
                while ((message = in.readLine()) != null) {
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(message);
                    }
                }
            } catch (IOException e) {
                System.err.println("Chat Client could not connect: " + e.getMessage());
                connected = false;
                if (onMessageReceived != null) {
                    onMessageReceived.accept("System: Could not connect to Chat Server. Receptionist may be offline.");
                }
            }
        }).start();
    }

    public void sendMessage(String sender, String msg) {
        if (out != null && connected) {
            out.println(sender + ": " + msg);
        } else {
            if (onMessageReceived != null) {
                onMessageReceived.accept("System: Not connected to chat server.");
            }
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
