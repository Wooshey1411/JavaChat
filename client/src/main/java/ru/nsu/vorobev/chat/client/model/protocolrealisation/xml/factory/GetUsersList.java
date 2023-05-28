package ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.nsu.vorobev.chat.client.model.EventHandle;

import java.util.ArrayList;
import java.util.List;

public class GetUsersList implements Operable{
    @Override
    public void doOperation(IContext context) {
        Element listUsersElem = (Element) context.getDocument().getElementsByTagName("listusers").item(0);
        if (listUsersElem == null) {
            return;
        }
        NodeList nodeList = listUsersElem.getChildNodes();
        List<String> users = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element userName = (Element) nodeList.item(i).getFirstChild();
                users.add(userName.getTextContent());
            }
        }
        context.getModel().setUsersList(users);
        context.getModel().onModelChange(EventHandle.NAMES_REQ_SUCCESSFUL);
    }
}
