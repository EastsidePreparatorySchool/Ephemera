/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.scserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.Session;

/**
 *
 * @author qbowers
 */
public class SocketClient {
    public static Map<Session, SocketClient> map = new ConcurrentHashMap<>();
    
    public String engine;
    public final String identifier;
    public boolean admin;
    public boolean debug;
    
    final public Session session;
    SocketClient(Session session) {
        this.session = session;
        map.put(session, this);
        identifier = session.getRemoteAddress().toString();
    }
    
    
}
