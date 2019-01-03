/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.spacecritters;

import org.eastsideprep.spacecritters.alieninterfaces.AlienShapeFactory;
import org.eastsideprep.spacecritters.gameengineinterfaces.AlienSpec;
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

    SpaceCritters gameShell;
    String speciesName;
    String domainName;
    String packageName;
    String className;
    int id;
    AlienShapeFactory asf;
    boolean instantiate;

    int count;
    Color color;
    String style;
    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty on = new SimpleBooleanProperty();

    static Color[] alienColors = {Color.LIGHTBLUE, Color.YELLOW, Color.LIGHTPINK, Color.LIGHTGREEN, Color.ORANGE, Color.WHITE};
    static String[] alienStyles = {"lightblue", "yellow", "lightpink", "lightgreen", "orange", "white"};
    static int colorCount = 0;

    AlienSpeciesForDisplay(SpaceCritters gameShellInstance, String speciesName, int id, boolean instantiate) {
        this.gameShell = gameShellInstance;
        this.speciesName = speciesName;
        this.count = 0;
        this.id = id;
        this.color = alienColors[colorCount % alienColors.length];
        this.style = alienStyles[colorCount % alienColors.length];
        colorCount++;
        this.asf = null;
        this.instantiate = instantiate;

        on.setValue(instantiate);

    }

    AlienSpeciesForDisplay(SpaceCritters gameShellInstance, AlienSpec as, AlienShapeFactory asf, boolean instantiate) {
        this.gameShell = gameShellInstance;
        this.domainName = as.domainName;
        this.packageName = as.packageName;
        this.className = as.className;
        this.speciesName = as.getFullSpeciesName();
        this.id = as.speciesID;
        this.count = 0;
        this.color = alienColors[colorCount % alienColors.length];
        this.style = alienStyles[colorCount % alienColors.length];
        colorCount++;
        this.asf = asf;

        on.setValue(instantiate);
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
