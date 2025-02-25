package org.algorithm.maze_solver.impl;

import org.algorithm.components.Node;
import org.algorithm.maze.Maze;
import org.algorithm.maze_solver.MazeSolver;

import java.util.*;

public class BfsAlgorithm implements MazeSolver {
    @Override
    public List<Node> getShortestPath(Node[][] maze, int nbRow, int nbColumn, Node start, Node end) {
        return List.of();
    }

    @Override
    public List<List<Node>> getAllPaths(Node[][] maze, int nbRow, int nbColumn, Node start, Node end) {
        Queue<List<Node>> queue = new LinkedList<>();
        List<List<Node>> paths = new ArrayList<>();

        // Start with the initial node
        queue.add(List.of(maze[start.getRow()][start.getColumn()]));

        // Possible movement directions (8 directions)
        int[] dRow = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] dCol = {-1, 0, 1, 1, 1, 0, -1, -1};

        while (!queue.isEmpty()) {
            List<Node> path = queue.poll();
            Node current = path.get(path.size() - 1);

            // If the end node is reached, add the path to results
            if (current.getRow() == end.getRow() && current.getColumn() == end.getColumn()) {
                paths.add(path);
                continue;
            }

            // Explore all possible movements
            for (int i = 0; i < 8; i++) {
                int newRow = current.getRow() + dRow[i];
                int newCol = current.getColumn() + dCol[i];

                // Check if the new position is within bounds
                if (newRow >= 0 && newRow < nbRow && newCol >= 0 && newCol < nbColumn) {
                    boolean isPossible = possibleMove(dRow[i], dCol[i], maze[current.getRow()][current.getColumn()].getBorders(), maze[newRow][newCol].getBorders());
                    Node newNode = maze[newRow][newCol];

                    // Ensure the move is valid and not already visited
                    if (isPossible && !path.contains(newNode)) {
                        List<Node> newPath = new ArrayList<>(path);
                        newPath.add(newNode);
                        queue.add(newPath);
                    }
                }
            }
        }
        return paths;
    }

    @Override
    public int updateScore(List<Node> currentPathNodes, Maze mazeGenerator, Set<String> foundWords, int score) {
        Set<String> newWords = new HashSet<>();
        List<Node> nodesToReplace = new ArrayList<>();
        int pathLength = currentPathNodes.size();

        // Check for valid words along the path
        for (int start = 0; start < pathLength; start++) {
            StringBuilder wordBuilder = new StringBuilder();
            for (int end = start; end < pathLength; end++) {
                wordBuilder.append(currentPathNodes.get(end).getValue());
                String word = wordBuilder.toString().toLowerCase();

                // Add new words to the set if found in the maze dictionary
                if (mazeGenerator.containsWord(word) && !foundWords.contains(word)) {
                    newWords.add(word);

                    // Mark nodes to be replaced
                    for (int i = start; i <= end; i++) {
                        nodesToReplace.add(currentPathNodes.get(i));
                    }
                }
            }
        }

        // Update score based on word length
        for (String word : newWords) {
            int points = word.length();
            score += points;
            foundWords.add(word);
            System.out.println("Mot trouvé: \"" + word + "\" (" + points + " points)");
        }

        // Log total points earned this round
        if (!newWords.isEmpty()) {
            System.out.println("Total gagné ce tour: " + newWords.stream().mapToInt(String::length).sum() + " points");
        }

        return score;
    }

    // Checks if a move is possible based on the node's borders
    boolean possibleMove(int dRow, int dCol, boolean[] cBorders, boolean[] nBorders) {
        return switch (dRow) {
            case -1 -> switch (dCol) {
                case -1 -> (!cBorders[0] && !cBorders[3]) && (!nBorders[1] && !nBorders[2]);
                case 0 -> !cBorders[0] && !nBorders[2];
                case 1 -> (!cBorders[0] && !cBorders[1]) && (!nBorders[2] && !nBorders[3]);
                default -> false;
            };
            case 1 -> switch (dCol) {
                case -1 -> (!cBorders[2] && !cBorders[3]) && (!nBorders[1] && !nBorders[0]);
                case 0 -> !cBorders[2] && !nBorders[0];
                case 1 -> (!cBorders[1] && !cBorders[2]) && (!nBorders[0] && !nBorders[3]);
                default -> false;
            };
            case 0 -> switch (dCol) {
                case -1 -> !cBorders[3] && !nBorders[1];
                case 0 -> true;
                case 1 -> !cBorders[1] && !nBorders[3];
                default -> false;
            };
            default -> false;
        };
    }
}
