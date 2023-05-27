package ru.nsu.vorobev.chat.server.protocolrealisation;

import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionByte;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionListener;
import ru.nsu.vorobev.chat.network.connection.UserWithSameName;
import ru.nsu.vorobev.chat.network.protocols.*;
import ru.nsu.vorobev.chat.server.ChatServer;

import java.io.*;
import java.net.ServerSocket;
import java.util.Objects;

public class SerializableProtocol implements TCPConnectionListener,Connection {

    private static int ID = 0;
    private final ChatServer server;

    private final int port;

    public SerializableProtocol(int port, ChatServer server){
        this.port = port;
        this.server = server;
    }

    byte[] convertObjectToByte(Object o){
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

    Object convertByteToObject(byte[] obj){
        ByteArrayInputStream bis = new ByteArrayInputStream(obj);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException ex){
            return null;
        }
    }

    @Override
    public void start(){
        System.out.println("Server running...");
        Log.enableLogger();
        Log.init();
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
            tcpConnectionSerializable.sendData(convertObjectToByte(new Message(msg.getMsg(),-1,msg.getSender())));
        }
    }

    @Override
    public synchronized void onReceiveData(TCPConnection tcpConnectionSerializable, byte[] bytes) {
        Object obj = convertByteToObject(bytes);
        if(obj == null){
            return;
        }
        if(obj instanceof Message){
            Message BCMessage = null;
            User sender = null;
            for (User user : server.getUsers().values()){
                if(user.getID() == ((Message) obj).getID()){
                    sender = user;
                    BCMessage = new Message(((Message) obj).getMessage(),-1, user.getNickname());
                }
            }
            if(sender == null){
                tcpConnectionSerializable.sendData(convertObjectToByte(new MessageAns(false, "Bad ID")));
                Log.log(Log.getTime() + ":Client try to send message with nonexistent ID. TCPConnection:" + tcpConnectionSerializable,Log.TypeOfLoggers.INFO);
                return;
            }
            if(((Message) obj).getMessage() == null){
                tcpConnectionSerializable.sendData(convertObjectToByte(new MessageAns(false, "Null string")));
                Log.log(Log.getTime() + ":Client try to send message with NULL string. ID=" + sender.getID(),Log.TypeOfLoggers.INFO);
                return;
            }
            tcpConnectionSerializable.sendData(convertObjectToByte(new MessageAns(true,null)));

            if(server.getMessages().size() == ChatServer.maxHistoryLen){
                server.getMessages().remove(0);
            }
            server.getMessages().add(new MessageType(BCMessage.getMessage(),BCMessage.getName()));
            broadCastMessage(BCMessage);
            Log.log(Log.getTime() + ":Client " + sender.getNickname() + " with ID=" + sender.getID() + " send message \"" + BCMessage.getMessage() + "\"",Log.TypeOfLoggers.INFO);
            return;
        }
        if(obj instanceof NamesReq){
            User sender = null;
            for (User user : server.getUsers().values()){
                if(user.getID() == ((NamesReq) obj).getID()){
                    sender = user;
                }
            }
            if(sender == null){
                tcpConnectionSerializable.sendData(convertObjectToByte(new NamesAns(false, "Bad ID")));
                Log.log(Log.getTime() + ":Client send request of names with nonexistent ID. TCPConnection:" + tcpConnectionSerializable,Log.TypeOfLoggers.INFO);
                return;
            }
            NamesAns ans = new NamesAns(true,null);
            for (User user : server.getUsers().values()){
                ans.addName(user.getNickname());
            }
            tcpConnectionSerializable.sendData(convertObjectToByte(ans));
            Log.log(Log.getTime() + ":Client " + sender.getNickname() + " with ID=" + sender.getID() + " send request of names successfully",Log.TypeOfLoggers.INFO);
        }
        if(obj instanceof DisconnectReq){
            int id = -1;
            for (User user : server.getUsers().values()){
                if(user.getID() == ((DisconnectReq) obj).getID()){
                    id = user.getID();
                    break;
                }
            }
            if(id == -1){
                tcpConnectionSerializable.sendData(convertObjectToByte(new DisconnectAns("Wrong session ID",false)));
                return;
            }
            tcpConnectionSerializable.sendData(convertObjectToByte(new DisconnectAns(null,true)));
        }

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
            registrationReqReceive = (RegistrationReq) convertByteToObject(tcpConnectionSerializable.receiveData());
        } catch (ClassNotFoundException ex){
            tcpConnectionSerializable.sendData(convertObjectToByte(new RegistrationAns(false, "wrong protocol",-1)));
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
        tcpConnectionSerializable.sendData(convertObjectToByte(registrationReqAns));
       // tcpConnectionSerializable.getOut().flush();
        if(!registrationReqAns.isSuccessful()){
            Log.log(Log.getTime() + ":Client try to connect with exist nickname connection was closed. Nickname:" + registrationReqReceive.getMsg(),Log.TypeOfLoggers.INFO);
            throw new UserWithSameName("Exist user with same name");
        }
        server.getUsers().put(tcpConnectionSerializable,new User(registrationReqReceive.getMsg(), registrationReqAns.getID()));
    }

    private void broadCastMessage(Object object) {
        //  System.out.println("Broadcast: " + object.toString());
        for (TCPConnection connection : server.getUsers().keySet()) {
            connection.sendData(convertObjectToByte(object));
        }
    }

}
