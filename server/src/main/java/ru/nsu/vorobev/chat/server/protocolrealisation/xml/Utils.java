package ru.nsu.vorobev.chat.server.protocolrealisation.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.server.protocolrealisation.User;
import ru.nsu.vorobev.chat.server.protocolrealisation.xml.factory.IContext;

public class Utils {
    static public String getMessage(IContext context, String message, String sender){
        Document BCMessage = context.getBuilder().newDocument();
        Element rootElement = BCMessage.createElement("event");
        rootElement.setAttribute("name", "message");
        BCMessage.appendChild(rootElement);
        Element messageElement = BCMessage.createElement("message");
        messageElement.setTextContent(message);
        Element nameElement = BCMessage.createElement("name");
        nameElement.setTextContent(sender);
        rootElement.appendChild(messageElement);
        rootElement.appendChild(nameElement);
        context.getStringWriter().getBuffer().setLength(0);
        context.getWriter().write(BCMessage, context.getLSOutput());
        return context.getStringWriter().toString();
    }

    static public synchronized boolean checkIDAndSendIfWrong(IContext context,TCPConnection tcpConnection, int ID, String attribute, String reasonS) {
        Document ans = context.getBuilder().newDocument();
        boolean IDFound = false;
        for (User user : context.getServer().getUsers().values()) {
            if (user.getID() == ID) {
                IDFound = true;
                break;
            }
        }
        if (!IDFound) {
            Element rootElement = ans.createElement("error");
            ans.appendChild(rootElement);
            Element reason = ans.createElement("reason");
            reason.setAttribute("name", attribute);
            reason.setTextContent(reasonS);
            rootElement.appendChild(reason);
            context.getStringWriter().getBuffer().setLength(0);
            context.getWriter().write(ans, context.getLSOutput());
            tcpConnection.sendData(context.getStringWriter().toString().getBytes());
            return true;
        }
        return false;
    }

    static public synchronized void sendSuccess(IContext context,TCPConnection tcpConnection, String attribute) {
        Document ans = context.getBuilder().newDocument();
        Element rootElement = ans.createElement("success");
        rootElement.setAttribute("name", attribute);
        ans.appendChild(rootElement);
        context.getStringWriter().getBuffer().setLength(0);
        context.getWriter().write(ans, context.getLSOutput());
        String ansDirty = context.getStringWriter().toString() + "</success>";
        int index = ansDirty.indexOf('/');
        String ansClear = ansDirty.substring(0,index) + ansDirty.substring(index+1);
        System.out.println(ansClear);
        tcpConnection.sendData(ansClear.getBytes());
    }

    static public synchronized void broadCastMessage(IContext context,String msg) {
        //  System.out.println("Broadcast: " + object.toString());
        for (TCPConnection connection : context.getServer().getUsers().keySet()) {
            connection.sendData(msg.getBytes());
        }
    }
}
