package ru.nsu.vorobev.chat.client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ru.nsu.vorobev.chat.client.model.Model;
import ru.nsu.vorobev.chat.client.model.configparser.BadConfigException;
import ru.nsu.vorobev.chat.client.model.configparser.ConfigParser;
import ru.nsu.vorobev.chat.client.model.configparser.NoConfigFileException;

public class Menu extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Menu.class.getResource("menuView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 275, 145);
        stage.setResizable(false);
        stage.setTitle("Вход в чат");
        stage.setScene(scene);
        Model model = null;
        try(ConfigParser parser = new ConfigParser()){
            String protocol = parser.getStrByName("protocol");
            model = new Model(protocol);
        } catch (BadConfigException | NoConfigFileException ex){

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setContentText("Config file: " + ex.getMessage());
            alert.showAndWait();
            Platform.exit();

        }



        if(model != null) {
            stage.show();
            ((MenuController) fxmlLoader.getController()).setModel(model);
        }

        stage.setOnCloseRequest(windowEvent -> {
           // model.close();
            Platform.exit();
        });
    }




    public static void main(String[] args) {
        launch();
    }

}
