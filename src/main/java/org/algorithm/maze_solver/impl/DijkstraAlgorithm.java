package org.algorithm.maze_solver.impl;

import org.algorithm.components.Node;
import org.algorithm.maze.Maze;
import org.algorithm.maze_solver.MazeSolver;

import java.util.*;

/**
 * Implementation of Dijkstra's algorithm to solve mazes.
 * This class provides methods to find the shortest path in a maze and all possible paths.
 */
public class DijkstraAlgorithm implements MazeSolver {
    /**
     * Inner class representing a cell in the maze with its position and distance.
     * Implements Comparable to be used in priority queue for Dijkstra's algorithm.
     */
    static class Cell implements Comparable<Cell> {
        int row, col, dist;

        /**
         * Constructor for a cell.
         * @param row Row index in the maze
         * @param col Column index in the maze
         * @param dist Distance from the start node
         */
        Cell(int row, int col, int dist) {
            this.row = row;
            this.col = col;
            this.dist = dist;
        }

        /**
         * Compares cells based on their distance for priority queue ordering.
         */
        @Override
        public int compareTo(Cell other) {
            return Integer.compare(this.dist, other.dist);
        }

        /**
         * Checks if two cells are at the same position (regardless of distance).
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cell cell = (Cell) o;
            return row == cell.row && col == cell.col;
        }

        /**
         * Generates hashcode for the cell based on its position.
         */
        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    /**
     * Finds the shortest path between start and end nodes in the maze using Dijkstra's algorithm.
     *
     * @param maze 2D array representing the maze
     * @param nbRow Number of rows in the maze
     * @param nbColumn Number of columns in the maze
     * @param startNode The starting node
     * @param endNode The destination node
     * @return List of nodes representing the shortest path, or null if no path exists
     */
    @Override
    public List<Node> getShortestPath(Node[][] maze, int nbRow, int nbColumn, Node startNode, Node endNode) {
        // Create cell objects for start and end positions
        Cell start = new Cell(startNode.getRow(), startNode.getColumn(), 0);
        Cell end = new Cell(endNode.getRow(), endNode.getColumn(), 0);

        // Initialize distance array with maximum values
        int[][] dist = new int[nbRow][nbColumn];
        initDist(dist);
        dist[start.row][start.col] = 0;

        // Priority queue to store cells ordered by distance
        PriorityQueue<Cell> pq = new PriorityQueue<>();
        pq.add(start);

        // Map to store parent cells for path reconstruction
        Map<Cell, Cell> parentMap = new HashMap<>();

        // Direction arrays for 8-directional movement (diagonal included)
        int[] dRow = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] dCol = {-1, 0, 1, 1, 1, 0, -1, -1};

        // Main Dijkstra algorithm loop
        while (!pq.isEmpty()) {
            Cell current = pq.poll();

            // If we reached the end, reconstruct the path
            if (current.row == end.row && current.col == end.col) {
                return reconstructPath(parentMap, current, maze);
            }

            // Check all 8 possible directions
            for (int i = 0; i < 8; i++) {
                int newRow = current.row + dRow[i];
                int newCol = current.col + dCol[i];

                // Check if the new position is valid and not a wall (#)
                if (newRow >= 0 && newRow < nbRow && newCol >= 0 && newCol < nbColumn && maze[newRow][newCol].getValue()!='#') {
                    // Check if the move is possible considering borders
                    boolean isPossible = possibleMove(dRow[i], dCol[i], maze[current.row][current.col].getBorders(), maze[newRow][newCol].getBorders());
                    if (isPossible) {
                        // Calculate new distance
                        int newDist = current.dist + 1;
                        // If we found a shorter path, update distance and add to queue
                        if (newDist < dist[newRow][newCol]) {
                            dist[newRow][newCol] = newDist;
                            Cell neighbor = new Cell(newRow, newCol, newDist);
                            pq.add(neighbor);
                            parentMap.put(neighbor, current);
                        }
                    }
                }
            }
        }

        // No path found
        return null;
    }

    /**
     * Finds all possible paths between start and end nodes.
     * Repeatedly finds shortest paths and eliminates them from the maze.
     *
     * @param maze 2D array representing the maze
     * @param nbRow Number of rows in the maze
     * @param nbColumn Number of columns in the maze
     * @param start The starting node
     * @param end The destination node
     * @return List of all possible paths between start and end
     */
    @Override
    public List<List<Node>> getAllPaths(Node[][] maze, int nbRow, int nbColumn, Node start, Node end) {
        List<List<Node>> paths = new ArrayList<>();

        // Create a deep copy of the maze (not used in the method)
        Node[][] mazeCopy = deepCopyMaze(maze, nbRow, nbColumn);

        // Find the shortest path
        List<Node> shortestPath = getShortestPath(maze, nbRow, nbColumn, start, end);

        // Keep finding paths until no more paths exist
        while (shortestPath != null) {
            paths.add(shortestPath);
            // Block the current path by marking intermediate nodes as walls
            eliminatePath(shortestPath, maze);
            // Find the next shortest path
            shortestPath = getShortestPath(maze, nbRow, nbColumn, start, end);
        }
        return paths;
    }

