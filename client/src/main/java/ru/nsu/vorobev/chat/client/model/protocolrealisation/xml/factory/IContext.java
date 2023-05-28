package ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory;

import org.w3c.dom.Document;
import ru.nsu.vorobev.chat.client.model.Model;
import ru.nsu.vorobev.chat.network.connection.TCPConnection;

public interface IContext {
    Document getDocument();
    Model getModel();
    TCPConnection getConnection();
}
