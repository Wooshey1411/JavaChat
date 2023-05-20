package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NamesAns implements Serializable {
    @Serial
    private static final long serialVersionUID = 5;

    public NamesAns(boolean isSuccessful, String reason){
        this.isSuccessful = isSuccessful;
        this.reason = reason;
    }
    boolean isSuccessful;
    String reason;
    List<String> names = new ArrayList<>();

    public List<String> getNames() {
        return names;
    }

    public void addName(String name){
        names.add(name);
    }

    public String getReason() {
        return reason;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }
}
