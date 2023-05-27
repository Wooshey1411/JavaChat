package ru.nsu.vorobev.chat.client.model.protocolrealisation.receive.serializable;

import ru.nsu.vorobev.chat.client.model.configparser.NoConfigFileException;
import ru.nsu.vorobev.chat.client.model.protocolrealisation.NoNameException;
import ru.nsu.vorobev.chat.client.model.protocolrealisation.ReceiveOperation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;

public class Factory {
    static final String pathToConfig = "serializable.config";
    private final HashMap<String,String> commands = new HashMap<>();

    public Factory() {
        try(InputStream stream = Factory.class.getResourceAsStream("commands.cfg")){
            if(stream == null){
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String str;
            while((str = reader.readLine()) != null){
                String[] strings = str.split(" ");
                commands.put(strings[0],strings[1]);
            }
        }
        catch (IOException e){
            throw new NoConfigFileException("Config file doesn't exist",e);
        }
    }

    /*public ReceiveOperation<Object> getCommand(String name){

        try {
            Class<? extends ReceiveOperation<Object>> command = Class.forName(commands.get(name)).asSubclass(ReceiveOperation<Object>.class);
            return command.getDeclaredConstructor().newInstance();
        } catch (Exception e){
            throw new NoNameException("Such command doesn't exist",e);
        }
    }*/
}
