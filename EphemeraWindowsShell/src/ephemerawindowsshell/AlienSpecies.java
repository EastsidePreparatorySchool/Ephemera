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

    String packageName;
    String className;
    int count;
    Color color;

    static Color[] alienColors = {Color.BLUE, Color.RED, Color.YELLOW, Color.MAGENTA, Color.GREEN, Color.ORANGE};
    static int colorCount;

    AlienSpecies(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
        this.count = 1; // if we create this object, there is at least one alien
        this.color = alienColors[colorCount++ % alienColors.length];
    }

    @Override
    public String toString() {
        return packageName + ":" + className + ": " + count;
    }

}
