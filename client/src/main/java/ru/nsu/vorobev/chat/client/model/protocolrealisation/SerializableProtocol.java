package ru.nsu.vorobev.chat.client.model.protocolrealisation;

import ru.nsu.vorobev.chat.client.model.EventHandle;
import ru.nsu.vorobev.chat.client.model.Model;
import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionListener;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionSerializable;
import ru.nsu.vorobev.chat.network.connection.UserWithSameName;
import ru.nsu.vorobev.chat.network.protocols.*;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class SerializableProtocol implements TCPConnectionListener,Connection {

    private final Model model;

    public SerializableProtocol(Model model){
        this.model = model;
    }

    private TCPConnectionSerializable connection;
    @Override
    public void connect() throws IOException {
        connection = new TCPConnectionSerializable(SerializableProtocol.this,new Socket(model.getIpAddress(), model.getPort()));
    }
    @Override
    public void disconnect(){
        connection.disconnect();
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnectionSerializable) {
        model.onModelReceive("Connection ready...");
    }

    @Override
    public void onReceiveData(TCPConnection tcpConnectionSerializable, Object o) {

        if (o instanceof MessageAns){
            if(!((MessageAns) o).isSuccessful()){
                model.setMsg(((MessageAns) o).getReason());
                model.onModelChange(EventHandle.MESSAGE_FAILED);
            } else {
                model.onModelChange(EventHandle.MESSAGE_SUCCESSFUL);
            }
            return;
        }

        if(o instanceof Message){
            model.onModelReceive(((Message) o).getName() + ": " + ((Message) o).getMessage());
            return;
        }

        if(o instanceof NamesAns){
            if(!((NamesAns) o).isSuccessful()){
                model.setMsg(((NamesAns) o).getReason());
                model.onModelChange(EventHandle.NAMES_REQ_FAILED);
            } else {
                model.setUsersList(((NamesAns) o).getNames());
                model.onModelChange(EventHandle.NAMES_REQ_SUCCESSFUL);
            }
            return;
        }
        if(o instanceof UserLogin){
            if(Objects.equals(((UserLogin) o).getName(), model.getName())){
                return;
            }
            model.getUsersList().add(((UserLogin) o).getName());
            model.setMsg(((UserLogin) o).getName());
            model.onModelChange(EventHandle.USER_LOGIN);
            return;
        }
        if(o instanceof UserLogout){
            model.getUsersList().remove(((UserLogout) o).getName());
            model.setMsg(((UserLogout) o).getName());
            model.onModelChange(EventHandle.USER_LOGOUT);
            return;
        }
        if(o instanceof Disconnect){
            if (((Disconnect) o).getSuccessful()){
                connection.disconnect();
                model.onModelChange(EventHandle.DISCONNECT);
            } else {
                model.setError(((Disconnect) o).getReason());
                model.onModelChange(EventHandle.ERROR);
            }
        }
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnectionSerializable) {
        model.onModelReceive("Connection closed");
    }

    @Override
    public void onException(TCPConnection tcpConnectionSerializable, Exception ex) {
        model.onModelReceive("Connection exception " + ex);
    }
    @Override
    public void onRegistration(TCPConnection tcpConnectionSerializable) throws IOException, ClassNotFoundException {
        Registration registrationReq = new Registration(false,-1, model.getName());

        tcpConnectionSerializable.sendData(registrationReq);

        Registration registrationAns;
        registrationAns = (Registration)tcpConnectionSerializable.receiveData();

        if(!registrationAns.isSuccessful()){
            tcpConnectionSerializable.disconnect();
            throw new UserWithSameName("Exist user with same nickname");
        }

        model.setID(registrationAns.getID());
    }

    @Override
    public void sendMsg(String msg){
        connection.sendData(new Message(msg, model.getID(),null));
    }
    @Override
    public void usersListRequest(){
        connection.sendData(new NamesReq(model.getID()));
    }

    @Override
    public void disconnectRequest() {
        connection.sendData(new Disconnect(true,null, model.getID()));
    }

}
