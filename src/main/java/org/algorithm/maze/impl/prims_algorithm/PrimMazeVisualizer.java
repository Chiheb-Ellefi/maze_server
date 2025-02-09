package org.algorithm.maze.impl.prims_algorithm;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.algorithm.components.Node;
import org.algorithm.maze.Maze;
import org.algorithm.maze_solver.MazeSolver;
import org.algorithm.maze_solver.impl.BfsAlgorithm;
import java.util.List;

public class PrimMazeVisualizer extends Application {

    private static final int CELL_SIZE = 30; // Size of each cell in pixels
    private static final int MAZE_WIDTH = 40; // Number of columns in the maze
    private static final int MAZE_HEIGHT = 20; // Number of rows in the maze

    private Pane mazePane; // Pane to hold the maze cells

    @Override
    public void start(Stage primaryStage) {
        // Create a BorderPane to hold the maze and the button
        BorderPane root = new BorderPane();

        // Create a Pane to hold the maze cells
        mazePane = new Pane();
        root.setCenter(mazePane);

        // Create a Button to regenerate the maze
        Button regenerateButton = new Button("Regenerate Maze");
        regenerateButton.setOnAction(event -> regenerateMaze());
        root.setBottom(regenerateButton);
        BorderPane.setAlignment(regenerateButton, Pos.CENTER);

        // Generate the initial maze
        regenerateMaze();

        // Create the scene and set it on the stage
        Scene scene = new Scene(root, MAZE_WIDTH * CELL_SIZE, MAZE_HEIGHT * CELL_SIZE + 40); // Extra space for the button
        primaryStage.setTitle("Prim's Maze Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void regenerateMaze() {
        // Clear the existing maze
        mazePane.getChildren().clear();

        // Generate a new maze
        // Maze generator instance
        Maze mazeGenerator = new PrimsAlgorithm(MAZE_HEIGHT, MAZE_WIDTH);
        mazeGenerator.setStartAndEnd();
        mazeGenerator.generateMaze();
        mazeGenerator.createLoops();
        // Get the start and end nodes
        Node startNode = mazeGenerator.getStart();
        Node endNode = mazeGenerator.getEnd();
        MazeSolver solver=new BfsAlgorithm();
      List<List<Node>> paths= solver.getAllPaths(mazeGenerator.getMaze(),MAZE_HEIGHT,MAZE_WIDTH,startNode,endNode);
        System.out.println("Start:"+startNode.getRow()+','+startNode.getColumn());
        System.out.println("End:"+endNode.getRow()+','+endNode.getColumn());
        for (int i = 0, pathsSize = paths.size(); i < pathsSize; i++) {
            List<Node> path = paths.get(i);
            System.out.print("Path nb:"+i);
            for (Node node : path) {
                /*System.out.print("(" + node.getRow() + "," + node.getColumn() + ")");*/
                System.out.print(node.getValue());
            }
           System.out.println(" ");
        }


        // Draw the new maze
        for (int y = 0; y < MAZE_HEIGHT; y++) {
            for (int x = 0; x < MAZE_WIDTH; x++) {
                Node node = mazeGenerator.getMaze()[y][x];
                boolean[] borders = node.getBorders();
                char value = node.getValue();

                // Calculate cell position
                double startX = x * CELL_SIZE;
                double startY = y * CELL_SIZE;
                double endX = (x + 1) * CELL_SIZE;
                double endY = (y + 1) * CELL_SIZE;

                // Draw borders
                if (borders[0]) { // Top border
                    mazePane.getChildren().add(new Line(startX, startY, endX, startY));
                }
                if (borders[1]) { // Right border
                    mazePane.getChildren().add(new Line(endX, startY, endX, endY));
                }
                if (borders[2]) { // Bottom border
                    mazePane.getChildren().add(new Line(startX, endY, endX, endY));
                }
                if (borders[3]) { // Left border
                    mazePane.getChildren().add(new Line(startX, startY, startX, endY));
                }

                // Add a Label to display the value inside the cell
                Label label = new Label(String.valueOf(value));
                label.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Bold text
                label.setTextFill(Color.BLACK); // Default text color

                // Center the Label in the cell
                label.setLayoutX(startX + (CELL_SIZE - label.getWidth()) / 3); // Center horizontally
                label.setLayoutY(startY); // Center vertically
                label.setAlignment(Pos.CENTER); // Center text within the Label

                // Change text color for start and end cells
                if (node.getRow() == startNode.getRow() && node.getColumn() == startNode.getColumn()) {
                    label.setTextFill(Color.GREEN); // Green text for start
                } else if (node.getRow() == endNode.getRow() && node.getColumn() == endNode.getColumn()) {
                    label.setTextFill(Color.RED); // Red text for end
                }

                mazePane.getChildren().add(label);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}