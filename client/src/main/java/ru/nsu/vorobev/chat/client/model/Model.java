package ru.nsu.vorobev.chat.client.model;


import ru.nsu.vorobev.chat.client.model.exceptions.SocketException;
import ru.nsu.vorobev.chat.network.connection.*;
import ru.nsu.vorobev.chat.network.protocols.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Model implements TCPConnectionListener {

    public static final int maxLengthOfName = 32;
    private String ipAddress;
    private int port;
    private String name;
    private int ID;
    private TCPConnectionSerializable connection;
    private String msg;
    private ModelListener listener;
    private List<String> usersList = new ArrayList<>();

    public void openConnection() {

        try {
            connection = new TCPConnectionSerializable(Model.this,new Socket(ipAddress,port));
        } catch (IOException ex){
            throw new SocketException("Error during opening the socket",ex);
        }
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setListener(ModelListener listener) {
        this.listener = listener;
    }
    public void sendEvent(EventHandle handle){
        listener.onModelChanged(handle);
    }

    public String getName() {
        return name;
    }

    public void sendMsg(String msg){
        connection.sendData(new Message(msg, ID,null));
    }


    public void usersListRequest(){
        connection.sendData(new NamesReq(ID));
    }


    @Override
    public void onConnectionReady(TCPConnectionSerializable tcpConnectionSerializable) {
        listener.onModelReceived("Connection ready...");
    }

    @Override
    public void onReceiveData(TCPConnectionSerializable tcpConnectionSerializable, Object o) {

        if (o instanceof MessageAns){
            if(!((MessageAns) o).isSuccessful()){
                msg = ((MessageAns) o).getReason();
                listener.onModelChanged(EventHandle.MESSAGE_FAILED);
            } else {
                listener.onModelChanged(EventHandle.MESSAGE_SUCCESSFUL);
            }
            return;
        }

        if(o instanceof Message){
            listener.onModelReceived(((Message) o).getName() + ": " + ((Message) o).getMessage());
            return;
        }

        if(o instanceof NamesAns){
            if(!((NamesAns) o).isSuccessful()){
                msg = ((NamesAns) o).getReason();
                listener.onModelChanged(EventHandle.NAMES_REQ_FAILED);
            } else {
                usersList = ((NamesAns) o).getNames();
                listener.onModelChanged(EventHandle.NAMES_REQ_SUCCESSFUL);
            }
            return;
        }
        if(o instanceof UserLogin){
            if(Objects.equals(((UserLogin) o).getName(), name)){
                return;
            }
            usersList.add(((UserLogin) o).getName());
            msg = ((UserLogin) o).getName();
            listener.onModelChanged(EventHandle.USER_LOGIN);
            return;
        }
        if(o instanceof UserLogout){
            usersList.remove(((UserLogout) o).getName());
            msg = ((UserLogout) o).getName();
            listener.onModelChanged(EventHandle.USER_LOGOUT);
        }
    }

    public String getMsg() {
        return msg;
    }

    public List<String> getUsersList() {
        return usersList;
    }

    @Override
    public void onDisconnect(TCPConnectionSerializable tcpConnectionSerializable) {
        listener.onModelReceived("Connection closed");
    }

    @Override
    public void onException(TCPConnectionSerializable tcpConnectionSerializable, Exception ex) {
        listener.onModelReceived("Connection exception " + ex);
    }


    public void close(){
        connection.disconnect();
    }

    @Override
    public void onRegistration(TCPConnectionSerializable tcpConnectionSerializable) throws IOException, ClassNotFoundException {
        Registration registrationReq = new Registration();
        registrationReq.msg=name;

        tcpConnectionSerializable.getOut().writeObject(registrationReq);
        tcpConnectionSerializable.getOut().flush();

        Registration registrationAns;
        registrationAns = (Registration)tcpConnectionSerializable.getIn().readObject();

        if(!registrationAns.isSuccessful){
            tcpConnectionSerializable.disconnect();
            throw new UserWithSameName("Exist user with same nickname");
        }

        ID = registrationAns.ID;
    }
}
