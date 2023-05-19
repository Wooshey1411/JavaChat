package ru.nsu.vorobev.chat.client.model;

public interface ModelListener {
    void onModelChanged(EventHandle handle);
    void onModelReceived(String msg);
}
