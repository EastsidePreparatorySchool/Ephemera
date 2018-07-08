/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.scserver;

import org.eastsideprep.spacecritters.gameengineimplementation.GameEngineV2;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameEngine;
import org.eastsideprep.spacecritters.gamelog.GameLogObserver;

/**
 *
 * @author gunnar
 */
public class ServerContext {
    GameLogObserver observer;
    GameEngineV2 engine;
    
    public ServerContext() {
    }
    
}
