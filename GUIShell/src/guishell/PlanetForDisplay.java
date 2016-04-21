/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guishell;

import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author gunnar
 */
public class PlanetForDisplay {
    int x;
    int y;
    String name;
    int energy;
    int tech;
    
    public PlanetForDisplay(int x, int y, String name, int energy, int tech) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.energy = energy;
        this.tech = tech;
    }
    
    public void draw(GraphicsContext gc, int cellWidth, int cellHeight) {
        
    }
    
}
