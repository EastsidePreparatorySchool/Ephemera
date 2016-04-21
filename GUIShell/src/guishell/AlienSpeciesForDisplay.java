/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guishell;

import gameengineinterfaces.AlienSpec;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

/**
 *
 * @author gmein
 */
public class AlienSpeciesForDisplay {

    String speciesName;
    String domainName;
    String packageName;
    String className;

    int count;
    Color color;
    String style;
    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty on = new SimpleBooleanProperty();

    static Color[] alienColors = {Color.LIGHTBLUE, Color.YELLOW, Color.LIGHTPINK, Color.LIGHTGREEN, Color.ORANGE};
    static String[] alienStyles = {"lightblue", "yellow", "lightpink", "lightgreen", "orange"};
    static int colorCount = 0;

    AlienSpeciesForDisplay(String speciesName) {
        this.speciesName = speciesName;
        this.count = 1; // if we create this object, there is at least one alien
        this.color = alienColors[colorCount % alienColors.length];
        this.style = alienStyles[colorCount % alienColors.length];
        colorCount++;

        on.setValue(true);

    }

    AlienSpeciesForDisplay(AlienSpec as) {
        this.domainName = as.domainName;
        this.packageName = as.packageName;
        this.className = as.className;
        this.speciesName = as.getFullSpeciesName();
        this.count = 1; // if we create this object, there is at least one alien
        this.color = alienColors[colorCount % alienColors.length];
        this.style = alienStyles[colorCount % alienColors.length];
        colorCount++;

        //on.setValue(true);
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
        if (on) {
            System.out.println("Setting property");
        }
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
