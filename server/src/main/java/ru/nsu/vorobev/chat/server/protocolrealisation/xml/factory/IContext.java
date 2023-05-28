package ru.nsu.vorobev.chat.server.protocolrealisation.xml.factory;

import org.w3c.dom.Document;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import ru.nsu.vorobev.chat.server.ChatServer;

import javax.xml.parsers.DocumentBuilder;
import java.io.StringWriter;

public interface IContext {
    Document getDocument();
    ChatServer getServer();
    DocumentBuilder getBuilder();
    LSOutput getLSOutput();
    StringWriter getStringWriter();
    LSSerializer getWriter();
}
