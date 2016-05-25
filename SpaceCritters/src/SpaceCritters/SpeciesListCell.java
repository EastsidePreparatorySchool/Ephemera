/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package SpaceCritters;

import gameengineinterfaces.GameCommand;
import gameengineinterfaces.GameCommandCode;
import gameengineinterfaces.GameElementSpec;
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
                BooleanProperty observable = new SimpleBooleanProperty(item != null?item.isOn():false);
                observable.addListener((obs, wasSelected, isNowSelected) -> {
                    item.setOn(isNowSelected);
                    GameElementSpec element = new GameElementSpec("ALIEN", item.domainName, item.packageName, item.className, null);
                    if (item.gameShell.field.totalTurnCounter > 0) {
                        if (isNowSelected) {
                            item.gameShell.engine.queueCommand(new GameCommand(GameCommandCode.AddElement, element));
                        } else {
                            //iter.remove(); // this would remove it from the display list
                            item.gameShell.engine.queueCommand(new GameCommand(GameCommandCode.KillElement, element));
                        }
                    }
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
            if (s.startsWith("ephemera.eastsideprep.org:stockaliens:")) {
                s = s.replace("ephemera.eastsideprep.org:stockaliens:", "System:");
            }
            setText(s);
        } else {
            this.setStyle("-fx-background-color: black; -fx-text-fill:black;");
        }
    }

}
