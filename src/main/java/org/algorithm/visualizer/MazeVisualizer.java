package org.algorithm.visualizer;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.media.AudioClip;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.algorithm.components.Node;

import org.algorithm.maze.impl.prims_algorithm.PrimsAlgorithm;
import org.algorithm.maze_solver.MazeSolver;
import org.algorithm.maze_solver.impl.BfsAlgorithm;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;


public class MazeVisualizer extends Application {
    private static final int CELL_SIZE = 50;
    private static final int MAZE_WIDTH = 37;
    private static final int MAZE_HEIGHT = 15;
    private static final int TURN_TIME = 60;
    private Pane mazePane;
    private Arc player;
    private int playerRow;
    private int playerCol;
    private Node[][] maze;
    private Node startNode;
    private Node endNode;
    private Rectangle[][] cellRectangles;
    private Button[] controlButtons;
    private Label scoreLabel;
    private Label timeLabel;
    private int currentScore = 0;
    private Rectangle[][] playerPathCells;
    private Arc pacmanArc;
    private Timeline pacmanAnimation;
    private AudioClip wakawaka;
    private  Timer timer;
    private int timeRemaining;
    private AudioClip victory;
    private AudioClip lose;
    private AudioClip gameStart;
    private AudioClip bonus;
    private boolean mouthOpen = true;
    private Stack<Node> currentPath;
    private MazeSolver solver;
    private static final Color BACKGROUND_COLOR = Color.web("#000000");
    private static final Color WALL_COLOR = Color.web("#2121DE");
    private static final Color PLAYER_COLOR = Color.web("#FFFF00");
    private static final Color PLAYER_PATH_COLOR = Color.web("#FFFF0040");
    private static final Color TEXT_COLOR = Color.web("#FFFFFF");
    private static final int PLAYER_SIZE = CELL_SIZE * 2 / 5;
    private  PrimsAlgorithm mazeGenerator;
    // In MazeVisualizer
    public static final StringProperty themeProperty = new SimpleStringProperty("");
    private boolean gameRunning = true;
    Set<String> foundWords;
    @Override
    public void init() throws Exception {
        // Initialize maze generator
         mazeGenerator = new PrimsAlgorithm(MAZE_HEIGHT, MAZE_WIDTH);
        mazeGenerator.setStartAndEnd();
        mazeGenerator.generateMaze();
        mazeGenerator.populateThisMaze();
        solver=new BfsAlgorithm();
        maze = mazeGenerator.getMaze();
        startNode = mazeGenerator.getStart();
        endNode = mazeGenerator.getEnd();
        foundWords = new HashSet<>();
        currentPath = new Stack<>();
        currentPath.push(maze[startNode.getRow()][startNode.getColumn()]);
        // Load audio resources
        wakawaka = new AudioClip(getClass().getResource("/sounds/wakawaka.wav").toExternalForm());
        gameStart = new AudioClip(getClass().getResource("/sounds/game_start.wav").toExternalForm());
        victory = new AudioClip(getClass().getResource("/sounds/victory.wav").toExternalForm());
        bonus = new AudioClip(getClass().getResource("/sounds/bonus.wav").toExternalForm());
        lose=new AudioClip(getClass().getResource("/sounds/death.wav").toExternalForm());

        initializePacmanAnimation();
        initializeTimer();
    }
    private void initializeTimer() {
        timer = new Timer(1000, e -> {
            if (timeRemaining > 0) {
                timeRemaining--;
                Platform.runLater(() -> updateTimeLabel());
            } else {
                gameRunning = false;
                timer.stop();
                pacmanAnimation.stop();
                Platform.runLater(() -> {
                    lose.play();
                    disableControls();
                    showLoseAlert(currentScore);
                });
            }
        });
        timer.start();
    }
    private void disableControls() {
        for (Button button : controlButtons) {
            button.setDisable(true);
        }
    }

