package ru.nsu.vorobev.chat.server.protocolrealisation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.time.LocalDateTime;

public class Log {

    private static final String res = "log4j.xml";
    private static boolean isInit = false;
    private static Logger info;
    private static Logger warning;
    private static Logger error;
    private static boolean isLoggerEnabled = false;

    public static synchronized String getTime(){
        String time = LocalDateTime.now().toString();
        // time format : 2023-05-03T22:58:16.119375100 - found last T
        time = time.substring(time.indexOf('T'));
        // TXX.XX.XX:XXX from 1 to 13 symbol
        time = time.substring(1,13);
        return time;
    }
    public static synchronized void init(){
        if(isInit){
            return;
        }
        byte[] buffer = new byte[1024];
        int readed;
        try(InputStream config = Log.class.getResourceAsStream(res)){
            assert config != null;
            readed = config.read(buffer);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
        File file;
        try {
            file = File.createTempFile("logger",".tmp");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try(OutputStream out = new FileOutputStream(file)){
            out.write(buffer,0,readed);
        } catch (IOException ex){
            ex.printStackTrace();
        }
        file.deleteOnExit();
        System.setProperty("log4j.configurationFile",file.getPath());
        info = LogManager.getLogger("log-info");
        warning = LogManager.getLogger("log-warning");
        error = LogManager.getLogger("log-error");
        isInit = true;
    }

    public static synchronized void enableLogger(){
        isLoggerEnabled = true;
    }

    public static synchronized void log(String message, TypeOfLoggers type){
        if(!isLoggerEnabled || !isInit){
            return;
        }
        switch (type){
            case INFO -> info.info(message);
            case WARNING -> warning.warn(message);
            case ERROR -> error.error(message);
        }

    }

    enum TypeOfLoggers{
        INFO,
        WARNING,
        ERROR
    }

}
