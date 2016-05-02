/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 *
 * @author gunnar
 */
public class PlanetForDisplay {
    int x;
    int y;
    String name;
    double energy;
    double tech;
    
    public PlanetForDisplay(int x, int y, String name, double energy, double tech) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.energy = energy;
        this.tech = tech;
    }
    
    public void draw(GraphicsContext gc, int cellWidth, int cellHeight, int heightPX) {
        double offset = cellWidth == 3? 1.0:0.5;
        gc.setLineWidth(cellWidth == 3? 1.0:0.5);
        
        gc.setLineCap(StrokeLineCap.ROUND);

        gc.setStroke(Color.LIGHTBLUE);
             gc.strokeLine(x * cellWidth + 1.5 + offset,
                    heightPX - y * cellHeight  + 1.5,
                    x * cellWidth + 1.5 + offset,
                    heightPX - y * cellHeight  + 1.5);
        gc.setLineWidth(1.0);

    }
    
}
