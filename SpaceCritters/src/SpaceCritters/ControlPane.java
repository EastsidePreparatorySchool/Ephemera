/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package SpaceCritters;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author gmein
 */
public final class ControlPane extends VBox {

    final SpaceCritters gameShell;

    final HBox top;
    final VBox viewControls;
    final VBox gameControls;
    final VBox gameStats;
    final VBox bottom;
    final ListView<AlienSpeciesForDisplay> speciesView;

    // inside viewControls
    final public ToggleGroup renderSelector;
    final RadioButton alienView;
    final RadioButton energyView;
    final RadioButton techView;

    // inside gameControls;
    final Button buttonPause;
    final Button buttonConsole;
    final Button buttonHelp;
    final Text turnCounter;
    final Text alienNumber;
    final Text timeForTurn;
    final Text timeForTurnAndAlien;

    //inside
    public ControlPane(SpaceCritters gameShellInstance) {
        gameShell = gameShellInstance;

        top = new HBox();
        viewControls = new VBox();
        gameControls = new VBox();
        gameStats = new VBox();
        bottom = new VBox();

        buttonPause = new Button("Start");
        buttonConsole = new Button("Console");
        buttonHelp = new Button("Help");
        turnCounter = new Text("Turns:        0");
        alienNumber =         new Text("Aliens:       0");
        timeForTurn =         new Text("s/turn:     0ms");
        timeForTurnAndAlien = new Text("s/turn/#:   0ms");
        alienView = new RadioButton("Aliens");
        energyView = new RadioButton("Energy");
        techView = new RadioButton("Tech");

        renderSelector = new ToggleGroup();
        speciesView = new ListView<>(gameShell.species.getObservableList());

        addGameControls();
        addViewControls();
        addStats();
        createSpeciesView();

        top.getChildren().addAll(viewControls, gameControls);
        bottom.getChildren().addAll(speciesView);

        this.setPrefWidth(300);
        this.getChildren().addAll(top, gameStats, speciesView);

        this.setPrefSize(300, 800);
        this.setMaxSize(500, 1920);
        this.setMinSize(200, 500);
    }

    public void addViewControls() {

        viewControls.setPadding(new Insets(15, 12, 15, 12));
        viewControls.setSpacing(10);
        viewControls.setStyle("-fx-background-color: black;");

        alienView.setStyle("-fx-text-fill:white;");
        alienView.setToggleGroup(renderSelector);
        alienView.setSelected(true);
        alienView.setOnAction((e) -> {
            if (alienView.isSelected()) {
                gameShell.field.setRenderMode("Aliens");
            }
        });

        energyView.setStyle("-fx-text-fill:white;");
        energyView.setToggleGroup(renderSelector);
        energyView.setSelected(false);
        energyView.setDisable(true);
        energyView.setOnAction((e) -> {
            if (energyView.isSelected()) {
                gameShell.field.setRenderMode("Energy");
            }
        });

        techView.setStyle("-fx-text-fill:white;");
        techView.setToggleGroup(renderSelector);
        techView.setSelected(false);
        techView.setDisable(true);
        techView.setOnAction((e) -> {
            if (alienView.isSelected()) {
                gameShell.field.setRenderMode("Tech");
            }
        });

        viewControls.getChildren().addAll(alienView, energyView, techView);

    }

    public void addStats() {
        gameStats.setPadding(new Insets(15, 12, 15, 12));
        gameStats.setSpacing(10);
        gameStats.setStyle("-fx-background-color: black;");

        turnCounter.setFont(Font.font("Consolas", FontWeight.NORMAL, 14));
        turnCounter.setStyle("-fx-background-color: black;");
        turnCounter.setFill(Color.WHITE);

        alienNumber.setFont(Font.font("Consolas", FontWeight.NORMAL, 14));
        alienNumber.setStyle("-fx-background-color: black;");
        alienNumber.setFill(Color.WHITE);
        
        timeForTurn.setFont(Font.font("Consolas", FontWeight.NORMAL, 14));
        timeForTurn.setStyle("-fx-background-color: black;");
        timeForTurn.setFill(Color.WHITE);

        timeForTurnAndAlien.setFont(Font.font("Consolas", FontWeight.NORMAL, 14));
        timeForTurnAndAlien.setStyle("-fx-background-color: black;");
        timeForTurnAndAlien.setFill(Color.WHITE);

        gameStats.getChildren().addAll(turnCounter, alienNumber, timeForTurn, timeForTurnAndAlien);
    }

    public void addGameControls() {
        gameControls.setPadding(new Insets(15, 12, 15, 12));
        gameControls.setSpacing(10);
        gameControls.setStyle("-fx-background-color: black;");

        buttonPause.setPrefSize(100, 20);
        buttonPause.setOnAction((ActionEvent e) -> {
            gameShell.startOrPauseGame(e);
        });

        buttonConsole.setPrefSize(100, 20);
        buttonConsole.setOnAction((ActionEvent e) -> {
            gameShell.consoleStage.show();
            gameShell.consoleStage.toFront();
        });

        buttonHelp.setPrefSize(100, 20);
        buttonHelp.setOnAction((ActionEvent e) -> {
            Alert alert = new Alert(AlertType.INFORMATION,
                    "Arrow keys      Move camera around focal point"
                    + System.lineSeparator()
                    + "Double-click    Set focal point & zoom in"
                    + System.lineSeparator()
                    + "-                       Zoom out"
                    + System.lineSeparator()
                    + "=/+                  Zoom in"
                    + System.lineSeparator()
                    + "PgUp               Elevate objects"
                    + System.lineSeparator()
                    + "PgDn               Drop objects"
                    + System.lineSeparator()
                    + "Esc                   Reset view"
            );
            alert.setTitle("Display controls");
            alert.setHeaderText("Keys to control display");
            alert.showAndWait();
        });

        gameControls.getChildren().addAll(buttonPause, buttonConsole, buttonHelp);
    }

    public void createSpeciesView() {
        bottom.setPadding(new Insets(10)); // Set all sides to 10
        bottom.setSpacing(8);              // Gap between nodes

        bottom.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        bottom.setPrefWidth(200);

        speciesView.setCellFactory((ListView<AlienSpeciesForDisplay> list) -> new SpeciesListCell());
        speciesView.setStyle("-fx-background-color: black;");
    }

}
