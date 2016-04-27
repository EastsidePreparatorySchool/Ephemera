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
    double energy;
    int flicker;
    long lastTime;
    int lastFlicker;

    public StarForDisplay(int x, int y, String name, double energy) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.energy = energy;
        this.flicker = 0;
        this.lastTime = 0;
        this.lastFlicker = 0;
    }

    public void draw(GraphicsContext gc, int cellWidth, int cellHeight) {

        int maxFlicker = 12;
        gc.setLineWidth(1.0);
        gc.setLineCap(StrokeLineCap.SQUARE);

        if ((System.nanoTime() - lastTime) > 200000000) {
            gc.setStroke(Color.BLACK);
            gc.strokeLine(x * cellWidth + 1.5 + cellWidth/2,
                    y * cellHeight - lastFlicker + 1.5 + cellHeight/2,
                    x * cellWidth + 1.5 + cellWidth/2,
                    y * cellHeight + lastFlicker + 1.5 + cellHeight/2);
            gc.strokeLine(x * cellWidth - lastFlicker + 1.5 + cellWidth / 2,
                    y * cellHeight + 1.5 + cellHeight / 2,
                    x * cellWidth + lastFlicker + 1.5 + cellWidth / 2,
                    y * cellHeight + 1.5 + cellHeight / 2);
            flicker = ((int) Math.abs(System.nanoTime())) % maxFlicker;
            flicker *= Math.sqrt(energy);
            flicker /= 16;
            flicker += 1;
            lastTime = System.nanoTime();
            lastFlicker = flicker;
        }

        gc.setStroke(Color.WHITE);
             gc.strokeLine(x * cellWidth + 1.5 + cellWidth/2,
                    y * cellHeight - lastFlicker + 1.5 + cellHeight/2,
                    x * cellWidth + 1.5 + cellWidth/2,
                    y * cellHeight + lastFlicker + 1.5 + cellHeight/2);
            gc.strokeLine(x * cellWidth - lastFlicker + 1.5 + cellWidth / 2,
                    y * cellHeight + 1.5 + cellHeight / 2,
                    x * cellWidth + lastFlicker + 1.5 + cellWidth / 2,
                    y * cellHeight + 1.5 + cellHeight / 2);
        gc.setLineWidth(1.0);

    }

}
