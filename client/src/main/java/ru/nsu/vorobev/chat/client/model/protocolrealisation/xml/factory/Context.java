package ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory;

import org.w3c.dom.Document;
import ru.nsu.vorobev.chat.client.model.Model;
import ru.nsu.vorobev.chat.network.connection.TCPConnection;

public class Context implements IContext{

    private Document doc;
    private final Model model;
    private final TCPConnection connection;

    public Context(Model model, TCPConnection connection){
        this.model = model;
        this.connection = connection;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }


    @Override
    public Document getDocument() {
        return doc;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public TCPConnection getConnection() {
        return connection;
    }
}
