package ru.nsu.vorobev.chat.client.gui;

import javafx.fxml.FXML;
import ru.nsu.vorobev.chat.client.model.Model;

public class ChatController extends ChatView {

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
        sendBtn.setDisable(true);
        //messageField.clear();

        model.sendMsg(msg);
    }

}
