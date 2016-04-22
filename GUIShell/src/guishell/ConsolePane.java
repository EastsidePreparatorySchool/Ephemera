/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guishell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author gmein, stolen from StackOverflow member skiwi
 */
public class ConsolePane extends BorderPane {

    protected final TextArea textArea = new TextArea();
    protected final TextField textField = new TextField();

    protected final List<String> history = new ArrayList<>();
    protected int historyPointer = 0;
    private int msgCounter = 0;

    private Consumer<String> onMessageReceivedHandler;

    public ConsolePane() {
        textArea.setEditable(false);
        textArea.setStyle("-fx-control-inner-background: black;");

        textField.setStyle("-fx-background-color: black; -fx-text-fill: blue;");

        this.setStyle("-fx-background-color: black;");
        setCenter(textArea);

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
        setBottom(textField);
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        textField.requestFocus();
    }

    public void setOnMessageReceivedHandler(final Consumer<String> onMessageReceivedHandler) {
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
        if (++msgCounter > 500) {
            msgCounter = 0;
            clear();
        }
        Utilities.runAndWait(() -> textArea.appendText(System.lineSeparator()));
    }
}
