package ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.nsu.vorobev.chat.client.model.EventHandle;

public class GetUsersListError implements Operable{
    @Override
    public void doOperation(IContext context) {
        NodeList nodeList = context.getDocument().getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                context.getModel().setMsg(nodeList.item(i).getTextContent());
                break;
            }
        }
        context.getModel().onModelChange(EventHandle.NAMES_REQ_FAILED);
    }
}
