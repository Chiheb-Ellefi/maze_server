package org.algorithm.maze.impl;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.algorithm.components.Node;
import org.algorithm.maze.Maze;
import org.algorithm.maze.impl.dfs_algorithm.DfsAlgorithm;
import org.algorithm.maze.impl.prims_algorithm.PrimsAlgorithm;
import org.algorithm.maze_solver.MazeSolver;
import org.algorithm.maze_solver.impl.DijkstraAlgorithm;

import java.util.List;

public class MazeVisualizer extends Application {

    private static final int CELL_SIZE = 30;
    private static final int MAZE_WIDTH = 40;
    private static final int MAZE_HEIGHT = 20;

    private Pane mazePane;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Create a centered maze pane with black background
        StackPane centerPane = new StackPane();
        mazePane = new Pane();
        mazePane.setStyle("-fx-background-color: black;");
        mazePane.setPrefSize(MAZE_WIDTH * CELL_SIZE, MAZE_HEIGHT * CELL_SIZE);
        centerPane.getChildren().add(mazePane);
        root.setCenter(centerPane);

        // Buttons for maze generation
        Button primButton = new Button("Generate with Prim's");
        primButton.setOnAction(e -> regenerateMaze(new PrimsAlgorithm(MAZE_HEIGHT, MAZE_WIDTH)));

        Button dfsButton = new Button("Generate with DFS");
        dfsButton.setOnAction(e -> regenerateMaze(new DfsAlgorithm(MAZE_HEIGHT, MAZE_WIDTH)));

        Pane buttonPane = new Pane(primButton, dfsButton);
        primButton.setLayoutX(10);
        primButton.setLayoutY(10);
        dfsButton.setLayoutX(200);
        dfsButton.setLayoutY(10);
        root.setBottom(buttonPane);
        BorderPane.setAlignment(buttonPane, Pos.CENTER);

        // Initial maze generation
        regenerateMaze(new PrimsAlgorithm(MAZE_HEIGHT, MAZE_WIDTH));

        Scene scene = new Scene(root, MAZE_WIDTH * CELL_SIZE, MAZE_HEIGHT * CELL_SIZE + 80);
        primaryStage.setTitle("Pac-Man Maze Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void regenerateMaze(Maze mazeGenerator) {
        mazePane.getChildren().clear();
        mazeGenerator.setStartAndEnd();
        mazeGenerator.generateMaze();
        mazeGenerator.createLoops();

        Node start = mazeGenerator.getStart();
        Node end = mazeGenerator.getEnd();

        // Optional: Solve maze (not visually emphasized here)
        MazeSolver solver = new DijkstraAlgorithm();
        List<Node> path = solver.getShortestPath(mazeGenerator.getMaze(), MAZE_HEIGHT, MAZE_WIDTH, start, end);

        // Draw maze with Pac-Man style
        for (int y = 0; y < MAZE_HEIGHT; y++) {
            for (int x = 0; x < MAZE_WIDTH; x++) {
                Node node = mazeGenerator.getMaze()[y][x];
                boolean[] walls = node.getBorders();
                char val = node.getValue();

                double startX = x * CELL_SIZE;
                double startY = y * CELL_SIZE;
                double endX = (x + 1) * CELL_SIZE;
                double endY = (y + 1) * CELL_SIZE;

                // Draw walls as thick blue lines
                if (walls[0]) drawLine(startX, startY, endX, startY);
                if (walls[1]) drawLine(endX, startY, endX, endY);
                if (walls[2]) drawLine(startX, endY, endX, endY);
                if (walls[3]) drawLine(startX, startY, startX, endY);

                // Add pellets to paths (excluding start/end)
                if (val == '#' && !isNode(node, start) && !isNode(node, end)) {
                    Circle pellet = new Circle(startX + CELL_SIZE / 2, startY + CELL_SIZE / 2, 2, Color.WHITE);
                    mazePane.getChildren().add(pellet);
                }

                // Add label with red text
                Label label = new Label(String.valueOf(val));
                label.setTextFill(Color.RED); // Set text color to red
                label.setLayoutX(startX + (CELL_SIZE - label.getWidth()) / 3);
                label.setLayoutY(startY+ (CELL_SIZE - label.getWidth()) / 4);
                label.setAlignment(Pos.CENTER);
                mazePane.getChildren().add(label);

                // Mark start (Pac-Man) and end (Ghost)
                if (isNode(node, start)) {
                    mazePane.getChildren().add(new Circle(startX + CELL_SIZE / 2, startY + CELL_SIZE / 2, CELL_SIZE / 3, Color.YELLOW));
                } else if (isNode(node, end)) {
                    mazePane.getChildren().add(new Circle(startX + CELL_SIZE / 2, startY + CELL_SIZE / 2, CELL_SIZE / 3, Color.RED));
                }
            }
        }
    }

    private void drawLine(double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.BLUE);
        line.setStrokeWidth(3);
        mazePane.getChildren().add(line);
    }

    private boolean isNode(Node node, Node target) {
        return node.getRow() == target.getRow() && node.getColumn() == target.getColumn();
    }

    public static void main(String[] args) {
        launch(args);
    }
}