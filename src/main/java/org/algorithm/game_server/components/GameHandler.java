package org.algorithm.game_server.components;

import org.algorithm.components.Node;
import org.algorithm.game_server.server.ServerImpl;
import org.algorithm.maze.Maze;
import org.algorithm.maze.impl.dfs_algorithm.DfsAlgorithm;
import org.algorithm.maze.impl.prims_algorithm.PrimsAlgorithm;
import org.algorithm.maze_solver.MazeSolver;
import org.algorithm.maze_solver.impl.DijkstraAlgorithm;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class GameHandler {
    private final Maze mazeGen;
    private final  int nbRow;
    private final  int nbCol;
    private int currentPlayerId = 1;
    private final Logger logger;
    Stack<Node> firstPath;
    Stack<Node> secondPath;
    private boolean gameOver = false;
    private Timer turnTimer;
    MazeSolver mazeSolver;
    int firstPlayerScore;
    Set<String> firstFoundWords;
    Set<String> secondFoundWords;
    int secondPlayerScore;
    private static final int GAME_DURATION_SECONDS = 15000;
    private final BlockingQueue<String> firstPlayerMessages = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> secondPlayerMessages = new LinkedBlockingQueue<>();
    private final BlockingQueue<Node> firstPlayerNodes = new LinkedBlockingQueue<>();
    private final BlockingQueue<Node> secondPlayerNodes = new LinkedBlockingQueue<>();
    public GameHandler(int nbCol,int nbRow) {
        this.nbRow=nbRow;
        this.nbCol=nbCol;
        this.mazeGen=new DfsAlgorithm(nbRow,nbCol);
        this.mazeGen.setStartAndEnd();
        this.mazeGen.generateMaze();
        this.logger = Logger.getLogger(ServerImpl.class.getName());
        this.firstPath=new Stack<>();
        this.secondPath=new Stack<>();
        this.firstPath.push(mazeGen.getStart());
        this.secondPath.push(mazeGen.getStart());
        this.mazeSolver=new DijkstraAlgorithm();
        this.firstPlayerScore=0;
        this.secondPlayerScore=0;
        this.firstFoundWords=new HashSet<>();
        this.secondFoundWords=new HashSet<>();
    }
    public BlockingQueue<String> getPlayerMessageQueue(int playerId) {
        return playerId == 0 ? firstPlayerMessages : secondPlayerMessages;
    }

    public BlockingQueue<Node> getPlayerNodeQueue(int playerId) {
        return playerId == 0 ? firstPlayerNodes : secondPlayerNodes;
    }




    public Maze getMazeGen() {
        return mazeGen;
    }

    public int getNbRow() {
        return nbRow;
    }

    public int getNbCol() {
        return nbCol;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public synchronized void sendMaze(Socket socket, byte[] mazeBytes) throws IOException {
        try (OutputStream os = socket.getOutputStream()) {
            os.write(intToByteArray(mazeBytes.length));
            os.write(mazeBytes);
            os.flush();
        }
    }
    private byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }
    // Update startTurn() in GameHandler to include more logging
    public synchronized void startTurn() throws InterruptedException {
        if (turnTimer != null) {
            turnTimer.cancel();
        }

        logger.info("Starting turn for Player " + currentPlayerId);
      /*  logGameState();  // Add state logging*/

        turnTimer = new Timer();
        turnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    logger.info("Turn timer expired for Player " + currentPlayerId);
                    endTurn();
                } catch (IOException e) {
                    logger.warning("Error ending turn: " + e.getMessage());
                }
            }
        }, GAME_DURATION_SECONDS);
        // Queue turn notifications
        if (currentPlayerId == 0) {
            logger.info("Player 0 turn ");
            firstPlayerMessages.put("turn");
            secondPlayerMessages.put("not");
        } else {
            logger.info("Player 1 turn ");
            secondPlayerMessages.put("turn");
            firstPlayerMessages.put("not");
        }
    }

    public synchronized void endTurn() throws IOException {
        if (isGameOver()) {
            if (turnTimer != null) {
                turnTimer.cancel();
                turnTimer = null;
            }
            return;
        }

        currentPlayerId = 1-currentPlayerId;
        logger.info("Player " + currentPlayerId + " turn");
        try {
            startTurn();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public synchronized void broadcastNode(int excludedClientId, Node node) throws InterruptedException {
        if (excludedClientId == 0) {
            secondPlayerNodes.put(node);
            secondPlayerMessages.put("node");
        } else {
            firstPlayerNodes.put(node);
            firstPlayerMessages.put("node");
        }
    }


    public synchronized void addNodeToPath(Node node, int playerId) {
        logger.info("Received node from Player " + playerId + ": (" + node.getRow() + "," + node.getColumn() + ")");


        Stack<Node> currentPath = (playerId == 0) ? firstPath : secondPath;
        Node previousNode = currentPath.peek();
        if (previousNode.getRow() == node.getRow() && previousNode.getColumn() == node.getColumn()) {
            currentPath.pop();
        } else {
            currentPath.push(node);
        }

        if(playerId == 0) {
            firstPlayerScore = mazeSolver.updateScore(firstPath.stream().toList(), mazeGen, firstFoundWords, firstPlayerScore);
        } else if (playerId == 1) {
            secondPlayerScore = mazeSolver.updateScore(secondPath.stream().toList(), mazeGen, secondFoundWords, secondPlayerScore);
        }
        logger.info("Player 0 score: " + firstPlayerScore);
        logger.info("Player 1 score: " + secondPlayerScore);

        logger.info("Player " + playerId + " moved to " + node.getRow() + "," + node.getColumn());
        try {
            logger.info("Broadcasting node " + node.getRow() + "," + node.getColumn());
            broadcastNode(1-playerId, node);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (node.getRow() == mazeGen.getEnd().getRow() &&
                node.getColumn() == mazeGen.getEnd().getColumn()) {
            handleGameEnd(playerId);
        } else {
            try {
                endTurn();
            } catch (IOException e) {
                logger.warning("Error ending turn after move: " + e.getMessage());
            }
        }

        /*// Log state again after all updates are complete
        logger.info("After processing node:");
        logGameState();*/
    }

    /*public synchronized void logGameState() {
        logger.info("=== Game State ===");
        logger.info("First path size: " + firstPath.size());
        logger.info("Second path size: " + secondPath.size());
        logger.info("================");
    }
*/


    private void handleGameEnd(int winningPlayerId) {
        setGameOver(true);
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
        logger.info("Game Over: " + gameOver);
        List<Node> shortestPath = mazeSolver.getShortestPath(mazeGen.getMaze(),nbRow,nbCol,mazeGen.getStart(),mazeGen.getEnd());
        List<Node> firstPlayerPath=firstPath.stream().toList();
        List<Node> secondPlayerPath=secondPath.stream().toList();
        if(firstPlayerPath.size()!=secondPlayerPath.size()){
            if(shortestPath.size()==firstPlayerPath.size()){
                winningPlayerId=0;
                firstPlayerScore+=100;
            }else if(shortestPath.size()==secondPlayerPath.size()){
                winningPlayerId=1;
                secondPlayerScore+=100;
            }
            logger.info("Player " + winningPlayerId + " won with score : "+ (winningPlayerId==0?firstPlayerScore:secondPlayerScore));
        }else{
            logger.info("Player " + winningPlayerId + " won with score : "+ (Math.max(firstPlayerScore, secondPlayerScore)));
        }

        try {
            if (winningPlayerId == 0) {
                firstPlayerMessages.put("win");
                secondPlayerMessages.put("lose");
            } else {
                secondPlayerMessages.put("win");
                firstPlayerMessages.put("lose");
            }
        } catch (InterruptedException e) {
            logger.warning("Error sending game end messages: " + e.getMessage());
        }
    }



}