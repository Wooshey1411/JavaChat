module ru.nsu.vorobev.chat.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires network;
    // requires network;

    opens ru.nsu.vorobev.chat.client.gui to javafx.fxml;
    //opens ru.nsu.vorobev.chat.client.model to network;
    exports ru.nsu.vorobev.chat.client.gui;
    exports ru.nsu.vorobev.chat.client.model;
}