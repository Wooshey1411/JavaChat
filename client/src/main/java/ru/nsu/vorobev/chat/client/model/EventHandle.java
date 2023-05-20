package ru.nsu.vorobev.chat.client.model;

public enum EventHandle {
    BAD_IP_INPUT,
    BAD_PORT_INPUT,
    BIG_NICKNAME,
    SOCKET_ERROR,
    USER_WITH_SAME_NAME,
    MESSAGE_SUCCESSFUL,
    MESSAGE_FAILED
}
