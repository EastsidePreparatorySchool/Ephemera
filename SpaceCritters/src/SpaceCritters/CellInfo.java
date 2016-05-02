/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import javafx.scene.paint.Color;

/**
 *
 * @author gmein
 */
public class CellInfo {
    String speciesName;
    public int count;
    public Color color;
    
    CellInfo(int count, Color color, String speciesName) {
        this.speciesName = speciesName;
        this.count = count;
        this.color = color;
    }
}
