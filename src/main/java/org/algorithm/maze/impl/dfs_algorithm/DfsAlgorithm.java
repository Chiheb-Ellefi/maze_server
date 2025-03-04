package org.algorithm.maze.impl.dfs_algorithm;

import org.algorithm.components.Node;
import org.algorithm.maze.Maze;

import java.util.List;
import java.util.Stack;

public class DfsAlgorithm extends Maze {
    public DfsAlgorithm(int nbRow, int nbColumn) {
        super(nbRow, nbColumn);
    }

    @Override
    public void generateMaze() {
        setStartAndEnd(); // Set the starting and ending points of the maze
        Stack<Node> stack = new Stack<>();
        Stack<Character> injected = new Stack<>();
        Stack<Character> toInject = new Stack<>();

        int index = 0;
        int word = 0;
        String currentWord = dictionary.get(word);
        Node current;
        char currentChar;

        do {
            if (stack.isEmpty()) { // Initialize with the starting node
                current = maze[start.getRow()][start.getColumn()];
                currentChar = currentWord.charAt(index);
                injected.push(currentChar);
                current.setValue(currentChar);
                index++;
                stack.push(current);
                maze[start.getRow()][start.getColumn()].setPartOfMaze(true);
            } else {
                current = stack.peek();
            }

            List<Node> unvisitedNeighbors = getUnvisitedNeighbors(getNeighbors(maze[current.getRow()][current.getColumn()]));
            if (!unvisitedNeighbors.isEmpty()) { // Move to an unvisited neighbor
                Node nextCell = unvisitedNeighbors.get(random.nextInt(unvisitedNeighbors.size()));
                removeWallBetween(current, nextCell);
                stack.push(nextCell);
                maze[nextCell.getRow()][nextCell.getColumn()].setPartOfMaze(true);
                current = nextCell;

                // Inject characters from the current word
                if (toInject.isEmpty()) {
                    currentChar = currentWord.charAt(index);
                    index++;
                } else {
                    currentChar = toInject.pop();
                }
                injected.push(currentChar);
                current.setValue(currentChar);
            } else { // Backtrack when no unvisited neighbors remain
                current = stack.pop();
                currentChar = injected.pop();
                toInject.push(currentChar);
            }

            // Switch to the next word if needed
            if (word < dictionary.size() - 1) {
                if (index == currentWord.length()) {
                    word++;
                    currentWord = dictionary.get(word);
                    index = 0;
                }
            } else { // If all words are used, repopulate the dictionary
                dictionary = mazePopulator.getData();
            }

        } while (!stack.isEmpty());
    }
}
