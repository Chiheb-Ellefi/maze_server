package org.algorithm.maze;

import org.algorithm.components.Node;
import org.algorithm.data.MazePopulator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public abstract class Maze {
    protected final int nbColumn;
    protected final  int nbRow;
    protected final  Node[][] maze;
    protected Node start;
    protected Node end;
    protected List<String> dictionary;
    protected final MazePopulator mazePopulator;
    protected final Random random;
    protected   Set<String> dictionaryLower = new HashSet<>();
   public Maze(int nbRow,int nbColumn){
        this.nbColumn=nbColumn;
        this.nbRow=nbRow;
       this.maze = new Node[nbRow][nbColumn];
       this.start=new Node(0,0);
       this.end=new Node(nbRow-1,nbColumn-1);
       this.random = new Random();
       this.mazePopulator=new MazePopulator();
       for (int i = 0; i < nbRow; i++) {
           for (int j = 0; j < nbColumn; j++) {
               maze[i][j] = new Node(i, j);
           }
       }
       this.dictionary=mazePopulator.getData();
       for (String word : this.dictionary) {
           dictionaryLower.add(word.toLowerCase());
       }
       for(int i=0;i<dictionary.size()/2;i++){
           int wordLen= random.nextInt(3)+2;
           StringBuilder wordBuilder= new StringBuilder();
           for(int j=0;j<wordLen;j++){
               wordBuilder.append((char) (random.nextInt(26) + 'A'));
           }
           int index= random.nextInt(dictionary.size());
           this.dictionary.add(index,wordBuilder.toString().toLowerCase());

       }
    }
    public String getTheme() {
        String theme = mazePopulator.getWordTheme();
        System.out.println("Retrieved theme: " + theme);
        return theme;
    }

    public Node getStart() {
        return start;
    }
    public Node getEnd() {
        return end;
    }
    public Node[][] getMaze() {
        return maze;
    }
    public void  setStartAndEnd(){
         boolean isVertical = random.nextBoolean();
         if (isVertical) {
             start.setColumn(0);
             end.setColumn(nbColumn - 1);
             start.setRow(random.nextInt(nbRow));

             end.setRow(start.getRow()<nbRow/2?random.nextInt(nbRow/2,nbRow):random.nextInt(0,nbRow/2));
         } else {
             start.setRow(0);
             end.setRow(nbRow - 1);
             start.setColumn(random.nextInt(nbColumn));
             end.setColumn(start.getColumn()<nbColumn/2?random.nextInt(nbColumn/2,nbColumn):random.nextInt(0,nbColumn/2));
         }
         while (start.getRow() == end.getRow() && start.getColumn() == end.getColumn()) {
             if (isVertical) {
                 end.setRow(random.nextInt(nbRow));
             } else {
                 end.setColumn(random.nextInt(nbColumn));
             }
         }
   }
    protected List<Node> getNeighbors( Node current) {
        List<Node> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] direction : directions) {
            int newRow = current.incrementRow(direction[0]);
            int newCol = current.incrementColumn(direction[1]);

            if (newRow >= 0 && newRow < nbRow &&
                    newCol >= 0 && newCol < nbColumn) {
                neighbors.add(maze[newRow][newCol]);
            }
        }
        return neighbors;
    }
    protected List<Node> getUnvisitedNeighbors(List<Node> neighbors){
       List<Node> unVisitedNeighbors = new ArrayList<>();
       for (Node node : neighbors) {
           if (!node.isPartOfMaze()) {
               unVisitedNeighbors.add(node);
           }
       }
       return unVisitedNeighbors;
   }
    protected List<Node> getVisitedNeighbors(List<Node> neighbors) {
        List<Node> visitedNeighbors = new ArrayList<>();
        for (Node node : neighbors) {
            if (node.isPartOfMaze()) {
                visitedNeighbors.add(node);
            }
        }
        return visitedNeighbors;
    }
    protected void removeWallBetween(Node current, Node next) {
        int rowDiff = next.getRow() - current.getRow();
        int colDiff = next.getColumn() - current.getColumn();

        if (rowDiff == -1) {
            current.removeBorder(0);
            next.removeBorder(2);
        } else if (rowDiff == 1) {
            current.removeBorder(2);
            next.removeBorder(0);
        } else if (colDiff == -1) {
            current.removeBorder(3);
            next.removeBorder(1);
        } else if (colDiff == 1) {
            current.removeBorder(1);
            next.removeBorder(3);
        }
    }
    public void createLoops() {
        int numberOfLoops=(int)(nbRow*nbColumn*0.4);
        int loopsCreated = 0;
        while (loopsCreated < numberOfLoops) {
            int col = random.nextInt(nbColumn);
            int row = random.nextInt(nbRow);
            int border = random.nextInt(4);
            if (isBorderValid(row, col, border)) {
                maze[row][col].removeBorder(border);
                int neighborRow = row;
                int neighborCol = col;
                switch (border) {
                    case 0:
                        neighborRow--;
                        break;
                    case 1:
                        neighborCol++;
                        break;
                    case 2:
                        neighborRow++;
                        break;
                    case 3:
                        neighborCol--;
                        break;
                }

                int oppositeBorder = (border + 2) % 4;
                maze[neighborRow][neighborCol].removeBorder(oppositeBorder);

                loopsCreated++;
            }
        }
    }
    protected boolean isBorderValid(int row, int col, int border) {
        return switch (border) {
            case 0 -> row > 0;
            case 1 -> col < nbColumn - 1;
            case 2 -> row < nbRow - 1;
            case 3 -> col > 0;
            default -> false;
        };
    }
    public void printMaze() {
        for (int i = 0; i < nbRow; i++) {
            for (int j = 0; j < nbColumn; j++) {
                System.out.print(maze[i][j].isPartOfMaze() ? maze[i][j].getValue() : "#");
            }
            System.out.println();
        }

    }
    public abstract void generateMaze();
    public byte[] serializeMaze() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(maze);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean containsWord(String word) {
        return dictionaryLower.contains(word.toLowerCase());
    }
}
