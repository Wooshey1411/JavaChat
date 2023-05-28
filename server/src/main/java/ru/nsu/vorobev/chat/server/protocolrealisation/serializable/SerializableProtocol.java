package ru.nsu.vorobev.chat.server.protocolrealisation.serializable;

import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionByte;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionListener;
import ru.nsu.vorobev.chat.network.connection.UserWithSameName;
import ru.nsu.vorobev.chat.network.protocols.*;
import ru.nsu.vorobev.chat.server.ChatServer;
import ru.nsu.vorobev.chat.server.protocolrealisation.Connection;
import ru.nsu.vorobev.chat.server.protocolrealisation.Log;
import ru.nsu.vorobev.chat.server.protocolrealisation.MessageType;
import ru.nsu.vorobev.chat.server.protocolrealisation.User;

import java.io.*;
import java.net.ServerSocket;
import java.util.Objects;

public class SerializableProtocol implements TCPConnectionListener, Connection {

    private static int ID = 0;
    private final ChatServer server;

    private final int port;

    private final Context context;
    public SerializableProtocol(int port, ChatServer server){
        this.port = port;
        this.server = server;
        context = new Context(server);
    }



    @Override
    public void start(){
        System.out.println("Server running...");
        Log.log(Log.getTime() + ":Server start working",Log.TypeOfLoggers.INFO);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    new TCPConnectionByte(this, serverSocket.accept());
                } catch (IOException ex) {
                    Log.log(Log.getTime() + ":TCPConnection exception:" + ex,Log.TypeOfLoggers.ERROR);
                }
                catch (UserWithSameName ignored){}

            }
        } catch (IOException ex) {
            Log.log(Log.getTime() + ":Server socket error! Server shutdown... exception:" + ex, Log.TypeOfLoggers.ERROR);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnectionSerializable) {

        String name = null;
        int ID = -1;
        for (TCPConnection connection : server.getUsers().keySet()){
            if(connection == tcpConnectionSerializable){
                name = server.getUsers().get(connection).getNickname();
                ID = server.getUsers().get(connection).getID();
                break;
            }
        }
        Log.log(Log.getTime() + ":Client connected. Nickname:" +name + " ID=" + ID, Log.TypeOfLoggers.INFO);
        broadCastMessage(new UserLogin(name));
        for (MessageType msg : server.getMessages()){
            tcpConnectionSerializable.sendData(Utils.convertObjectToByte(new MessageFromServer(msg.getMsg(),msg.getSender(),-1)));
        }
    }

    @Override
    public synchronized void onReceiveData(TCPConnection tcpConnectionSerializable, byte[] bytes) {
        Object obj = Utils.convertByteToObject(bytes);
        if(!(obj instanceof OperableServer)){
            return;
        }
        ((OperableServer) obj).doOperation(context,tcpConnectionSerializable);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnectionSerializable) {
        String name = null;
        for(TCPConnection connection : server.getUsers().keySet()){
            if (tcpConnectionSerializable == connection){
                name = server.getUsers().get(connection).getNickname();
                server.getUsers().remove(connection);
                break;
            }
        }
        Log.log(Log.getTime() + ":Client disconnected. Nickname:" +name,Log.TypeOfLoggers.INFO);
        broadCastMessage(new UserLogout(name));
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnectionSerializable, Exception ex) {
        Log.log(Log.getTime() + ":" + ex, Log.TypeOfLoggers.WARNING);
    }

    @Override
    public void onRegistration(TCPConnection tcpConnectionSerializable) throws IOException, ClassNotFoundException {
        RegistrationReq registrationReqReceive;
        try {
            registrationReqReceive = (RegistrationReq) Utils.convertByteToObject(tcpConnectionSerializable.receiveData());
        } catch (ClassNotFoundException ex){
            tcpConnectionSerializable.sendData(Utils.convertObjectToByte(new RegistrationAns(false, "wrong protocol",-1)));
            tcpConnectionSerializable.disconnect();
            Log.log(Log.getTime() + ":TCPConnection try to connect with wrong protocol " + tcpConnectionSerializable, Log.TypeOfLoggers.WARNING);
            throw new ClassNotFoundException("Wrong protocol");
        }

        //System.out.println(registrationReceive.msg);
        RegistrationAns registrationReqAns = null;
        for (User user : server.getUsers().values()){
            if(Objects.equals(user.getNickname(), registrationReqReceive.getMsg())){
                registrationReqAns = new RegistrationAns(false,"Exist user with same name",-1);
                break;
            }
        }
        if(registrationReqAns == null){
            registrationReqAns = new RegistrationAns(true,null,ID++);
        }
        //tcpConnectionSerializable.getOut().writeObject(registrationAns);
        tcpConnectionSerializable.sendData(Utils.convertObjectToByte(registrationReqAns));
       // tcpConnectionSerializable.getOut().flush();
        if(!registrationReqAns.isSuccessful()){
            Log.log(Log.getTime() + ":Client try to connect with exist nickname connection was closed. Nickname:" + registrationReqReceive.getMsg(),Log.TypeOfLoggers.INFO);
            throw new UserWithSameName("Exist user with same name");
        }
        server.getUsers().put(tcpConnectionSerializable,new User(registrationReqReceive.getMsg(), registrationReqAns.getID()));
    }

    private void broadCastMessage(Object object) {
        for (TCPConnection connection : server.getUsers().keySet()) {
            connection.sendData(Utils.convertObjectToByte(object));
        }
    }

}
