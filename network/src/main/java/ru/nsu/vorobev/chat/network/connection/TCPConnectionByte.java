package ru.nsu.vorobev.chat.network.connection;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TCPConnectionByte implements TCPConnection{

    private final Socket socket;
    private Thread thread;
    private final TCPConnectionListener eventListener;
    private final BufferedInputStream in;
    private final BufferedOutputStream out;
    public TCPConnectionByte(TCPConnectionListener eventListener, String ip, int port) throws IOException {
        this(eventListener, new Socket(ip,port));
    }
    public TCPConnectionByte(final TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;

        out = new BufferedOutputStream(socket.getOutputStream());
        out.flush();
        in = new BufferedInputStream(socket.getInputStream());

        try {
            eventListener.onRegistration(TCPConnectionByte.this);
        } catch (SocketException | ClassNotFoundException ex){
            socket.close();
            eventListener.onException(null, ex);
            return;
        }

        thread = new Thread(new Runnable() {
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnectionByte.this);
                    while (thread != null && !thread.isInterrupted()) {
                        eventListener.onReceiveData(TCPConnectionByte.this, receiveData());
                    }
                } catch (IOException ex){
                    eventListener.onException(TCPConnectionByte.this,ex);
                }  finally {
                    eventListener.onDisconnect(TCPConnectionByte.this);
                }
            }
        });
        thread.start();
    }

    @Override
    public synchronized void sendData(Object obj){
        try{

            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(((String)obj).length());
            buffer.rewind();
            byte[] len = buffer.array();
            byte[] msg = ((String)obj).getBytes();
            byte[] send = new byte[msg.length + Integer.SIZE / 8];

            System.arraycopy(len,0,send,0,Integer.SIZE/8);
            System.arraycopy(msg,0,send,Integer.SIZE/8,msg.length);
            out.write(send);
            out.flush();

        } catch (IOException ex){
            eventListener.onException(TCPConnectionByte.this,ex);
            disconnect();
        }
    }

    @Override
    public synchronized Object receiveData() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(in.readNBytes(Integer.BYTES));
        buffer.rewind();
        int len = buffer.getInt();
        byte[] buff;
        buff = in.readNBytes(len);
        return new String(buff,StandardCharsets.UTF_8);
    }

    @Override
    public synchronized void disconnect(){
        if(thread != null) {
            thread.interrupt();
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
