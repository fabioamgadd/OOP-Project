package hotel.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ChatServer {

    private static final int PORT = 8080;
    private static ChatServer instance;
    private ServerSocket serverSocket;
    private boolean running = false;
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private Consumer<String> onMessageReceived;

    private ChatServer() {}

    public static synchronized ChatServer getInstance() {
        if (instance == null) instance = new ChatServer();
        return instance;
    }

    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }

    public void start() {
        if (running) return;
        
        threadPool.execute(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                running = true;
                System.out.println("Chat Server started on port " + PORT);
                
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket);
                    clients.add(handler);
                    threadPool.execute(handler);
                }
            } catch (IOException e) {
                System.err.println("Chat Server could not start (port might be in use): " + e.getMessage());
            }
        });
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
        if (onMessageReceived != null) {
            onMessageReceived.accept(message);
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        threadPool.shutdown();
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(message);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            } finally {
                clients.remove(this);
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String msg) {
            if (out != null) {
                out.println(msg);
            }
        }
    }
}
