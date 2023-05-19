package ru.nsu.vorobev.chat.client.gui;

import javafx.scene.control.Alert;
import ru.nsu.vorobev.chat.client.model.EventHandle;
import ru.nsu.vorobev.chat.client.model.Model;
import ru.nsu.vorobev.chat.client.model.ModelListener;

public class MenuView implements ModelListener {

    protected Model model;

    private void makeAlert(String message){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning Dialog");
        alert.setContentText(message);
        alert.showAndWait();
    }
    @Override
    public void onModelChanged(EventHandle handle) {
        switch (handle){
            case BAD_IP_INPUT -> makeAlert("Bad ip format!");
            case BAD_PORT_INPUT -> makeAlert("Bad port!");
            case BIG_NICKNAME -> makeAlert("Count of symbols in nickname must be more than 0 and less than " + Model.maxLengthOfName);
            case SOCKET_ERROR -> makeAlert("Server not available!");
            case USER_WITH_SAME_NAME -> makeAlert("Exist user with same nickname!");
        }
    }

    @Override
    public void onModelReceived(String msg) {
        System.out.println(msg);
    }
}
