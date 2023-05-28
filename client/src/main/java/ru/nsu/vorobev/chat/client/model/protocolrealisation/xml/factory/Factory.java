package ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Factory {
    private final Map<String, String> names = new HashMap<>();

    public Factory() {
        try (InputStream stream = Factory.class.getResourceAsStream("operators.cfg")) {
            if (stream == null) {
                throw new NoFactoryConfigException("Config file doesn't exist");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String str;
            while ((str = reader.readLine()) != null) {
                String[] strings = str.split(" ");
                names.put(strings[0], strings[1]);
            }
        } catch (IOException e) {
            throw new NoFactoryConfigException("Config file doesn't exist", e);
        }
    }

    public Operable getOperator(String name){

        try {
            Class<? extends Operable> operator =  Class.forName(names.get(name)).asSubclass(Operable.class);
            return operator.getDeclaredConstructor().newInstance();
        } catch (Exception e){
            throw new NoNameException("Such command doesn't exist");
        }
    }

}