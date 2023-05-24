package ru.nsu.vorobev.chat.server.protocolrealisation;

import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionByte;
import ru.nsu.vorobev.chat.network.connection.TCPConnectionListener;
import ru.nsu.vorobev.chat.network.connection.UserWithSameName;
import ru.nsu.vorobev.chat.network.protocols.*;
import ru.nsu.vorobev.chat.server.ChatServer;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SerializableProtocol implements TCPConnectionListener,Connection {

    private static int ID = 0;

    private final List<User> users = new ArrayList<>();
    private final List<Message> messagesHistory = new ArrayList<>();

    private final int port;

    public SerializableProtocol(int port){
        this.port = port;
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
        for (User user : users){
            if(user.getConnection() == tcpConnectionSerializable){
                name = user.getNickname();
                ID = user.getID();
                break;
            }
        }
        Log.log(Log.getTime() + ":Client connected. Nickname:" +name + " ID=" + ID, Log.TypeOfLoggers.INFO);
        broadCastMessage(new UserLogin(name));
        for (Message msg : messagesHistory){
            tcpConnectionSerializable.sendData(convertObjectToByte(msg));
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
            for (User user : users){
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

            if(messagesHistory.size() == ChatServer.maxHistoryLen){
                messagesHistory.remove(0);
            }
            messagesHistory.add(BCMessage);
            broadCastMessage(BCMessage);
            Log.log(Log.getTime() + ":Client " + sender.getNickname() + " with ID=" + sender.getID() + " send message \"" + BCMessage.getMessage() + "\"",Log.TypeOfLoggers.INFO);
            return;
        }
        if(obj instanceof NamesReq){
            User sender = null;
            for (User user : users){
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
            for (User user : users){
                ans.addName(user.getNickname());
            }
            tcpConnectionSerializable.sendData(convertObjectToByte(ans));
            Log.log(Log.getTime() + ":Client " + sender.getNickname() + " with ID=" + sender.getID() + " send request of names successfully",Log.TypeOfLoggers.INFO);
        }
        if(obj instanceof Disconnect){
            int id = -1;
            for (User user : users){
                if(user.getID() == ((Disconnect) obj).getID()){
                    id = user.getID();
                    break;
                }
            }
            if(id == -1){
                tcpConnectionSerializable.sendData(convertObjectToByte(new Disconnect(false,"Wrong session ID",0)));
                return;
            }
            tcpConnectionSerializable.sendData(convertObjectToByte(new Disconnect(true,"null",0)));
        }

    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnectionSerializable) {
        String name = null;
        for(User user : users){
            if (tcpConnectionSerializable == user.getConnection()){
                name = user.getNickname();
                users.remove(user);
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
        Registration registrationReceive;
        try {
            registrationReceive = (Registration) convertByteToObject(tcpConnectionSerializable.receiveData());
        } catch (ClassNotFoundException ex){
            tcpConnectionSerializable.sendData(convertObjectToByte(new Registration(false, -1,"wrong protocol")));
            tcpConnectionSerializable.disconnect();
            Log.log(Log.getTime() + ":TCPConnection try to connect with wrong protocol " + tcpConnectionSerializable, Log.TypeOfLoggers.WARNING);
            throw new ClassNotFoundException("Wrong protocol");
        }

        //System.out.println(registrationReceive.msg);
        Registration registrationAns = new Registration(true,ID++,null);
        for (User user : users){
            if(Objects.equals(user.getNickname(), registrationReceive.getMsg())){
                registrationAns.setSuccessful(false);
                registrationAns.setMsg("Exist user with same name");
                break;
            }
        }
        //tcpConnectionSerializable.getOut().writeObject(registrationAns);
        tcpConnectionSerializable.sendData(convertObjectToByte(registrationAns));
       // tcpConnectionSerializable.getOut().flush();
        if(!registrationAns.isSuccessful()){
            Log.log(Log.getTime() + ":Client try to connect with exist nickname connection was closed. Nickname:" + registrationReceive.getMsg(),Log.TypeOfLoggers.INFO);
            throw new UserWithSameName("Exist user with same name");
        }
        users.add(new User(tcpConnectionSerializable, registrationReceive.getMsg(),registrationAns.getID()));
    }

    private void broadCastMessage(Object object) {
        //  System.out.println("Broadcast: " + object.toString());
        for (User user : users) {
            user.getConnection().sendData(convertObjectToByte(object));
        }
    }

}
