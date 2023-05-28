package ru.nsu.vorobev.chat.server.protocolrealisation.xml.factory;

import org.w3c.dom.Element;
import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.server.protocolrealisation.xml.Utils;

public class LogoutOp implements Operable{
    @Override
    public void doOperation(IContext context, TCPConnection connection) {
        Element sessionElem = (Element) context.getDocument().getElementsByTagName("session").item(0);
        int id = Integer.parseInt(sessionElem.getTextContent());
        if (Utils.checkIDAndSendIfWrong(context,connection, id, "logout", "Wrong session ID")) {
            return;
        }
        Utils.sendSuccess(context,connection, "logout");
    }
}