    // Add method to enable controls
    private void enableControls() {
        for (Button button : controlButtons) {
            button.setDisable(false);
        }
    }
    private void updateTimeLabel() {
        if (timeLabel != null) {
            timeLabel.setText("Time: " + timeRemaining);
            if (timeRemaining <= 5) {
                timeLabel.setTextFill(Color.RED);
            } else {
                timeLabel.setTextFill(Color.WHITE);
            }
        }
    }
    private void initializePacmanAnimation() {
        pacmanArc = new Arc(0, 0, PLAYER_SIZE, PLAYER_SIZE, 45, 270);
        pacmanArc.setFill(PLAYER_COLOR);
        pacmanArc.setType(ArcType.ROUND);

        pacmanAnimation = new Timeline(
                new KeyFrame(Duration.millis(100), e -> {
                    mouthOpen = !mouthOpen;
                    pacmanArc.setStartAngle(mouthOpen ? 5 : 45);
                    pacmanArc.setLength(mouthOpen ? 350 : 270);
                })
        );
        pacmanAnimation.setCycleCount(Timeline.INDEFINITE);
        pacmanAnimation.play();
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #000000;");

        // Create maze container
        StackPane mazeContainer = new StackPane();
        mazeContainer.setPadding(new Insets(20));
        mazeContainer.setAlignment(Pos.CENTER);

        mazePane = new Pane();
        mazePane.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        // Calculate maze dimensions
        double mazeWidth = MAZE_WIDTH * CELL_SIZE;
        double mazeHeight = MAZE_HEIGHT * CELL_SIZE;
        mazePane.setPrefSize(mazeWidth, mazeHeight);

        // Center the maze
        VBox centeringBox = new VBox(mazePane);
        centeringBox.setAlignment(Pos.CENTER);
       // Center the maze by adding it directly to the StackPane
        mazeContainer.getChildren().add(centeringBox);
        StackPane.setAlignment(mazePane, Pos.CENTER);


        // Initialize path tracking
        playerPathCells = new Rectangle[MAZE_HEIGHT][MAZE_WIDTH];

        // Create status bar
        HBox statusBar = new HBox(20);
        statusBar.setAlignment(Pos.CENTER);
        statusBar.setPadding(new Insets(10));
        statusBar.setStyle("-fx-background-color: #000000; -fx-border-color: #2121DE; -fx-border-width: 0 0 2 0;");

        scoreLabel = new Label("Score: 0");
        Font arcadeFont = Font.font("Arial", FontWeight.BOLD, 16);
        styleLabel(scoreLabel, arcadeFont);

        Label themeLabel = new Label();
        themeLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> "Theme: " + themeProperty.get().toUpperCase(),
                        themeProperty
                )
        );
        timeLabel = new Label("Time: "+TURN_TIME);
