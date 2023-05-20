package ru.nsu.vorobev.chat.network.connection;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class TCPConnectionSerializable {
    private final Socket socket;
    private Thread thread;
    private final TCPConnectionListener eventListener;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    public TCPConnectionSerializable(TCPConnectionListener eventListener, String ip, int port) throws IOException {
        this(eventListener, new Socket(ip,port));
    }
    public TCPConnectionSerializable(final TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;

        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        try {
            eventListener.onRegistration(TCPConnectionSerializable.this);
        } catch (ClassNotFoundException ex){
            socket.close();
            eventListener.onException(null, ex);
            return;
        }

        thread = new Thread(new Runnable() {
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnectionSerializable.this);
                    while (thread != null && !thread.isInterrupted()) {
                        Object msg = in.readObject();
                        eventListener.onReceiveData(TCPConnectionSerializable.this, msg);
                    }
                } catch (IOException ex){
                    eventListener.onException(TCPConnectionSerializable.this,ex);
                } catch (ClassNotFoundException ignored) {
                } finally {
                    eventListener.onDisconnect(TCPConnectionSerializable.this);
                }
            }
        });
        thread.start();
    }


    public synchronized void sendData(Object obj){
        try{
            out.writeObject(obj);
        } catch (IOException ex){
            eventListener.onException(TCPConnectionSerializable.this,ex);
            disconnect();
        }
    }

    public synchronized void disconnect(){
        if(thread != null) {
            thread.interrupt();
        }
        try {
            socket.close();
        } catch (IOException ex){
            eventListener.onException(TCPConnectionSerializable.this,ex);
        }
    }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress().toString().substring(1) + ":" + socket.getPort();
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public ObjectOutputStream getOut() {
        return out;
    }
}
