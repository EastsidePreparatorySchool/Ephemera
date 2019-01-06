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
import org.eastsideprep.spacecritters.gameengineimplementation.GameEngineV2;

@WebSocket
public class WebSocketHandler {
    public static class Record {
        String type;
    }
    public static class MessageRecord extends Record {
        String content;
        MessageRecord(String s) {
            content = s;
            type = "MessageRecord";
        }
    }
    public static class SocketError extends Throwable {
        String content;
        SocketError(String s) {
            content = s;
        }
    }
    
    
    
    private static final Map<String, WebVisualizer> visMap = new ConcurrentHashMap<>();
    private static final Map<WebVisualizer, Queue<SocketClient>> clientsFromVis = new ConcurrentHashMap<>();
    private final JSONRT jsonConverter = new JSONRT();
    
    
    public WebSocketHandler() {}
    public void broadcastString(String json, WebVisualizer vis) {
        clientsFromVis.get(vis).forEach((SocketClient client) -> {
            send(client.session, json);
        });
    }
    
    public void broadcastJSON(Object o, WebVisualizer vis) {
        broadcastString(jsonConverter.render(o), vis);
    }
    public void broadcastJSON(Object[] o, WebVisualizer vis) {
        broadcastString(jsonConverter.render(o), vis);
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
    
    
    
    
    
    
    
    @OnWebSocketConnect
    public void connected(Session session) {
        SocketClient client = new SocketClient(session);
        SocketClient.map.put(session, client);
        send(session, new MessageRecord("HANDSHAKE:" + client.identifier));
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        System.out.println("Lost connection to a client: " + reason);
        
        SocketClient client = SocketClient.map.get(session);
        
        SocketClient.map.remove( session );
        if (client.engine != null) {
            try {
                WebVisualizer vis = visMap.get(client.engine);
                clientsFromVis.get(vis).remove(client);
            } catch (Exception e) {}
        }
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        System.out.println("Socket Got: " + message);
        String action = message.split(":")[0];
        
        SocketClient client = SocketClient.map.get(session);
         try {
            switch(action) {
                case "GETSTATE":
                    sendState(client);
                    break;
                case "ATTACH":
                    attach(client, message);
                    break;
            }
        } catch (SocketError e) {
            send(client.session, new MessageRecord("ERROR:" + e.content));
        }
    }
    
    
    private void sendState(SocketClient client) throws SocketError {
        String engine = client.engine;
        try {
            WebVisualizer webVisualizer = visMap.get(engine);
            
            send(client.session, webVisualizer.objectState.toArray());
            send(client.session, webVisualizer.speciesState.toArray());
            //send(session, webVisualizer.energyState.toArray());
            send(client.session, webVisualizer.alienState.toArray());
        } catch (Exception e) {
            throw new SocketError("Cannot Get State: Not Connected");
        }
    }
    
    private void attach(SocketClient client, String message) throws SocketError {
        String[] args = message.split(":");
        try {
            
            String engineName = args[1];
            client.engine = engineName;
            WebVisualizer vis = visMap.get(engineName);
            clientsFromVis.get(vis).add(client);
            
            send(client.session, new WebSocketHandler.MessageRecord("ATTACHED:" + engineName));
        } catch (Exception e) {
            e.printStackTrace();
            throw new SocketError("Invalid Command");
        }
    }
    
    
    public void addEngine(GameEngineV2 engine) {
        //create visualizer, connect to engine
        WebVisualizer webVisualizer = new WebVisualizer();
        webVisualizer.init();
        engine.attachVisualizer(webVisualizer);
        
        
        visMap.put(engine.name, webVisualizer);
        clientsFromVis.put(webVisualizer, new ConcurrentLinkedQueue<SocketClient>());
    }
    
    
    
    
    
    
    
    
    
}
