package ru.nsu.vorobev.chat.client.model.protocolrealisation;

import ru.nsu.vorobev.chat.client.model.EventHandle;
import ru.nsu.vorobev.chat.client.model.Model;
import ru.nsu.vorobev.chat.client.model.exceptions.ProtocolException;
import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionByte;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionListener;
import ru.nsu.vorobev.chat.network.connection.UserWithSameName;
import ru.nsu.vorobev.chat.network.protocols.*;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class SerializableProtocol implements TCPConnectionListener,Connection {

    private final Model model;

    public SerializableProtocol(Model model){
        this.model = model;
    }

    private TCPConnectionByte connection;
    @Override
    public void connect() throws IOException {
        connection = new TCPConnectionByte(SerializableProtocol.this,new Socket(model.getIpAddress(), model.getPort()));
    }

    byte[] ConvertObjectToByte(Object o){
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out;
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            return bos.toByteArray();
        } catch (IOException ex){
            return null;
        }
    }

    Object ConvertByteToObject(byte[] obj){
        ByteArrayInputStream bis = new ByteArrayInputStream(obj);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException ex){
            return null;
        }
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
    public void onReceiveData(TCPConnection tcpConnectionSerializable, byte[] bytes) {
        Object o = ConvertByteToObject(bytes);
        if(o == null){
            return;
        }
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
    public void onRegistration(TCPConnection tcpConnectionSerializable) throws IOException {
        Registration registrationReq = new Registration(false,-1, model.getName());

        tcpConnectionSerializable.sendData(ConvertObjectToByte(registrationReq));

        Registration registrationAns;
        try {
            registrationAns = (Registration) ConvertByteToObject(tcpConnectionSerializable.receiveData());
        } catch (ClassNotFoundException ex){
            model.setError("Wrong protocol");
            throw new ProtocolException("Wrong protocol");
        }

        if(!registrationAns.isSuccessful()){
            tcpConnectionSerializable.disconnect();
            model.setError(registrationAns.getMsg());
            throw new UserWithSameName(registrationAns.getMsg());
        }

        model.setID(registrationAns.getID());
    }

    @Override
    public void sendMsg(String msg){
        connection.sendData(ConvertObjectToByte(new Message(msg, model.getID(),null)));
    }
    @Override
    public void usersListRequest(){
        connection.sendData(ConvertObjectToByte(new NamesReq(model.getID())));
    }

    @Override
    public void disconnectRequest() {
        connection.sendData(ConvertObjectToByte(new Disconnect(true,null, model.getID())));
    }

}
