package com.example.konstantin.cool_chat_project;

import java.io.IOException;

/**
 * Created by konstantin on 22.03.16.

 */
public interface iMessageSender {

    void send(SocketAdapter socket) throws IOException;
    void receive(SocketAdapter socket) throws IOException;

}
