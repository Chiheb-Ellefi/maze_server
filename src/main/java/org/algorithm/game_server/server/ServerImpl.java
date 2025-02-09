package org.algorithm.game_server.server;

import org.algorithm.game_server.components.ClientHandler;
import org.algorithm.game_server.components.GameHandler;
import org.algorithm.game_server.utils.Utilities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ServerImpl implements Server {
    private ServerSocket server;
    private final int port;
    private Queue<ClientHandler> queue;
    private int nbRow;
    private int nbCol;
    private final Logger logger;
    private ExecutorService executorService; // Thread pool for handling clients

    public ServerImpl(int port) {
        this.port = port;
        this.queue = new ConcurrentLinkedQueue<>();
        this.nbRow = 5;
        this.nbCol = 5;
        this.logger = Logger.getLogger(ServerImpl.class.getName());
        this.executorService = Executors.newCachedThreadPool(); // Create a thread pool
    }

    public void start() throws IOException {
        System.out.println("Starting server on port " + port);
        server = new ServerSocket(port);
        acceptConnections();
    }

    void acceptConnections() throws IOException {
        while (!server.isClosed()) {
            Socket clientSocket = server.accept();
            System.out.println("Connection established...");
            ClientHandler client = new ClientHandler(clientSocket);
            queue.add(client);
            if (queue.size() >= 2) {
                handleGame();
            }
        }
    }

    public void stop() throws IOException {
        executorService.shutdown();
        server.close();
    }

    @Override
    public String createSession(String firstIp, String secondIp) {
        ClientHandler firstClient = queue.poll();
        ClientHandler secondClient = queue.poll();
        assert firstClient != null;
        assert secondClient != null;
        return Utilities.createSessionID(firstIp, secondIp);
    }

    // In ServerImpl.java
    public void handleGame() throws IOException {
        ClientHandler firstClient = queue.poll();
        ClientHandler secondClient = queue.poll();
        if (firstClient == null || secondClient == null) {
            logger.warning("Not enough clients to start a game session.");
            return;
        }

        GameHandler gameHandler = new GameHandler(nbCol, nbRow);
        firstClient.setGameHandler(gameHandler);
        secondClient.setGameHandler(gameHandler);
        firstClient.setClientId(0);
        secondClient.setClientId(1);
        String sessionId = createSession(
                firstClient.getClientSocket().getRemoteSocketAddress().toString(),
                secondClient.getClientSocket().getRemoteSocketAddress().toString()
        );
        logger.info("Session created: " + sessionId);

        executorService.submit(firstClient);
        executorService.submit(secondClient);

        // Wait for both clients to initialize before starting the game
        Thread initializationChecker = new Thread(() -> {
            while (!firstClient.isClientsInitialized() || !secondClient.isClientsInitialized()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            try {
                logger.info("Both clients initialized, starting game");
                gameHandler.startTurn();
            } catch (InterruptedException e) {
                logger.warning("Error starting game: " + e.getMessage());
            }
        });
        initializationChecker.start();
    }
    public static void main(String[] args) {
        int port = 5000;
        ServerImpl server = new ServerImpl(port);
        try {
            server.start();
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {
            try {
                server.stop();
            } catch (IOException e) {
                System.out.println("Error while stopping server: " + e.getMessage());
            }
        }
    }
}