package org.algorithm.game_server.components;

import org.algorithm.components.Node;
import org.algorithm.game_server.server.ServerImpl;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
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
    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.logger = Logger.getLogger(ServerImpl.class.getName());
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getClientId() {
        return clientId;
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

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }
    @Override
    public void run() {
        try {
            Thread messageProcessor = new Thread(this::processMessages);
            messageProcessor.start();
            String inputLine;
            while (!clientSocket.isClosed() && (inputLine = in.readLine()) != null) {
                switch (inputLine) {
                    case "row":
                        out.println(gameHandler.getNbRow());
                        break;
                    case "column":
                        out.println(gameHandler.getNbCol());
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
    }
    private void handleNodeData(String nodeData) {
        if (nodeData != null && nodeData.matches("\\(\\d+,\\d+\\)")) {
            String[] parts = nodeData.substring(1, nodeData.length() - 1).split(",");
            int row = Integer.parseInt(parts[0]);
            int column = Integer.parseInt(parts[1]);
            Node node = new Node(row, column);
            gameHandler.addNodeToPath(node, clientId);
        } else {
            logger.warning("Invalid node format: " + nodeData);
        }
    }
    private void processMessages() {
        BlockingQueue<String> messageQueue = gameHandler.getPlayerMessageQueue(clientId);
        BlockingQueue<Node> nodeQueue = gameHandler.getPlayerNodeQueue(clientId);

        while (running) {  // Changed from while (running && !messageQueue.isEmpty())
            try {
                // Use take() instead of poll() to wait for messages
                String message = messageQueue.take();  // Changed from poll()
                if (message != null) {

                    out.println(message);
                    if (message.equals("node")) {
                        Node node = nodeQueue.take();
                        sendNode(node);
                    }
                }
            } catch (InterruptedException e) {
                logger.warning("Message processing interrupted: " + e.getMessage());
                break;
            }
        }
    }

    public Node receiveNode() {
        try {
            String nodeResponse = in.readLine();
            logger.info("Received node data: " + nodeResponse);
            if (nodeResponse != null && nodeResponse.matches("\\(\\d+,\\d+\\)")) {
                String[] parts = nodeResponse.substring(1, nodeResponse.length() - 1).split(",");
                int row = Integer.parseInt(parts[0]);
                int column = Integer.parseInt(parts[1]);
                return new Node(row, column);
            } else {
                logger.warning("Invalid node format received: " + nodeResponse);
            }
        } catch (IOException e) {
            logger.warning("Error receiving node: " + e.getMessage());
        }
        return null;
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

        out.println("(" + node.getRow() + "," + node.getColumn() + ")");
    }

    public void sendMessage(String msg) {

            out.println(msg);

    }

}