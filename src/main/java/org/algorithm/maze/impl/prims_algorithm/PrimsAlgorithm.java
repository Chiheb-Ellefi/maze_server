package org.algorithm.maze.impl.prims_algorithm;

import org.algorithm.components.Node;
import org.algorithm.maze.Maze;
import org.algorithm.maze_solver.MazeSolver;
import org.algorithm.maze_solver.impl.BfsAlgorithm;

import java.util.ArrayList;
import java.util.List;

public class PrimsAlgorithm extends Maze {
private MazeSolver solver;
private List<List<Node>> paths;
    public PrimsAlgorithm(int nbRow, int nbColumn) {
        super(nbRow, nbColumn);
       solver=new BfsAlgorithm();
       paths=new ArrayList<>();
    }

    @Override
    public void generateMaze() {
        Node current = start;
        List<Node> frontier = new ArrayList<>();
        current.setPartOfMaze(true);


        frontier.add(current);
        do {
            current = frontier.get(random.nextInt(frontier.size()));
            maze[current.getRow()][current.getColumn()].setValue((char) (random.nextInt(26) + 'A'));
            maze[current.getRow()][current.getColumn()].setPartOfMaze(true);
            List<Node> visitedNeighbors = getVisitedNeighbors(getNeighbors(current));
            if (!visitedNeighbors.isEmpty()) {
                Node neighbor = visitedNeighbors.get(random.nextInt(visitedNeighbors.size()));
                removeWallBetween(maze[current.getRow()][current.getColumn()], maze[neighbor.getRow()][neighbor.getColumn()]);
            }

            for (Node neighbor : getNeighbors(current)) {
                if (!neighbor.isPartOfMaze() && !frontier.contains(neighbor)) {
                    frontier.add(neighbor);
                }
            }

            frontier.remove(current);
        }while(!frontier.isEmpty());


    }

    public void  populateThisMaze(){
        int nb=0;
        int index=0;
        String word=defaultDictionary.get(nb);
        paths=solver.getAllPaths(maze,nbRow,nbColumn,start,end);
        System.out.println("all paths"+paths.size());
        for (List<Node> path : paths) {
            for (Node node : path) {
                // Check if index has reached the end of the current word
                if (index >= word.length()) {
                    if (nb < defaultDictionary.size() - 1) {
                        nb++;
                        word = defaultDictionary.get(nb);
                        index = 0;
                    } else {
                        // Optionally, reset or update the defaultDictionary as needed
                        defaultDictionary = mazePopulator.getData();
                        nb = 0;
                        word = defaultDictionary.get(nb);
                        index = 0;
                    }
                }
                maze[node.getRow()][node.getColumn()].setValue(word.charAt(index));
                index++;
            }
        }

    }


}
