package org.algorithm.maze_solver.impl;

import org.algorithm.components.Node;
import org.algorithm.maze.Maze;
import org.algorithm.maze_solver.MazeSolver;

import java.util.*;

public class DijkstraAlgorithm implements MazeSolver {
    static class Cell implements Comparable<Cell> {
        int row, col, dist;

        Cell(int row, int col, int dist) {
            this.row = row;
            this.col = col;
            this.dist = dist;
        }

        @Override
        public int compareTo(Cell other) {
            return Integer.compare(this.dist, other.dist);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cell cell = (Cell) o;
            return row == cell.row && col == cell.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    @Override
    public List<Node> getShortestPath(Node[][] maze, int nbRow, int nbColumn, Node startNode, Node endNode) {
        Cell start = new Cell(startNode.getRow(), startNode.getColumn(), 0);
        Cell end = new Cell(endNode.getRow(), endNode.getColumn(), 0);

        // Initialize distance array
        int[][] dist = new int[nbRow][nbColumn];
        initDist(dist);
        dist[start.row][start.col] = 0;

        // Priority queue for Dijkstra's algorithm
        PriorityQueue<Cell> pq = new PriorityQueue<>();
        pq.add(start);

        // Map to store the parent of each cell for path reconstruction
        Map<Cell, Cell> parentMap = new HashMap<>();

        // Directions for 8 possible movements
        int[] dRow = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] dCol = {-1, 0, 1, 1, 1, 0, -1, -1};

        while (!pq.isEmpty()) {
            Cell current = pq.poll();

            // If the current cell is the end cell, reconstruct the path
            if (current.row == end.row && current.col == end.col) {
                return reconstructPath(parentMap, current, maze);
            }

            // Process neighbors in all 8 directions
            for (int i = 0; i < 8; i++) {
                int newRow = current.row + dRow[i];
                int newCol = current.col + dCol[i];

                // Check if the new cell is within bounds
                if (newRow >= 0 && newRow < nbRow && newCol >= 0 && newCol < nbColumn && maze[newRow][newCol].getValue()!='#') {
                    boolean isPossible = possibleMove(dRow[i], dCol[i], maze[current.row][current.col].getBorders(), maze[newRow][newCol].getBorders());
                    if (isPossible) {
                        int newDist = current.dist + 1;

                        // If a shorter path is found, update the distance and add to the queue
                        if (newDist < dist[newRow][newCol]) {
                            dist[newRow][newCol] = newDist;
                            Cell neighbor = new Cell(newRow, newCol, newDist);
                            pq.add(neighbor);
                            parentMap.put(neighbor, current); // Track the parent
                        }
                    }
                }
            }
        }


        return null;
    }

    @Override
    public List<List<Node>> getAllPaths(Node[][] maze, int nbRow, int nbColumn, Node start, Node end) {
        List<List<Node>> paths = new ArrayList<>();

        Node[][] mazeCopy = deepCopyMaze(maze, nbRow, nbColumn);

        List<Node> shortestPath = getShortestPath(maze, nbRow, nbColumn, start, end);
        while (shortestPath != null) {
            paths.add(shortestPath);
            eliminatePath(shortestPath, maze);
            shortestPath = getShortestPath(maze, nbRow, nbColumn, start, end);
        }
        return paths;
    }

    @Override
    public int updateScore(List<Node> currentPathNodes, Maze mazeGenerator, Set<String> foundWords,int score) {
        Set<String> newWords = new HashSet<>();
        List<Node> nodesToReplace = new ArrayList<>();
        int pathLength = currentPathNodes.size();
        for (int start = 0; start < pathLength; start++) {
            StringBuilder wordBuilder = new StringBuilder();
            for (int end = start; end < pathLength; end++) {
                wordBuilder.append(currentPathNodes.get(end).getValue());
                String word = wordBuilder.toString().toLowerCase();

                if (mazeGenerator.containsWord(word) && !foundWords.contains(word)) {
                    newWords.add(word);

                    for (int i = start; i <= end; i++) {
                        nodesToReplace.add(currentPathNodes.get(i));
                    }
                }
            }
        }

        // Ajout des logs pour les nouveaux mots trouvés
        for (String word : newWords) {
            int points = word.length();
            score += points;
            foundWords.add(word);
            System.out.println("Mot trouvé: \"" + word + "\" (" + points + " points)");
        }

        // Log du total des points
        if (!newWords.isEmpty()) {
            System.out.println("Total gagné ce tour: " + newWords.stream().mapToInt(String::length).sum() + " points");
        }

        return score;
    }

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

    private void eliminatePath(List<Node> path, Node[][] maze) {
        for (int i = 0; i < path.size(); i++) {
            Node node = path.get(i);
           if(i!=0 && i!=path.size()-1){
               maze[node.getRow()][node.getColumn()].setValue('#');
           }
        }
    }


    void initDist(int[][] dist) {
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
    }

    boolean possibleMove(int dRow, int dCol, boolean[] cBorders, boolean[] nBorders) {
        return switch (dRow) {
            case -1 -> {
                yield switch (dCol) {
                    case -1 -> (!cBorders[0] && !cBorders[3]) && (!nBorders[1] && !nBorders[2]);
                    case 0 -> !cBorders[0] && !nBorders[2];
                    case 1 -> (!cBorders[0] && !cBorders[1]) && (!nBorders[2] && !nBorders[3]);
                    default -> false;
                };
            }
            case 1 -> {
                yield switch (dCol) {
                    case -1 -> (!cBorders[2] && !cBorders[3]) && (!nBorders[1] && !nBorders[0]);
                    case 0 -> !cBorders[2] && !nBorders[0];
                    case 1 -> (!cBorders[1] && !cBorders[2]) && (!nBorders[0] && !nBorders[3]);
                    default -> false;
                };
            }
            case 0 -> {
                yield switch (dCol) {
                    case -1 -> !cBorders[3] && !nBorders[1];
                    case 0 -> true;
                    case 1 -> !cBorders[1] && !nBorders[3];
                    default -> false;
                };
            }
            default -> false;
        };
    }


    private List<Node> reconstructPath(Map<Cell, Cell> parentMap, Cell endCell, Node[][] maze) {
        List<Node> path = new ArrayList<>();
        Cell current = endCell;

        // Backtrack from the end cell to the start cell using the parent map
        while (current != null) {
            path.add(maze[current.row][current.col]);
            current = parentMap.get(current);
        }

        // Reverse the path to get it from start to end
        Collections.reverse(path);
        return path;
    }

}