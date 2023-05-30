package ru.nsu.vorobev.chat.server.protocolrealisation.xml.factory;

import org.w3c.dom.Element;
import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.server.ChatServer;
import ru.nsu.vorobev.chat.server.protocolrealisation.Log;
import ru.nsu.vorobev.chat.server.protocolrealisation.MessageType;
import ru.nsu.vorobev.chat.server.protocolrealisation.User;
import ru.nsu.vorobev.chat.server.protocolrealisation.xml.Utils;

public class MessageOp implements Operable{
    @Override
    public void doOperation(IContext context, TCPConnection connection) {
        Element msgElem = (Element) context.getDocument().getElementsByTagName("message").item(0);
        Element sessionElem = (Element) context.getDocument().getElementsByTagName("session").item(0);
        if (msgElem == null || sessionElem == null) {
            return;
        }
        int ID = Integer.parseInt(sessionElem.getTextContent().strip());
        if (Utils.checkIDAndSendIfWrong(context,connection, ID, "message", "wrong session ID for send message of users")) {
            Log.log(Log.getTime() + ":Client try to send message with nonexistent ID. TCPConnection:" + connection,Log.TypeOfLoggers.INFO);
            return;
        }
        String senderName = "";
        for (User user : context.getServer().getUsers().values()) {
            if (user.getID() == ID) {
                senderName = user.getNickname();
                break;
            }
        }
        String msg = Utils.getMessage(context,msgElem.getTextContent(),senderName);
        if (context.getServer().getMessages().size() == ChatServer.maxHistoryLen) {
            context.getServer().getMessages().remove(0);
        }
        context.getServer().getMessages().add(new MessageType(msgElem.getTextContent(),senderName));
        Utils.broadCastMessage(context,msg);
        Log.log(Log.getTime() + ":Client " + senderName + " with ID=" + ID + " send message \"" + msgElem.getTextContent() + "\"",Log.TypeOfLoggers.INFO);
        Utils.sendSuccess(context,connection, "message");
    }
}
