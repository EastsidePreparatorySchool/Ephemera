/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ephemerawindowsshell;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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

    static Color[] alienColors = {Color.LIGHTBLUE, Color.YELLOW, Color.LIGHTPINK, Color.LIGHTGREEN, Color.ORANGE};
    static String[] alienStyles = {"lightblue", "yellow", "lightpink", "lightgreen", "orange"};
    static int colorCount = 0;

    AlienSpecies(String speciesName) {
        this.speciesName = speciesName;
        this.count = 1; // if we create this object, there is at least one alien
        this.color = alienColors[colorCount % alienColors.length];
        this.style = alienStyles[colorCount % alienColors.length];
        colorCount++;
    }

    @Override
    public String toString() {
        return speciesName + ": " + count;
    }

    public String getStyle() {
        return "-fx-text-fill: " + style + "; -fx-background-color: black;";
    }
}