/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ephemerawindowsshell;

import javafx.scene.control.ListCell;

/**
 *
 * @author gmein
 */
public class SpeciesListCell extends ListCell<AlienSpecies> {
    @Override
    public void updateItem(AlienSpecies item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            this.setStyle(item.getStyle());
            setText(item.toString());  
        } else
            this.setStyle("-fx-background-color: black; -fx-text-fill:black;");
    }
}
    

