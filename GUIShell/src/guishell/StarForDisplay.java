/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guishell;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 *
 * @author gunnar
 */
public class StarForDisplay {

    int x;
    int y;
    String name;
    int energy;
    int lastSize;
    int lastTime;

    public StarForDisplay(int x, int y, String name, int energy) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.energy = energy;
        this.lastSize = 0;
        this.lastTime = 0;
    }

    public void draw(GraphicsContext gc, int cellWidth, int cellHeight) {

        
        int flicker = ((int) Math.abs(System.nanoTime())) % 8;
        flicker *= Math.sqrt(energy);
        flicker /= 16;
        flicker += 1;

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(0.5);
        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.strokeLine(x * cellWidth + 0.5, y * cellHeight - flicker + 0.5, x * cellWidth + 0.5, y * cellHeight + flicker + 0.5);
        gc.strokeLine(x * cellWidth -flicker + 0.5, y * cellHeight  + 0.5, x * cellWidth + flicker+ 0.5, y * cellHeight  + 0.5);
        gc.setLineWidth(1.0);

    }

}
