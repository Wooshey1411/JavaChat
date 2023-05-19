package ru.nsu.vorobev.chat.client.gui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import ru.nsu.vorobev.chat.client.model.Model;

public class ChatController extends ChatView {

    @FXML
    private TextArea messageField;
    public void setModel(Model model) {
        this.model = model;
        model.setListener(this);
    }


    @FXML
    protected void OnButtonClick(){
        if(messageField.getText().isEmpty()){
            return;
        }
        String msg = messageField.getText();
        messageField.clear();
        model.sendMsg(model.getName() + ": " + msg);
    }

}
