package ru.nsu.vorobev.chat.client.model.configparser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigParser implements AutoCloseable{
    private static final String pathToConfig = "res/client-config.txt";
    private final BufferedReader reader;
    private final List<String> lines = new ArrayList<>();

    public ConfigParser() {
        try {
            reader = new BufferedReader(new FileReader(pathToConfig));
        } catch (FileNotFoundException ex){
            throw new NoConfigFileException("No config file",ex);
        }
        String str;

        try {
            while ((str = reader.readLine()) != null) {
                if (str.length() != 0 && str.getBytes()[0] != '#') {
                    lines.add(str);
                }
            }
        } catch (IOException ex){
            throw new BadConfigException("Error during reading config",ex);
        }
    }

    private int getIndex(String str) {
        int index = str.indexOf('=');
        if (index == -1 || index == str.length()-1) {
            throw new BadConfigException("Bad config format");
        }
        return index;
    }
    public String getStrByName(String name){
        String line = null;
        for (String str : lines){
            String currName = str.substring(0,getIndex(str));
            if(Objects.equals(currName, name)){
                line = str;
                break;
            }
        }
        if(line == null){
            throw new BadConfigException("Such line doesn't exist " + name);
        }

        int index = getIndex(line);
        return line.substring(index+1);
    }
    @Override
    public void close() throws Exception {
        reader.close();
    }
}
