package ru.nsu.vorobev.chat.network.protocols;

import java.util.List;

public interface IContext {
    void onMessageReact(String msg, String sender);
    void onMessageAnsReact(boolean successful,String ans);
    void onNamesReact(boolean successful, String reason, List<String> names);
    void onDisconnectReact(boolean successful,String reason);
    void onLoginReact(String name);
    void onLogoutReact(String name);
}
