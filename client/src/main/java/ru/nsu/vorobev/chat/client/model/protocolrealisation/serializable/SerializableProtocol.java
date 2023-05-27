package ru.nsu.vorobev.chat.client.model.protocolrealisation.serializable;

import ru.nsu.vorobev.chat.client.model.Model;
import ru.nsu.vorobev.chat.client.model.exceptions.ProtocolException;
import ru.nsu.vorobev.chat.client.model.protocolrealisation.Connection;
import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionByte;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionListener;
import ru.nsu.vorobev.chat.network.connection.UserWithSameName;
import ru.nsu.vorobev.chat.network.protocols.*;

import java.io.*;
import java.net.Socket;

public class SerializableProtocol implements TCPConnectionListener, Connection {

    private final Model model;
    private Context context;

    public SerializableProtocol(Model model){
        this.model = model;
    }

    private TCPConnectionByte connection;
    @Override
    public void connect() throws IOException {
        connection = new TCPConnectionByte(this,new Socket(model.getIpAddress(), model.getPort()));
        context = new Context(model,connection);
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
        if(!(o instanceof Operable)){
            return;
        }
        ((Operable) o).doOperation(context);
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
        RegistrationReq registrationReq = new RegistrationReq(-1, model.getName());

        tcpConnectionSerializable.sendData(ConvertObjectToByte(registrationReq));

        RegistrationAns registrationAns;
        try {
            registrationAns = (RegistrationAns) ConvertByteToObject(tcpConnectionSerializable.receiveData());
        } catch (ClassNotFoundException ex){
            model.setError("Wrong protocol");
            throw new ProtocolException("Wrong protocol");
        }

        if(!registrationAns.isSuccessful()){
            tcpConnectionSerializable.disconnect();
            model.setError(registrationAns.getMessage());
            throw new UserWithSameName(registrationAns.getMessage());
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
        connection.sendData(ConvertObjectToByte(new DisconnectReq(model.getID())));
    }

}
