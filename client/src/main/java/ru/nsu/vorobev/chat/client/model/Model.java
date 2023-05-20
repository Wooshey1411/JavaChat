package ru.nsu.vorobev.chat.client.model;


import ru.nsu.vorobev.chat.client.model.exceptions.SocketException;
import ru.nsu.vorobev.chat.network.connection.*;
import ru.nsu.vorobev.chat.network.protocols.Message;
import ru.nsu.vorobev.chat.network.protocols.Registration;

import java.io.IOException;
import java.net.Socket;

public class Model implements TCPConnectionListener {

    public static final int maxLengthOfName = 32;
    private String ipAddress;
    private int port;
    private String name;
    private int ID;
    private TCPConnectionSerializable connection;

    private ModelListener listener;

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
        connection.sendData(new Message(msg, true, ID));
    }


    public void usersListRequest(){

    }


    @Override
    public void onConnectionReady(TCPConnectionSerializable tcpConnectionSerializable) {
        listener.onModelReceived("Connection ready...");
    }

    @Override
    public void onReceiveData(TCPConnectionSerializable tcpConnectionSerializable, Object o) {
        Message message = new Message("", true,ID);
        System.out.println("o= " + o.toString());
        message = (Message)o;
        listener.onModelReceived(message.getMessage());
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

        System.out.println(registrationAns.ID);
        if(!registrationAns.isSuccessful){
         //   tcpConnectionSerializable.disconnect();
            throw new UserWithSameName("Exist user with same nickname");
        }

        System.out.println(registrationAns.ID);
        ID = registrationAns.ID;
    }
}
