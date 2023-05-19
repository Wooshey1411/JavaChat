package ru.nsu.vorobev.chat.server;

import ru.nsu.vorobev.chat.network.connection.*;
import ru.nsu.vorobev.chat.network.protocols.Message;
import ru.nsu.vorobev.chat.network.protocols.Registration;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ChatServer implements TCPConnectionListener {

    private static final int port = 8377;
    private static int ID = 0;

    private final List<TCPConnectionSerializable> connections = new ArrayList<>();
    private final List<User> users = new ArrayList<>();

    public static void main(String[] args) {
        new ChatServer();
    }

    private ChatServer() {
        System.out.println("Server running...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    new TCPConnectionSerializable(this, serverSocket.accept());
                } catch (IOException ex) {
                    System.out.println("TCPConnection exception: " + ex);
                }

            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnectionSerializable tcpConnectionSerializable) {
        System.out.println("Client connected: " + tcpConnectionSerializable);
        broadCastMessage(new Message("Client connected: " + tcpConnectionSerializable));
    }

    @Override
    public synchronized void onReceiveData(TCPConnectionSerializable tcpConnectionSerializable, Object obj) {
        broadCastMessage(obj);
    }

    @Override
    public synchronized void onDisconnect(TCPConnectionSerializable tcpConnectionSerializable) {
        for(User user : users){
            if (tcpConnectionSerializable == user.getConnection()){
                users.remove(user);
                break;
            }
        }
        System.out.println("Client disconnected: " + tcpConnectionSerializable);
        broadCastMessage(new Message("Client disconnected: " + tcpConnectionSerializable));
    }

    @Override
    public synchronized void onException(TCPConnectionSerializable tcpConnectionSerializable, Exception ex) {
        System.out.println("TCPConnection exception: " + ex);
    }

    @Override
    public void onRegistration(TCPConnectionSerializable tcpConnectionSerializable) throws IOException, ClassNotFoundException {
        Registration registrationReceive = new Registration();
        registrationReceive = (Registration) tcpConnectionSerializable.getIn().readObject();

        System.out.println(registrationReceive.msg);
        Registration registrationAns = new Registration();
        registrationAns.ID = ID++;
        registrationAns.isSuccessful = true;
        tcpConnectionSerializable.getOut().writeObject(registrationAns);
        users.add(new User(tcpConnectionSerializable, registrationAns.msg,ID++));
    }

    private void broadCastMessage(Object object) {
        System.out.println("Broadcast: " + object.toString());
        for (User user : users) {
            user.getConnection().sendData(object);
        }
    }
}
