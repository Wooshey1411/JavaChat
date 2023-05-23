package ru.nsu.vorobev.chat.server.configparser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigParser implements AutoCloseable{
    private static final String pathToConfig = "res/server-config.txt";
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

    private int readIntFromConfig(String str, int index) {
        int count;
        try {
            count = Integer.parseInt(str.substring(index + 1));
            if (count <= 0) {
                throw new ArithmeticException();
            }
        } catch (NumberFormatException | ArithmeticException ex) {
            throw new BadConfigException("No integer after '=' or number less than 0", ex);
        }
        return count;
    }
    private int getIndex(String str) {
        int index = str.indexOf('=');
        if (index == -1 || index == str.length()-1) {
            throw new BadConfigException("Bad config format");
        }
        return index;
    }

    private String getLineByName(String name){
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
        return line;
    }

    public int getIntByName(String name){
        String line = getLineByName(name);
        int index = getIndex(line);
        return readIntFromConfig(line,index);
    }
    public String getStrByName(String name){
        String line = getLineByName(name);
        int index = getIndex(line);
        return line.substring(index+1);
    }
    @Override
    public void close() throws Exception {
        reader.close();
    }
}
