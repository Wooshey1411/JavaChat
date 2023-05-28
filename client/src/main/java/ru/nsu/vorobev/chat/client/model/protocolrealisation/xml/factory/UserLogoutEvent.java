package ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory;

import org.w3c.dom.Element;
import ru.nsu.vorobev.chat.client.model.EventHandle;

import java.util.Objects;

public class UserLogoutEvent implements Operable{
    @Override
    public void doOperation(IContext context) {
        Element nameElem = (Element) context.getDocument().getElementsByTagName("name").item(0);
        if (nameElem == null) {
            return;
        }
        String userName = nameElem.getTextContent();
        if(Objects.equals(userName, context.getModel().getName())){
            return;
        }
        context.getModel().getUsersList().remove(userName);
        context.getModel().setMsg(userName);
        context.getModel().onModelChange(EventHandle.USER_LOGOUT);
    }
}
