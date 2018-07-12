/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.spacecritters;

import org.eastsideprep.spacecritters.gameengineinterfaces.GameCommand;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameCommandCode;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameElementSpec;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
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
                BooleanProperty observable = new SimpleBooleanProperty(item != null ? item.isOn() : false);
                observable.addListener((obs, wasSelected, isNowSelected) -> {
                    item.setOn(isNowSelected);
                    GameElementSpec element = new GameElementSpec("ALIEN", item.domainName, item.packageName, item.className, null);
                    //if (item.gameShell.fieldGrid.totalTurnCounter > 0) {
                        if (isNowSelected) {
                            item.gameShell.engine.queueCommand(new GameCommand(GameCommandCode.AddElement, element));
                            System.out.println("Adding one " + item.className);
                        } else {
                            //iter.remove(); // this would remove it from the display list
                            item.gameShell.engine.queueCommand(new GameCommand(GameCommandCode.KillElement, element));
                            System.out.println("Killing all " + item.className);
                        }
                    //}
                });
                return observable;
            }
        });

    }

    @Override
    public void updateItem(AlienSpeciesForDisplay item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null && !empty) {
            this.setStyle(item.getStyle());
            String s = item.toString();
            //if (s.startsWith("org.eastsideprep.spacecritters:org.eastsideprep.spacecritters.stockelements:")) {
                s = s.replace("org.eastsideprep.spacecritters:org.eastsideprep.spacecritters.stockelements:", "System:");
            //}
            setText(s);
        } else {
            this.setStyle("-fx-background-color: black; -fx-text-fill:black;");
        }
    }

}
