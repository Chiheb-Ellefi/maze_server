package org.algorithm.maze.impl.prims_algorithm;

import org.algorithm.components.Node;
import org.algorithm.maze.Maze;
import org.algorithm.maze_solver.MazeSolver;
import org.algorithm.maze_solver.impl.BfsAlgorithm;

import java.util.ArrayList;
import java.util.List;

public class PrimsAlgorithm extends Maze {
    // Solver used to find all paths in the maze
    private final MazeSolver solver;
    // List to store all found paths
    private List<List<Node>> paths;

    public PrimsAlgorithm(int nbRow, int nbColumn) {
        super(nbRow, nbColumn);
        solver = new BfsAlgorithm();
        paths = new ArrayList<>();
    }

    @Override
    public void generateMaze() {
        Node current = start;
        List<Node> frontier = new ArrayList<>();
        // Mark the starting node as part of the maze
        current.setPartOfMaze(true);
        frontier.add(current);

        do {
            // Select a random node from the frontier list
            current = frontier.get(random.nextInt(frontier.size()));
            // Set a random character as the node's value (A-Z)
            maze[current.getRow()][current.getColumn()].setValue((char) (random.nextInt(26) + 'A'));
            maze[current.getRow()][current.getColumn()].setPartOfMaze(true);

            // Retrieve already visited neighbors of the current node
            List<Node> visitedNeighbors = getVisitedNeighbors(getNeighbors(current));
            if (!visitedNeighbors.isEmpty()) {
                // Connect current node to one random visited neighbor by removing the wall between them
                Node neighbor = visitedNeighbors.get(random.nextInt(visitedNeighbors.size()));
                removeWallBetween(maze[current.getRow()][current.getColumn()], maze[neighbor.getRow()][neighbor.getColumn()]);
            }

            // Add all unvisited neighbors to the frontier list
            for (Node neighbor : getNeighbors(current)) {
                if (!neighbor.isPartOfMaze() && !frontier.contains(neighbor)) {
                    frontier.add(neighbor);
                }
            }

            // Remove the current node from the frontier
            frontier.remove(current);
        } while (!frontier.isEmpty());
    }

    public void populateThisMaze() {
        int nb = 0;      // Index for the current word in the dictionary
        int index = 0;   // Character index within the current word
        String word = defaultDictionary.get(nb);

        // Retrieve all paths in the maze using the BFS solver
        paths = solver.getAllPaths(maze, nbRow, nbColumn, start, end);
        System.out.println("Number of paths found : " + paths.size());

        // Fill in the maze with characters from the dictionary along each found path
        for (List<Node> path : paths) {
            for (Node node : path) {
                // If the current word is exhausted, move to the next word
                if (index >= word.length()) {
                    if (nb < defaultDictionary.size() - 1) {
                        nb++;
                        word = defaultDictionary.get(nb);
                        index = 0;
                    } else {
                        // If we've reached the end of the dictionary, repopulate it
                        defaultDictionary = mazePopulator.getData();
                        nb = 0;
                        word = defaultDictionary.get(nb);
                        index = 0;
                    }
                }
                // Set the node's value to the current character of the word
                maze[node.getRow()][node.getColumn()].setValue(word.charAt(index));
                index++;
            }
        }
    }
}
