package org.algorithm.game_server.components;

import org.algorithm.components.Node;
import org.algorithm.game_server.server.ServerImpl;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final BufferedReader in;
    private final PrintWriter out;
    private GameHandler gameHandler;
    private final Logger logger;
    private int clientId;
    private boolean clientsInitialized = false;
    private boolean running = true;
    private final BlockingQueue<String> outgoingMessages = new LinkedBlockingQueue<>();
    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.logger = Logger.getLogger(ServerImpl.class.getName());
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }


    public boolean isClientsInitialized() {
        return clientsInitialized;
    }

    public void setGameHandler(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }


    @Override
    public void run() {
        try {
            startMessageListener();
            startMessageSender();

            BlockingQueue<String> messageQueue = gameHandler.getPlayerMessageQueue(clientId);
            BlockingQueue<Node> nodeQueue = gameHandler.getPlayerNodeQueue(clientId);

            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    String message = messageQueue.take();
                    outgoingMessages.put(message);
                    if (message.equals("node")) {
                        Node node = nodeQueue.poll();
                        if (node != null) {
                            sendNode(node);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            logger.warning("Connection error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    private void startMessageSender() {
        Thread senderThread = new Thread(() -> {
            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    String message = outgoingMessages.take();
                    try {
                        synchronized (out) {
                            out.println(message);
                            out.flush();
                        }
                        logger.info("Sent message to client " + clientId + ": " + message);
                    } catch (Exception e) {
                        logger.warning("Failed to send message to client " + clientId + ": " + e.getMessage());

                        outgoingMessages.put(message);
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        senderThread.setDaemon(true);
        senderThread.start();
    }

    private void startMessageListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                String inputLine;
                while (!clientSocket.isClosed() && (inputLine = in.readLine()) != null) {
                    switch (inputLine) {
                        case "row":
                            out.println(gameHandler.getNbRow());
                            break;
                        case "column":
                            out.println(gameHandler.getNbCol());
                            break;
                        case "theme":
                            out.println(gameHandler.getTheme());
                            break;
                        case "maze":
                            gameHandler.sendMaze(clientSocket, gameHandler.getMazeGen().serializeMaze());
                            clientsInitialized = true;
                            break;
                        case "start":
                            sendNode(gameHandler.getMazeGen().getStart());
                            break;
                        case "end":
                            sendNode(gameHandler.getMazeGen().getEnd());
                            break;
                        case "heartbeat":
                            break;
                        case "node":
                            String nodeData = in.readLine();
                            handleNodeData(nodeData);
                            break;

                        default:
                            logger.warning("Unknown message received: " + inputLine);
                    }
                }
            } catch (IOException e) {
                logger.warning("Connection error: " + e.getMessage());
            } finally {
                cleanup();
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    private void handleNodeData(String nodeData) {
        if (nodeData != null && nodeData.matches("\\(\\d+,\\d+\\)")) {
            String[] parts = nodeData.substring(1, nodeData.length() - 1).split(",");
            int row = Integer.parseInt(parts[0]);
            int column = Integer.parseInt(parts[1]);
            Node node = gameHandler.getMazeGen().getMaze()[row][column];
            try {
                gameHandler.addNodeToPath(node, clientId);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.warning("Invalid node format: " + nodeData);
        }
    }


    private void cleanup() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.warning("Error closing socket: " + e.getMessage());
        }
    }



    public synchronized void sendNode(Node node) {
        try {
            String nodeMessage = "(" + node.getRow() + "," + node.getColumn() + ")";
            outgoingMessages.put(nodeMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning("Failed to queue node message: " + e.getMessage());
        }
    }



}