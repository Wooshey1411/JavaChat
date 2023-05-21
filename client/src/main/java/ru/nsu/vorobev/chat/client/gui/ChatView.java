package ru.nsu.vorobev.chat.client.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import ru.nsu.vorobev.chat.client.model.EventHandle;
import ru.nsu.vorobev.chat.client.model.Model;
import ru.nsu.vorobev.chat.client.model.ModelListener;

import java.util.Objects;

public class ChatView implements ModelListener {

    protected Model model;
    @FXML
    private TextArea chatField;
    @FXML
    private TextArea usersField;
    @FXML
    protected Button sendBtn;

    @FXML
    protected TextArea messageField;

    private void makeAlert(String message){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning Dialog");
        alert.setContentText(message);
        alert.showAndWait();
    }

    void updateUsers(){
        Platform.runLater(() -> {
            usersField.clear();
            for (String name : model.getUsersList()){
                if(Objects.equals(name, model.getName())){
                    usersField.appendText(name + "(You)\n");
                    continue;
                }
                usersField.appendText(name + "\n");
            }
        });
    }
    @Override
    public void onModelChanged(EventHandle handle) {
        switch (handle){
            case MESSAGE_SUCCESSFUL -> {
                messageField.clear();
                sendBtn.setDisable(false);
            }
            case MESSAGE_FAILED -> {
                makeAlert(model.getMsg());
                sendBtn.setDisable(false);
            }
            case NAMES_REQ_FAILED -> makeAlert(model.getMsg());
            case NAMES_REQ_SUCCESSFUL -> updateUsers();
            case USER_LOGIN -> {
                updateUsers();
                Platform.runLater(() -> chatField.appendText("User " + model.getMsg() + " connected" + "\n"));
            }
            case USER_LOGOUT -> {
                updateUsers();
                Platform.runLater(() -> chatField.appendText("User " + model.getMsg() + " disconnected" + "\n"));
            }
        }
    }

    @Override
    public void onModelReceived(String msg) {
        Platform.runLater(() -> chatField.appendText(msg + "\n"));
    }
}
