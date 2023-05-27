package ru.nsu.vorobev.chat.server.protocolrealisation.serializable;

import java.io.*;

class Utils {
    static byte[] convertObjectToByte(Object o){
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

    static Object convertByteToObject(byte[] obj){
        ByteArrayInputStream bis = new ByteArrayInputStream(obj);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException ex){
            return null;
        }
    }
}
