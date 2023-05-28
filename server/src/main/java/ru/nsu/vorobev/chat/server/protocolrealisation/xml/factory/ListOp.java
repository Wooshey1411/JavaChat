package ru.nsu.vorobev.chat.server.protocolrealisation.xml.factory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.server.protocolrealisation.Log;
import ru.nsu.vorobev.chat.server.protocolrealisation.User;
import ru.nsu.vorobev.chat.server.protocolrealisation.xml.Utils;

class ListOp implements Operable{

    @Override
    public void doOperation(IContext context, TCPConnection connection) {
        NodeList nodeList = context.getDocument().getChildNodes();
        int ID = -1;
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                ID = Integer.parseInt(nodeList.item(i).getTextContent());
                break;
            }
        }
        if (Utils.checkIDAndSendIfWrong(context,connection, ID, "list", "wrong session ID for get list of users")) {
            Log.log(Log.getTime() + ":Client send request of names with nonexistent ID. TCPConnection:" + connection,Log.TypeOfLoggers.INFO);
            return;
        }
        Document ans = context.getBuilder().newDocument();
        Element rootElement = ans.createElement("success");
        rootElement.setAttribute("name", "list");
        ans.appendChild(rootElement);
        Element listusersElement = ans.createElement("listusers");
        rootElement.appendChild(listusersElement);
        for (User user : context.getServer().getUsers().values()) {
            Element userElement = ans.createElement("user");
            Element nameElement = ans.createElement("name");
            nameElement.setTextContent(user.getNickname());
            Element typeElement = ans.createElement("type");
            typeElement.setTextContent("" + user.getID());
            userElement.appendChild(nameElement);
            userElement.appendChild(typeElement);
            listusersElement.appendChild(userElement);
        }
        context.getStringWriter().getBuffer().setLength(0);
        context.getWriter().write(ans, context.getLSOutput());
        String ansS = context.getStringWriter().toString();
        connection.sendData(ansS.getBytes());
        Log.log(Log.getTime() + ":Client with ID=" + ID + " send request of names successfully",Log.TypeOfLoggers.INFO);
    }
}
