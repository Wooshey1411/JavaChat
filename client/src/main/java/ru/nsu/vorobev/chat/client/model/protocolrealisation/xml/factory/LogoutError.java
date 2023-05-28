package ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory;

import org.w3c.dom.Element;
import ru.nsu.vorobev.chat.client.model.EventHandle;

public class LogoutError implements Operable{
    @Override
    public void doOperation(IContext context) {
        Element reasonElem = (Element) context.getDocument().getElementsByTagName("reason").item(0);
        context.getModel().setError(reasonElem.getTextContent());
        context.getModel().onModelChange(EventHandle.ERROR);
    }
}
