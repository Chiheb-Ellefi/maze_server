package org.algorithm.maze.impl.prims_algorithm;

import org.algorithm.components.Node;
import org.algorithm.maze.Maze;

import java.util.ArrayList;
import java.util.List;

public class PrimsAlgorithm extends Maze {

    public PrimsAlgorithm(int nbRow, int nbColumn) {
        super(nbRow, nbColumn);
    }

    @Override
    public void generateMaze() {
        Node current = start;
        List<Node> frontier = new ArrayList<>();
        current.setPartOfMaze(true);
        int nb=0;
        int index=0;
        String word=dictionary.get(nb);
        frontier.add(current);
        do {
            current = frontier.get(random.nextInt(frontier.size()));
            maze[current.getRow()][current.getColumn()].setValue(word.charAt(index));
            index++;
            // mark current node as visited
            maze[current.getRow()][current.getColumn()].setPartOfMaze(true);
            // get the visited neighbors
            List<Node> visitedNeighbors = getVisitedNeighbors(getNeighbors(current));
            if (!visitedNeighbors.isEmpty()) {
                // choose random visited neighbor
                Node neighbor = visitedNeighbors.get(random.nextInt(visitedNeighbors.size()));
                removeWallBetween(maze[current.getRow()][current.getColumn()], maze[neighbor.getRow()][neighbor.getColumn()]);
            }
            //add unvisited neighbors to the frontier set
            for (Node neighbor : getNeighbors(current)) {
                if (!neighbor.isPartOfMaze() && !frontier.contains(neighbor)) {
                    frontier.add(neighbor);
                }
            }
            if(nb < dictionary.size() - 1){
                if (index == word.length()  ) {
                    nb++;
                    word = dictionary.get(nb);
                    index = 0;
                }
            }else{
                dictionary=mazePopulator.getData();
            }
            frontier.remove(current);
        }while(!frontier.isEmpty());
    }

}
