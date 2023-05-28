package ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory;

import org.w3c.dom.Element;

public class MessageEvent implements Operable{

    @Override
    public void doOperation(IContext context) {
        Element messageElem = (Element) context.getDocument().getElementsByTagName("message").item(0);
        Element nameElem = (Element) context.getDocument().getElementsByTagName("name").item(0);
        if (messageElem == null || nameElem == null) {
            return;
        }
        context.getModel().onModelReceive(nameElem.getTextContent() + ": " + messageElem.getTextContent());
    }
}
