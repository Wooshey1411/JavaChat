package ru.nsu.vorobev.chat.network.connection;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class TCPConnectionByte implements TCPConnection{

    private final Socket socket;
    private Thread thread;
    private final TCPConnectionListener eventListener;
    private final DataInputStream in;
    private final DataOutputStream out;
    public TCPConnectionByte(TCPConnectionListener eventListener, String ip, int port) throws IOException {
        this(eventListener, new Socket(ip,port));
    }
    public TCPConnectionByte(final TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;

        out = new DataOutputStream(socket.getOutputStream());
        out.flush();
        in = new DataInputStream(socket.getInputStream());

        try {
            eventListener.onRegistration(TCPConnectionByte.this);
        } catch (SocketException | ClassNotFoundException ex){
            socket.close();
            eventListener.onException(null, ex);
            return;
        }

        thread = new Thread(() -> {
            try {
                if(socket.isConnected()) {
                    eventListener.onConnectionReady(TCPConnectionByte.this);
                }
                while (thread != null && !thread.isInterrupted()) {
                    byte[] data = receiveData();
                        if(data == null){
                            break;
                        }
                    eventListener.onReceiveData(TCPConnectionByte.this, data);
                }
            } catch (IOException ex){
                eventListener.onException(TCPConnectionByte.this,ex);
            }  finally {
                eventListener.onDisconnect(TCPConnectionByte.this);
            }
        });
        thread.start();
    }

    @Override
    public void sendData(byte[] obj){
        try{
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(obj.length);
            buffer.rewind();
            byte[] len = buffer.array();
            byte[] send = new byte[obj.length + Integer.BYTES];

            System.arraycopy(len,0,send,0,Integer.BYTES);
            System.arraycopy(obj,0,send,Integer.BYTES,obj.length);
            out.write(send);
        } catch (IOException ex){
            ex.printStackTrace();
            eventListener.onException(TCPConnectionByte.this,ex);
            disconnect();
        }
    }

    @Override
    public byte[] receiveData() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(in.readNBytes(Integer.BYTES));
        buffer.rewind();
        int len = buffer.getInt();
        if(len <= 0){
            return null;
        }
        byte[] buff;
        buff = in.readNBytes(len);
        return buff;
    }

    @Override
    public synchronized void disconnect(){
        if(thread != null) {
            thread.interrupt();
        }
        try {
            in.close();
            out.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException ex){
            eventListener.onException(TCPConnectionByte.this,ex);
        }
    }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress().toString().substring(1) + ":" + socket.getPort();
    }
}
