package org.algorithm.maze_solver;

import org.algorithm.components.Node;
import org.algorithm.maze.Maze;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public interface MazeSolver {
    List<Node> getShortestPath(Node[][] maze,int nbRow,int nbColumn,Node start,Node end);
    List<List<Node>> getAllPaths(Node[][] maze,int nbRow,int nbColumn,Node start,Node end);
     int updateScore(List<Node> currentPathNodes, Maze mazeGenerator, Set<String> foundWords,int score);

}
