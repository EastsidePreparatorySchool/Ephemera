/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guishell;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;

/**
 *
 * @author gmein
 */
public class SpeciesListCell extends CheckBoxListCell<AlienSpeciesForDisplay> {

    public SpeciesListCell() {
        this.setSelectedStateCallback(new Callback<AlienSpeciesForDisplay, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(AlienSpeciesForDisplay item) {
                BooleanProperty observable = new SimpleBooleanProperty(true);
                observable.addListener((obs, wasSelected, isNowSelected)
                        -> {
                    item.setOn(isNowSelected);
                    //System.out.println("Check box for " + item + " changed from " + wasSelected + " to " + isNowSelected);
                }
                );
                return observable;
            }
        });

    }

    @Override
    public void updateItem(AlienSpeciesForDisplay item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null && !empty) {
            this.setStyle(item.getStyle());
            setText(item.toString());
        } else {
            this.setStyle("-fx-background-color: black; -fx-text-fill:black;");
        }
    }

}