// Change text as needed
        styleLabel(timeLabel, arcadeFont);
        styleLabel(themeLabel, arcadeFont);
        statusBar.getChildren().addAll(timeLabel,scoreLabel);
        VBox topContainer = new VBox(5, themeLabel, statusBar);
        topContainer.setAlignment(Pos.CENTER);
        root.setTop(topContainer);

        // Create control buttons
        HBox controlBox = new HBox(15);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(15));
        controlBox.setStyle("-fx-background-color: #000000; -fx-border-color: #2121DE; -fx-border-width: 2 0 0 0;");

        controlButtons = new Button[] {
                createPacManButton("↑", -1, 0),
                createPacManButton("↓", 1, 0),
                createPacManButton("←", 0, -1),
                createPacManButton("→", 0, 1),
                createPacManButton("↖", -1, -1),
                createPacManButton("↗", -1, 1),
                createPacManButton("↙", 1, -1),
                createPacManButton("↘", 1, 1)
        };

        Button regenerateButton = createPacManButton("New Maze", 0, 0);
        regenerateButton.setOnAction(e -> regenerateMaze());

        controlBox.getChildren().addAll(controlButtons);
        controlBox.getChildren().add(regenerateButton);

        root.setBottom(controlBox);
        root.setCenter(mazeContainer);

        regenerateMaze();

        // Set up the scene
        double windowWidth = Math.max(MAZE_WIDTH * CELL_SIZE + 100, 800);
        double windowHeight = MAZE_HEIGHT * CELL_SIZE + 200;
        Scene scene = new Scene(root, windowWidth, windowHeight);

        // Add keyboard controls
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case Z -> movePlayer(-1, 0);  // Up
                case S -> movePlayer(1, 0);   // Down
                case Q -> movePlayer(0, -1);  // Left
                case D -> movePlayer(0, 1);   // Right
                case E -> movePlayer(-1, 1);  // Top-right
                case A -> movePlayer(-1, -1); // Top-left
                case W -> movePlayer(1, -1);  // Bottom-left
                case C -> movePlayer(1, 1);   // Bottom-right
            }
        });

        primaryStage.setTitle("Pac-Man Maze Game");
        primaryStage.setScene(scene);
        primaryStage.show();
        gameStart.play();
    }

    private void styleLabel(Label label, Font font) {
        label.setFont(font);
        label.setTextFill(TEXT_COLOR);
        label.setStyle("-fx-effect: dropshadow(gaussian, #2121DE, 2, 0.5, 0, 0);");
    }

    private Button createPacManButton(String text, int deltaRow, int deltaCol) {
        Button button = new Button(text);
        button.setStyle("""
            -fx-background-color: #2121DE;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #0000FF, 5, 0.5, 0, 0);
            -fx-border-color: #4242FF;
            -fx-border-width: 2;
            """);
        button.setFont(Font.font("Arial", 16));
        button.setMinSize(50, 50);

        if (!text.equals("New Maze")) {
            button.setOnAction(e -> movePlayer(deltaRow, deltaCol));
        }

        // Hover effects
        button.setOnMouseEntered(e -> button.setStyle("""
            -fx-background-color: #4242FF;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #0000FF, 8, 0.8, 0, 0);
            -fx-border-color: #6363FF;
            -fx-border-width: 2;
            """));
        button.setOnMouseExited(e -> button.setStyle("""
            -fx-background-color: #2121DE;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #0000FF, 5, 0.5, 0, 0);
            -fx-border-color: #4242FF;
            -fx-border-width: 2;
            """));

        return button;
    }

    private void movePlayer(int deltaRow, int deltaCol) {
        if (!gameRunning) return;  // Don't allow movement if game is not running

        int newRow = playerRow + deltaRow;
        int newCol = playerCol + deltaCol;

        if (isValidMove(playerRow, playerCol, newRow, newCol)) {
            wakawaka.play();

            double angle = Math.toDegrees(Math.atan2(deltaRow, deltaCol));
            pacmanArc.setRotate(angle);

            updatePlayerPosition(newRow, newCol);
            Node node = maze[newRow][newCol];
            Node previousNode = currentPath.peek();
            if (previousNode.getRow() == node.getRow() && previousNode.getColumn() == node.getColumn()) {
                currentPath.pop();
            } else {
                currentPath.push(node);
            }
            int score = solver.updateScore(currentPath.stream().toList(), mazeGenerator, foundWords, currentScore);
            if(score!=currentScore) {
                currentScore = score;
                scoreLabel.setText("Score: " + score);
                bonus.play();
            }

            if (newRow == endNode.getRow() && newCol == endNode.getColumn()) {
                gameRunning = false;  // Stop the game on victory
                victory.play();
                showVictoryAlert(score);
            }
        }
    }

    private void updatePlayerPosition(int newRow, int newCol) {
        // Add path cell for previous position
        if (playerPathCells[playerRow][playerCol] == null) {
            Rectangle pathCell = new Rectangle(
                    playerCol * CELL_SIZE,
                    playerRow * CELL_SIZE,
                    CELL_SIZE,
                    CELL_SIZE
            );
            pathCell.setFill(PLAYER_PATH_COLOR);
            mazePane.getChildren().add(pathCell);
            playerPathCells[playerRow][playerCol] = pathCell;
        }

        playerRow = newRow;
        playerCol = newCol;
        player.setCenterX(newCol * CELL_SIZE + CELL_SIZE / 2);
        player.setCenterY(newRow * CELL_SIZE + CELL_SIZE / 2);
    }

    private boolean isValidMove(int oldRow, int oldCol, int newRow, int newCol) {
        if (newRow < 0 || newRow >= MAZE_HEIGHT || newCol < 0 || newCol >= MAZE_WIDTH) return false;

        int rowDiff = newRow - oldRow;
        int colDiff = newCol - oldCol;

        return possibleMove(rowDiff, colDiff,
                maze[oldRow][oldCol].getBorders(),
                maze[newRow][newCol].getBorders());
    }

    private boolean possibleMove(int dRow, int dCol, boolean[] cBorders, boolean[] nBorders) {
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

    private void regenerateMaze() {
        // Generate new maze
        gameRunning = true;
        timeRemaining = TURN_TIME;
        timer.restart();
        updateTimeLabel();
        enableControls();
        pacmanAnimation.play();
         mazeGenerator = new PrimsAlgorithm(MAZE_HEIGHT, MAZE_WIDTH);
        mazeGenerator.setStartAndEnd();
        mazeGenerator.generateMaze();
        mazeGenerator.populateThisMaze();
        solver=new BfsAlgorithm();
        maze = mazeGenerator.getMaze();
        startNode = mazeGenerator.getStart();
        endNode = mazeGenerator.getEnd();
        foundWords = new HashSet<>();
        currentPath = new Stack<>();
        currentPath.push(maze[startNode.getRow()][startNode.getColumn()]);
        // Reset score
        currentScore = 0;
        scoreLabel.setText("Score: 0");

        // Clear and redraw maze
        mazePane.getChildren().clear();
        cellRectangles = new Rectangle[MAZE_HEIGHT][MAZE_WIDTH];
        playerPathCells = new Rectangle[MAZE_HEIGHT][MAZE_WIDTH];

        for (int y = 0; y < MAZE_HEIGHT; y++) {
            for (int x = 0; x < MAZE_WIDTH; x++) {
                Node node = maze[y][x];
                double startX = x * CELL_SIZE;
                double startY = y * CELL_SIZE;

                Rectangle bgRect = new Rectangle(startX, startY, CELL_SIZE, CELL_SIZE);
                bgRect.setFill(Color.TRANSPARENT);
                mazePane.getChildren().add(bgRect);
                cellRectangles[y][x] = bgRect;

                // Draw walls
                boolean[] borders = node.getBorders();
                if (borders[0]) drawPacManWall(startX, startY, startX + CELL_SIZE, startY);
                if (borders[1]) drawPacManWall(startX + CELL_SIZE, startY, startX + CELL_SIZE, startY + CELL_SIZE);
                if (borders[2]) drawPacManWall(startX, startY + CELL_SIZE, startX + CELL_SIZE, startY + CELL_SIZE);
                if (borders[3]) drawPacManWall(startX, startY, startX, startY + CELL_SIZE);

                // Draw node value
                Label label = new Label(String.valueOf(node.getValue()));
                label.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 20));
                label.setTextFill(TEXT_COLOR);
                label.setLayoutX(startX + (CELL_SIZE - label.getWidth()) / 3);
                label.setLayoutY(startY + (CELL_SIZE - label.getWidth()) / 4);
                label.setAlignment(Pos.CENTER);
                mazePane.getChildren().add(label);

                // Draw special cells (start/end)
                if (node.getRow() == startNode.getRow() && node.getColumn() == startNode.getColumn()) {
                    drawSpecialPacManCell(startX, startY, String.valueOf(node.getValue()), "#FFD700");
                } else if (node.getRow() == endNode.getRow() && node.getColumn() == endNode.getColumn()) {
                    drawSpecialPacManCell(startX, startY, String.valueOf(node.getValue()), "#FF0000");
                }
            }
        }

        // Set initial player position to start node
        playerRow = startNode.getRow();
        playerCol = startNode.getColumn();
        createPacManPlayer();
    }

    private void drawPacManWall(double startX, double startY, double endX, double endY) {
        Line wall = new Line(startX, startY, endX, endY);
        wall.setStroke(WALL_COLOR);
        wall.setStrokeWidth(3);
        wall.setStyle("-fx-effect: dropshadow(gaussian, #0000FF, 5, 0.5, 0, 0);");
        mazePane.getChildren().add(wall);
    }

    private void drawSpecialPacManCell(double x, double y, String value, String color) {
        Rectangle rect = new Rectangle(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
        rect.setFill(Color.web(color + "40"));
        rect.setStroke(Color.web(color));
        rect.setStrokeWidth(3);
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        rect.setStyle("-fx-effect: dropshadow(gaussian, " + color + ", 10, 0.5, 0, 0);");
        mazePane.getChildren().add(rect);
    }

    private void createPacManPlayer() {
        player = pacmanArc;
        player.setFill(PLAYER_COLOR);
        player.setStroke(Color.TRANSPARENT);
        player.setStrokeWidth(2);
        updatePlayerPosition(playerRow, playerCol);
        mazePane.getChildren().add(player);

        if (!pacmanAnimation.getStatus().equals(Animation.Status.RUNNING)) {
            pacmanAnimation.play();
        }
    }

    private void showVictoryAlert(int score) {
        timer.stop();
        Stage dialogStage = new Stage(StageStyle.TRANSPARENT);
        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(30));
        dialogVbox.setStyle("""
            -fx-background-color: #000000;
            -fx-border-color: #2121DE;
            -fx-border-width: 3;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, #0000FF, 20, 0.5, 0, 0);
            """);

        Label titleLabel = new Label("VICTORY!");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#FFD700"));
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, #FFD700, 10, 0.7, 0, 0);");

        Label messageLabel = new Label("Congratulations! You reached the end!");
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        messageLabel.setTextFill(Color.WHITE);

        Label scoreLabel = new Label(String.format("Final Score: %d", score));
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        scoreLabel.setTextFill(Color.WHITE);

        Button newGameButton = new Button("New Game");
        newGameButton.setStyle("""
            -fx-background-color: #2121DE;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 16;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-effect: dropshadow(gaussian, #0000FF, 5, 0.5, 0, 0);
            """);
        newGameButton.setOnAction(e -> {
            dialogStage.close();
            regenerateMaze();
        });

        Button closeButton = new Button("Close");
        closeButton.setStyle("""
            -fx-background-color: #2121DE;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 16;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-effect: dropshadow(gaussian, #0000FF, 5, 0.5, 0, 0);
            """);
        closeButton.setOnAction(e -> dialogStage.close());

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(newGameButton, closeButton);

        dialogVbox.getChildren().addAll(titleLabel, messageLabel, scoreLabel, buttonBox);

        Scene dialogScene = new Scene(dialogVbox);
        dialogScene.setFill(null);
        dialogStage.setScene(dialogScene);

        // Center the dialog on the screen
        dialogStage.setOnShown(e -> {
            Stage mainStage = (Stage) mazePane.getScene().getWindow();
            dialogStage.setX(mainStage.getX() + (mainStage.getWidth() - dialogStage.getWidth()) / 2);
            dialogStage.setY(mainStage.getY() + (mainStage.getHeight() - dialogStage.getHeight()) / 2);
        });

        dialogStage.show();
    }
    private void showLoseAlert(int score) {
        Stage dialogStage = new Stage(StageStyle.TRANSPARENT);
        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(30));
        dialogVbox.setStyle("""
        -fx-background-color: #000000;
        -fx-border-color: #FF0000;
        -fx-border-width: 3;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
        -fx-effect: dropshadow(gaussian, #FF0000, 20, 0.5, 0, 0);
        """);

        Label titleLabel = new Label("TIME'S UP!");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#FF0000"));
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, #FF0000, 10, 0.7, 0, 0);");

        Label messageLabel = new Label("You ran out of time!");
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        messageLabel.setTextFill(Color.WHITE);

        Label scoreLabel = new Label(String.format("Final Score: %d", score));
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        scoreLabel.setTextFill(Color.WHITE);

        Button newGameButton = new Button("Try Again");
        newGameButton.setStyle("""
        -fx-background-color: #FF0000;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-font-size: 16;
        -fx-padding: 10 20;
        -fx-background-radius: 5;
        -fx-effect: dropshadow(gaussian, #FF0000, 5, 0.5, 0, 0);
        """);
        newGameButton.setOnAction(e -> {
            dialogStage.close();
            gameRunning = true;  // Reset game state
            timeRemaining = TURN_TIME;
            enableControls();  // Re-enable controls
            regenerateMaze();
            pacmanAnimation.play();  // Restart Pacman animation
            timer.start();
        });

        Button closeButton = new Button("Close");
        closeButton.setStyle("""
        -fx-background-color: #FF0000;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-font-size: 16;
        -fx-padding: 10 20;
        -fx-background-radius: 5;
        -fx-effect: dropshadow(gaussian, #FF0000, 5, 0.5, 0, 0);
        """);
        closeButton.setOnAction(e -> dialogStage.close());

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(newGameButton, closeButton);

        dialogVbox.getChildren().addAll(titleLabel, messageLabel, scoreLabel, buttonBox);

        Scene dialogScene = new Scene(dialogVbox);
        dialogScene.setFill(null);
        dialogStage.setScene(dialogScene);

        // Center the dialog on the screen
        dialogStage.setOnShown(e -> {
            Stage mainStage = (Stage) mazePane.getScene().getWindow();
            dialogStage.setX(mainStage.getX() + (mainStage.getWidth() - dialogStage.getWidth()) / 2);
            dialogStage.setY(mainStage.getY() + (mainStage.getHeight() - dialogStage.getHeight()) / 2);
        });

        dialogStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}