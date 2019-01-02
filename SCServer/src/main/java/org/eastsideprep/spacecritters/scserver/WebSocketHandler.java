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
import com.google.gson.Gson;

@WebSocket
public class WebSocketHandler {
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    public Thread updateLoop;
    private final JSONRT jsonConverter = new JSONRT();
    private WebVisualizer webVisualizer;
    
    
    public WebSocketHandler() {
    }
    
    @OnWebSocketConnect
    public void connected(Session session) {
        sessions.add(session);
        send(session, "Hello new person!");
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        System.out.println("Lost connection to a client");
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        System.out.println("\n\n\n\nGot: " + message);
        switch(message) {
            case "GETSTATE":
                send(session, webVisualizer.objectState.toArray());
                send(session, webVisualizer.speciesState.toArray());
                //send(session, webVisualizer.energyState.toArray());
                send(session, webVisualizer.alienState.toArray());
                break;
        }
    }
    
    public void broadcastString(String json) {
        sessions.forEach((session) -> {
            send(session, json);
        });
    }
    
    public void broadcastJSON(Object o) {
        broadcastString(jsonConverter.render(o));
    }
    public void broadcastJSON(Object[] o) {
        broadcastString(jsonConverter.render(o));
    }
    
    public void send(Session session, String message) {
        try {
            session.getRemote().sendString(message);
        } catch (IOException ex) {
            System.out.println("Exception in websocket send:");
            System.out.println(ex);
        }
    }
    public void send(Session session, Object o) {
        send(session, jsonConverter.render(o));
    }
    public void send(Session session, Object[] o) {
        send(session, jsonConverter.render(o));
    }
    
    
    /*public void beginUpdateLoop() {
        updateLoop = new Thread(() -> { update(); });
        updateLoop.run();
    }
    public void update() {
        //List<GameLogEntry> list = observer.getNewItems();
        //broadcastJSONString(jsonConverter.render(list));
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            System.out.println("Exception in update loop:");
            System.out.println(ex);
        }
        update();
    }*/
}
