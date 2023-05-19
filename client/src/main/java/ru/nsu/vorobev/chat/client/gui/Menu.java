package ru.nsu.vorobev.chat.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.nsu.vorobev.chat.client.model.Model;

public class Menu extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Menu.class.getResource("menuView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 275, 145);
        stage.setResizable(false);
        stage.setTitle("Вход в чат");
        stage.setScene(scene);
        stage.show();

        Model model = new Model();

        ((MenuController) fxmlLoader.getController()).setModel(model);

        stage.setOnCloseRequest(windowEvent -> {
          //  model.close();
           // Platform.exit();
        });
    }




    public static void main(String[] args) {
        launch();
    }

}