    //Updates the score based on words found in the current path.

    @Override
    public int updateScore(List<Node> currentPathNodes, Maze mazeGenerator, Set<String> foundWords, int score) {
        Set<String> newWords = new HashSet<>();
        List<Node> nodesToReplace = new ArrayList<>();
        int pathLength = currentPathNodes.size();

        // Check all possible substrings in the path to find valid words
        for (int start = 0; start < pathLength; start++) {
            StringBuilder wordBuilder = new StringBuilder();
            for (int end = start; end < pathLength; end++) {
                wordBuilder.append(currentPathNodes.get(end).getValue());
                String word = wordBuilder.toString().toLowerCase();

                // If the word is valid and hasn't been found yet, add it
                if (mazeGenerator.containsWord(word) && !foundWords.contains(word)) {
                    newWords.add(word);

                    // Add nodes to the replacement list (not used in the method)
                    for (int i = start; i <= end; i++) {
                        nodesToReplace.add(currentPathNodes.get(i));
                    }
                }
            }
        }

        // Update score and print found words
        for (String word : newWords) {
            int points = word.length();
            score += points;
            foundWords.add(word);
            System.out.println("Mot trouvé: \"" + word + "\" (" + points + " points)");
        }

        // Print total points gained in this turn
        if (!newWords.isEmpty()) {
            System.out.println("Total gagné ce tour: " + newWords.stream().mapToInt(String::length).sum() + " points");
        }

        return score;
    }

    // Creates a deep copy of the maze.

    private Node[][] deepCopyMaze(Node[][] original, int nbRow, int nbColumn) {
        Node[][] copy = new Node[nbRow][nbColumn];
        for (int i = 0; i < nbRow; i++) {
            for (int j = 0; j < nbColumn; j++) {
                Node originalNode = original[i][j];
                copy[i][j] = new Node(originalNode.getRow(), originalNode.getColumn());
                copy[i][j].setValue(originalNode.getValue());
            }
        }
        return copy;
    }

    //Marks the intermediate nodes in a path as walls (#) to eliminate this path.

    private void eliminatePath(List<Node> path, Node[][] maze) {
        for (int i = 0; i < path.size(); i++) {
            Node node = path.get(i);
            if(i!=0 && i!=path.size()-1){
                maze[node.getRow()][node.getColumn()].setValue('#');
            }
        }
    }

    // Initializes distance array with maximum values.

    void initDist(int[][] dist) {
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
    }

    //Determines if a move is possible considering the borders of current and neighbor cells.
    boolean possibleMove(int dRow, int dCol, boolean[] cBorders, boolean[] nBorders) {
        return switch (dRow) {
            case -1 -> {
                yield switch (dCol) {
                    case -1 -> (!cBorders[0] && !cBorders[3]) && (!nBorders[1] && !nBorders[2]); // Northwest
                    case 0 -> !cBorders[0] && !nBorders[2]; // North
                    case 1 -> (!cBorders[0] && !cBorders[1]) && (!nBorders[2] && !nBorders[3]); // Northeast
                    default -> false;
                };
            }
            case 1 -> {
                yield switch (dCol) {
                    case -1 -> (!cBorders[2] && !cBorders[3]) && (!nBorders[1] && !nBorders[0]); // Southwest
                    case 0 -> !cBorders[2] && !nBorders[0]; // South
                    case 1 -> (!cBorders[1] && !cBorders[2]) && (!nBorders[0] && !nBorders[3]); // Southeast
                    default -> false;
                };
            }
            case 0 -> {
                yield switch (dCol) {
                    case -1 -> !cBorders[3] && !nBorders[1]; // West
                    case 0 -> true; // Same cell
                    case 1 -> !cBorders[1] && !nBorders[3]; // East
                    default -> false;
                };
            }
            default -> false;
        };
    }

    // Reconstructs the path from start to end using the parent map.
    private List<Node> reconstructPath(Map<Cell, Cell> parentMap, Cell endCell, Node[][] maze) {
        List<Node> path = new ArrayList<>();
        Cell current = endCell;

        // Trace back from end to start
        while (current != null) {
            path.add(maze[current.row][current.col]);
            current = parentMap.get(current);
        }

        // Reverse to get path from start to end
        Collections.reverse(path);
        return path;
    }
}