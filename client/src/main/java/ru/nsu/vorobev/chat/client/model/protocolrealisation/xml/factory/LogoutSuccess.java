package ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory;

import ru.nsu.vorobev.chat.client.model.EventHandle;

public class LogoutSuccess implements Operable{
    @Override
    public void doOperation(IContext context) {
        context.getConnection().disconnect();
        context.getModel().onModelChange(EventHandle.DISCONNECT);
    }
}
