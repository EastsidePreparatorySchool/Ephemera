/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guishell;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.paint.Color;

/**
 *
 * @author gmein
 */
public class AlienSpecies {

    String speciesName;
    int count;
    Color color;
    String style;
    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty on = new SimpleBooleanProperty();

    static Color[] alienColors = {Color.LIGHTBLUE, Color.YELLOW, Color.LIGHTPINK, Color.LIGHTGREEN, Color.ORANGE};
    static String[] alienStyles = {"lightblue", "yellow", "lightpink", "lightgreen", "orange"};
    static int colorCount = 0;

    AlienSpecies(String speciesName) {
        this.speciesName = speciesName;
        this.count = 1; // if we create this object, there is at least one alien
        this.color = alienColors[colorCount % alienColors.length];
        this.style = alienStyles[colorCount % alienColors.length];
        colorCount++;
        
        on.setValue(true);

    }

    public final StringProperty nameProperty() {
        return this.name;
    }

    public final String getName() {
        return this.toString();
    }

    public final void setName(final String name) {
        this.nameProperty().set(name);
    }

    public final BooleanProperty onProperty() {
        return this.on;
    }

    public final boolean isOn() {
        return this.onProperty().get();
    }

    public final void setOn(final boolean on) {
        this.onProperty().set(on);
    }

   @Override
    public String toString() {
        return speciesName + ": " + count;
    }

    public String getStyle() {
        return "-fx-text-fill: " + style + "; -fx-background-color: black;";
    }
}
