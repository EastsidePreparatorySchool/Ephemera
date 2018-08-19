/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.scserver;

/**
 *
 * @author Quinn
 */
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebSocket
public class WebSocketHandler {
    //stored list of observers
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

    @OnWebSocketConnect
    public void connected(Session session) {
        sessions.add(session);
        send(session, "Hello new person!");
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        System.out.println("Got: " + message);
    }
    
    public void broadcastJSONString(String json) {
        sessions.forEach((session) -> {
            send(session, json);
        });
    }
    
    public void send(Session session, String message) {
        try {
            session.getRemote().sendString(message);
        } catch (IOException ex) {
            Logger.getLogger(WebSocketHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
