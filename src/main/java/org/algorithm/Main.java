package org.algorithm;

import org.algorithm.components.Node;
import org.algorithm.maze.Maze;
import org.algorithm.maze.impl.dfs_algorithm.DfsAlgorithm;
import org.algorithm.maze_solver.MazeSolver;
import org.algorithm.maze_solver.impl.BfsAlgorithm;
import org.algorithm.maze_solver.impl.DijkstraAlgorithm;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int NB_ROW=20;
        int NB_COLUMN=10;
        Maze mazeGen=new DfsAlgorithm(NB_ROW,NB_COLUMN);
        mazeGen.generateMaze();
        mazeGen.createLoops();
        mazeGen.printMaze();
        MazeSolver solver=new BfsAlgorithm();
        List<Node> shortestPath=   solver.getShortestPath(mazeGen.getMaze(),NB_ROW,NB_COLUMN,mazeGen.getStart(),mazeGen.getEnd());
        System.out.println("Start:"+mazeGen.getStart().getRow()+','+mazeGen.getStart().getColumn());
        System.out.println("End:"+mazeGen.getEnd().getRow()+','+mazeGen.getEnd().getColumn());
     for(Node node:shortestPath){
         System.out.println("("+node.getRow()+","+node.getColumn()+")");
     }
        System.out.println(solver.getAllPaths(mazeGen.getMaze(),NB_ROW,NB_COLUMN,mazeGen.getStart(),mazeGen.getEnd()).size());
    }

    }
