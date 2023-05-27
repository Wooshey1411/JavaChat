package ru.nsu.vorobev.chat.client.model.protocolrealisation.serializable;

import ru.nsu.vorobev.chat.client.model.EventHandle;
import ru.nsu.vorobev.chat.client.model.Model;
import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.network.protocols.*;

import java.util.List;
import java.util.Objects;

public class Context implements IContext {

    private final Model model;
    private final TCPConnection connection;

    public Context(Model model,TCPConnection connection){
        this.model = model;
        this.connection = connection;
    }
    @Override
    public void onMessageReact(String msg, String sender) {
        model.onModelReceive(sender + ":" + msg);
    }

    @Override
    public void onMessageAnsReact(boolean successful,String ans) {
        if(!successful){
            model.setMsg(ans);
            model.onModelChange(EventHandle.MESSAGE_FAILED);
        } else {
            model.onModelChange(EventHandle.MESSAGE_SUCCESSFUL);
        }
    }

    @Override
    public void onNamesReact(boolean successful, String reason, List<String> names) {
        if(!successful){
            model.setMsg(reason);
            model.onModelChange(EventHandle.NAMES_REQ_FAILED);
        } else {
            model.setUsersList(names);
            model.onModelChange(EventHandle.NAMES_REQ_SUCCESSFUL);
        }
    }

    @Override
    public void onDisconnectReact(boolean successful,String reason) {
        if (successful){
            connection.disconnect();
            model.onModelChange(EventHandle.DISCONNECT);
        } else {
            model.setError(reason);
            model.onModelChange(EventHandle.ERROR);
        }
    }

    @Override
    public void onLoginReact(String s) {
        if(Objects.equals(s, model.getName())){
            return;
        }
        model.getUsersList().add(s);
        model.setMsg(s);
        model.onModelChange(EventHandle.USER_LOGIN);
    }

    @Override
    public void onLogoutReact(String s) {
        model.getUsersList().remove(s);
        model.setMsg(s);
        model.onModelChange(EventHandle.USER_LOGOUT);
    }

}
