package ru.nsu.vorobev.chat.client.model;


import ru.nsu.vorobev.chat.client.model.exceptions.SocketException;
import ru.nsu.vorobev.chat.client.model.protocolrealisation.Connection;
import ru.nsu.vorobev.chat.client.model.protocolrealisation.SerializableProtocol;
import ru.nsu.vorobev.chat.client.model.protocolrealisation.XMLProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Model {

    public static final int maxLengthOfName = 32;
    private String ipAddress;
    private int port;
    private String name;
    private int ID;
    private String msg;
    private ModelListener listener;
    private List<String> usersList = new ArrayList<>();

    private Connection connection = new SerializableProtocol(this);

    public void openConnection() {

        try {
           // connection = new TCPConnectionSerializable(Model.this,new Socket(ipAddress,port));
            connection.connect();
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

    public void sendMsg(String msg){
        connection.sendMsg(msg);
    }
    public void usersListRequest(){
        connection.usersListRequest();
    }
    public String getMsg() {
        return msg;
    }

    public List<String> getUsersList() {
        return usersList;
    }

    public void close(){
        connection.disconnect();
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void onModelChange(EventHandle handle){
        listener.onModelChanged(handle);
    }
    public void onModelReceive(String msg){
        listener.onModelReceived(msg);
    }

    public void setUsersList(List<String> usersList) {
        this.usersList = usersList;
    }

    public String getName() {
        return name;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getPort() {
        return port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getID() {
        return ID;
    }

    public void tryDisconnect(){
        connection.disconnectRequest();
    }
}
