package ru.nsu.vorobev.chat.client.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import ru.nsu.vorobev.chat.client.model.EventHandle;
import ru.nsu.vorobev.chat.client.model.Model;
import ru.nsu.vorobev.chat.client.model.ModelListener;

public class ChatView implements ModelListener {

    protected Model model;
    @FXML
    private TextArea chatField;
    @FXML
    private TextArea usersField;
    @Override
    public void onModelChanged(EventHandle handle) {

    }

    @Override
    public void onModelReceived(String msg) {
        Platform.runLater(() -> chatField.appendText(msg + "\n"));
    }
}
