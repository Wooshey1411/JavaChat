package ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory;

import ru.nsu.vorobev.chat.client.model.EventHandle;
public class MessageSuccess implements Operable {

    @Override
    public void doOperation(IContext context) {
        context.getModel().onModelChange(EventHandle.MESSAGE_SUCCESSFUL);
    }
}
