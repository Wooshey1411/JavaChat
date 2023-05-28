package ru.nsu.vorobev.chat.server.protocolrealisation.xml;

import org.w3c.dom.Document;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import ru.nsu.vorobev.chat.server.ChatServer;
import ru.nsu.vorobev.chat.server.protocolrealisation.xml.factory.IContext;

import javax.xml.parsers.DocumentBuilder;
import java.io.StringWriter;

public class Context implements IContext {

    private Document document;
    private final ChatServer server;
    private final DocumentBuilder builder;
    private final LSOutput output;
    private final StringWriter stringWriter;
    private final LSSerializer writer;

    public Context(ChatServer server,DocumentBuilder builder,LSOutput output,StringWriter stringWriter,LSSerializer writer){
        this.server = server;
        this.builder = builder;
        this.output = output;
        this.stringWriter =stringWriter;
        this.writer = writer;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    @Override
    public Document getDocument() {
        return document;
    }

    @Override
    public ChatServer getServer() {
        return server;
    }

    @Override
    public DocumentBuilder getBuilder() {
        return builder;
    }

    @Override
    public LSOutput getLSOutput() {
        return output;
    }

    @Override
    public StringWriter getStringWriter() {
        return stringWriter;
    }

    @Override
    public LSSerializer getWriter() {
        return writer;
    }

}
