package ru.nsu.vorobev.chat.client.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.nsu.vorobev.chat.client.model.EventHandle;
import ru.nsu.vorobev.chat.client.model.Model;
import ru.nsu.vorobev.chat.client.model.exceptions.SocketException;
import ru.nsu.vorobev.chat.network.connection.UserWithSameName;

import java.io.IOException;
import java.util.Objects;

public class MenuController extends MenuView {

    @FXML
    private TextField portField;
    @FXML
    private TextField ipField;

    @FXML
    private TextField nickField;

    public void setModel(Model model) {
        this.model = model;
        model.setListener(this);
    }

    @FXML
    protected void OnBtnClick() throws IOException {
        String ip = ipField.getText();
        if (ip.chars().filter(c -> c == '.').count() != 3) {
            model.sendEvent(EventHandle.BAD_IP_INPUT);
            return;
        }
        int pos = 0;
        for (int i = 0; i < 4; i++) { // in IPv4 4 numbers 0-255 delimited by points '.'
            int index;
            if (i != 3) {
                index = ip.indexOf('.', pos);
            } else {
                index = ip.length();
            }
            int num;
            try {
                num = Integer.parseInt(ip.substring(pos, index));
                if (num > 255 || num < 0 || num < 10 && index - pos != 1 || num > 10 && num < 100 && index - pos != 2) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                model.sendEvent(EventHandle.BAD_IP_INPUT);
                return;
            }
            pos = index + 1;
        }
        model.setIpAddress(ip);
        int port;
        try {
            port = Integer.parseInt(portField.getText());
            if (port < 0 || port > 65535) { // ports value from 0 to 2^16-1
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            model.sendEvent(EventHandle.BAD_PORT_INPUT);
            return;
        }
        model.setPort(port);

        String nickname = nickField.getText();

        if (nickname.length() > Model.maxLengthOfName || nickname.length() == 0) {
            model.sendEvent(EventHandle.BIG_NICKNAME);
            return;
        }
        model.setName(nickname);
        // try open chat...
        try{
            model.openConnection();
        } catch (SocketException ex){
            model.sendEvent(EventHandle.SOCKET_ERROR);
            return;
        } catch (UserWithSameName ignored){
            model.sendEvent(EventHandle.USER_WITH_SAME_NAME);
            return;
        }

        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(Menu.class.getResource("chatView.fxml")));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Чат");
        stage.setResizable(true); // wow! It's resizable
        stage.setScene(new Scene(root,600,400));
        ((ChatController)loader.getController()).setModel(model);
        stage.show();
        stage.setOnCloseRequest(windowEvent -> {
            model.close();
            Platform.exit();
        });
        Stage stageM = (Stage) nickField.getScene().getWindow();
        stageM.close();



        System.out.println("Open chat");
    }
}
