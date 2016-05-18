/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package SpaceCritters;

import gameengineinterfaces.GameCommand;
import gameengineinterfaces.GameCommandCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;


/**
 *
 * @author gmein, stolen from StackOverflow member skiwi
 */
public class ConsolePane extends BorderPane {

    public final CheckBox chatter = new CheckBox("Aliens");
    public final TextField filter = new TextField();
    private final TextArea textArea = new TextArea();
    private final TextField textField = new TextField();

    private final List<String> history = new ArrayList<>();
    private int historyPointer = 0;
    private int msgCounter = 0;
    private final SpaceCritters gameShell;

    private Consumer<String> onMessageReceivedHandler;

    public ConsolePane(SpaceCritters gameShellInstance) {
        
        this.gameShell = gameShellInstance;

        setTop(createControls());
        //setBottom(textField);
        setCenter(textArea);

        textArea.setEditable(false);
        textArea.setStyle("-fx-control-inner-background: black;");
        textField.setStyle("-fx-background-color: black; -fx-text-fill: blue;");
        this.setStyle("-fx-background-color: black;");

        this.textField.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            switch (keyEvent.getCode()) {
                case ENTER:
                    String text = textField.getText();
                    textArea.appendText(text + System.lineSeparator());
                    history.add(text);
                    historyPointer++;
                    if (onMessageReceivedHandler != null) {
                        onMessageReceivedHandler.accept(text);
                    }
                    textField.clear();
                    break;
                case UP:
                    if (historyPointer == 0) {
                        break;
                    }
                    historyPointer--;
                    Utilities.runSafe(() -> {
                        textField.setText(history.get(historyPointer));
                        textField.selectAll();
                    });
                    break;
                case DOWN:
                    if (historyPointer == history.size() - 1) {
                        break;
                    }
                    historyPointer++;
                    Utilities.runAndWait(() -> {
                        textField.setText(history.get(historyPointer));
                        textField.selectAll();
                    });
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        textField.requestFocus();
    }

    private  void setOnMessageReceivedHandler(final Consumer<String> onMessageReceivedHandler) {
        this.onMessageReceivedHandler = onMessageReceivedHandler;
    }

    public void clear() {
        Utilities.runSafe(() -> textArea.clear());
        history.clear();
    }

    public void print(final String text) {
        Objects.requireNonNull(text, "text");
        Utilities.runAndWait(() -> textArea.appendText(text));
    }

    public void println(final String text) {
        if (++msgCounter > 1000) {
            msgCounter = 0;
            clear();
        }
        Objects.requireNonNull(text, "text");
        Utilities.runAndWait(() -> textArea.appendText(text + System.lineSeparator()));
    }

    public void println() {
        Utilities.runAndWait(() -> textArea.appendText(System.lineSeparator()));
    }
    
    
    private HBox createControls () {
     
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(15, 12, 15, 12));
        hBox.setSpacing(10);  
        hBox.setStyle("-fx-background-color: black;");
        hBox.setAlignment(Pos.CENTER_LEFT);

    
        chatter.setStyle("-fx-text-fill: white;");
        chatter.setOnAction((e) -> {
            gameShell.engine.queueCommand(
                    new GameCommand(GameCommandCode.SetConstant, "CHATTER", chatter.isSelected() ? "true" : "false"));
        });

        filter.setEditable(true);
        filter.setOnAction((e) -> gameShell.field.setFilter(filter.getText().trim().equals("") ? null:filter.getText()));

        Label l = new Label("   Filter:");
        l.setStyle("-fx-text-fill: white;");

        Button b1 = new Button("Set");
        b1.setOnAction((e) -> gameShell.field.setFilter(filter.getText().trim().equals("") ? null:filter.getText()));

        hBox.getChildren().addAll(chatter, l, filter, b1);

        return hBox;

    }
}
