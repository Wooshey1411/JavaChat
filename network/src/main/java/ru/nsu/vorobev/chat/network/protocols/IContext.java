package ru.nsu.vorobev.chat.network.protocols;

import java.util.List;

public interface IContext {
    void onMessageReact(String msg);
    void onMessageAnsReact(String ans);
    void onNamesAnsReact(String reason, List<String> names);

}
