package org.algorithm.game_server.components;

import org.algorithm.components.Node;
import org.algorithm.game_server.server.ServerImpl;
import org.algorithm.maze.Maze;
import org.algorithm.maze.impl.dfs_algorithm.DfsAlgorithm;
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
   public static String theme;

    private static final int GAME_DURATION_SECONDS = 20000;
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
        this.mazeGen.createLoops();
        this.logger = Logger.getLogger(ServerImpl.class.getName());
        this.firstPath=new Stack<>();
        this.secondPath=new Stack<>();
        this.firstPath.push(mazeGen.getMaze()[mazeGen.getStart().getRow()][mazeGen.getStart().getColumn()]);
        this.secondPath.push(mazeGen.getMaze()[mazeGen.getStart().getRow()][mazeGen.getStart().getColumn()]);
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


    public String getTheme() {
            return theme;
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

    public void sendMaze(Socket socket, byte[] mazeData) throws IOException {
        String base64Data = Base64.getEncoder().encodeToString(mazeData);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(base64Data);
    }



    public synchronized void startTurn() throws InterruptedException {
        if (turnTimer != null) {
            turnTimer.cancel();
        }
        turnTimer = new Timer();
        turnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    endTurn();
                } catch (IOException e) {
                    logger.warning("Error ending turn: " + e.getMessage());
                }
            }
        }, GAME_DURATION_SECONDS);

        if (currentPlayerId == 0) {
            firstPlayerMessages.put("turn");
            secondPlayerMessages.put("not");
        } else {
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


    public synchronized void addNodeToPath(Node node, int playerId) throws InterruptedException {
        Stack<Node> currentPath = (playerId == 0) ? firstPath : secondPath;
        Node previousNode = currentPath.peek();
        if (previousNode.getRow() == node.getRow() && previousNode.getColumn() == node.getColumn()) {
            currentPath.pop();
        } else {
            currentPath.push(node);
        }

        if(playerId == 0) {
            firstPlayerScore = mazeSolver.updateScore(firstPath.stream().toList(), mazeGen, firstFoundWords, firstPlayerScore);
            sendScores(firstPlayerMessages,secondPlayerMessages,firstPlayerScore);
        } else if (playerId == 1) {
            secondPlayerScore = mazeSolver.updateScore(secondPath.stream().toList(), mazeGen, secondFoundWords, secondPlayerScore);
            sendScores(secondPlayerMessages,firstPlayerMessages,secondPlayerScore);
        }
        logger.info("Player " + playerId + " moved to " + node.getRow() + "," + node.getColumn());
        logger.info("Player 0 score: " + firstPlayerScore);
        logger.info("Player 1 score: " + secondPlayerScore);
        try {
            broadcastNode(playerId, node);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (node.getRow() == mazeGen.getEnd().getRow() &&
                node.getColumn() == mazeGen.getEnd().getColumn()) {
            handleGameEnd();
        }
    }

    private void handleGameEnd() throws InterruptedException {
        int winningPlayerId;
        setGameOver(true);
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
        List<Node> shortestPath = mazeSolver.getShortestPath(mazeGen.getMaze(),nbRow,nbCol,mazeGen.getStart(),mazeGen.getEnd());
        List<Node> firstPlayerPath=firstPath.stream().toList();
        List<Node> secondPlayerPath=secondPath.stream().toList();
        boolean firstReachedLastNode=firstPath.peek().getRow()==mazeGen.getEnd().getRow() && firstPath.peek().getColumn()==mazeGen.getEnd().getColumn();
        boolean secondReachedLastNode=secondPath.peek().getRow()==mazeGen.getEnd().getRow() && secondPath.peek().getColumn()==mazeGen.getEnd().getColumn();
            if(firstReachedLastNode ){
                firstPlayerScore+=5;
                if(shortestPath.size()==firstPlayerPath.size()){
                    firstPlayerScore+=10;
                }
                sendScores(firstPlayerMessages,secondPlayerMessages,firstPlayerScore);
            }
            if(secondReachedLastNode){
                secondPlayerScore+=5;
                if(shortestPath.size()==secondPlayerPath.size()){
                    secondPlayerScore+=10;

                }
                sendScores(secondPlayerMessages,firstPlayerMessages,secondPlayerScore);
            }
            winningPlayerId=firstPlayerScore>secondPlayerScore?0:1;
            logger.info("Player " + winningPlayerId + " won with score : "+ (Math.max(firstPlayerScore, secondPlayerScore)));
        try {
           firstPlayerMessages.put("gameOver");
           secondPlayerMessages.put("gameOver");
           logger.info("Game Over: " + gameOver);
        } catch (InterruptedException e) {
            logger.warning("Error sending game end messages: " + e.getMessage());
        }
    }


    synchronized  void sendScores(BlockingQueue<String> player,BlockingQueue<String> opponent,int playerScore) throws InterruptedException {
        player.put("score");
        player.put(String.valueOf(playerScore));
        opponent.put("otherScore");
        opponent.put(String.valueOf(playerScore));
    }


}