/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.scserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import org.eastsideprep.spacecritters.alieninterfaces.AlienShapeFactory;
import org.eastsideprep.spacecritters.alieninterfaces.Orbit;
import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.SpaceObject;
import org.eastsideprep.spacecritters.alieninterfaces.WorldVector;
import org.eastsideprep.spacecritters.gameengineinterfaces.AlienSpec;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameState;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameVisualizer;
import org.eastsideprep.spacecritters.orbit.Conic;
import org.eastsideprep.spacecritters.orbit.DummyTrajectory;
import org.eastsideprep.spacecritters.orbit.Trajectory;

/**
 *
 * @author qbowers
 */



//grand plan:
//when visualizer gets new listener or game starts, spit out a list of states to relevant parties
    //all objects
    //all species
    //all aliens
    //all energies
//on new turn
    //emit updates

//on messages from gameengine, update states and updates accordingly




public class WebVisualizer implements GameVisualizer {
    WebSocketHandler webSocket = new WebSocketHandler();
    
    
    HashMap<String, ObjectRecord> objectMap = new HashMap<>();
    ArrayList<ObjectRecord> objectState = new ArrayList<>();
    ArrayList<SpeciesRecord> speciesState = new ArrayList<>();
    ArrayList<AlienSpec> alienState = new ArrayList<>();
    ArrayList<EnergyRecord> energyState = new ArrayList<>();
    
    Stack<WebSocketHandler.Record> updates = new Stack<>();
    
    
    
    public abstract class ObjectRecord extends WebSocketHandler.Record {
        double x, y, mass;
        String name;
        int index; //independent among all objects
        ObjectRecord(double x, double y, String name, int index, double mass) {
            this.x = x;
            this.y = y;
            this.mass = mass;
            this.index = index;
            this.name = name;
            
            objectMap.put(name, this);
        }
    }
    public class StarRecord extends ObjectRecord {
        double luminosity;
        StarRecord(double x, double y, String name, int index, double luminosity, double mass) {
            super(x,y,name,index,mass);
            this.luminosity = luminosity;
            
            type = "StarRecord";
        }
    }
    public class PlanetRecord extends ObjectRecord {
        int tech;
        double energy;
        OrbitRecord orbit;
        PlanetRecord(double x, double y, String name, int index, double energy, int tech, double mass, Trajectory t) {
            super(x,y,name,index,mass);
            this.tech = tech;
            this.energy = energy;
            this.orbit = new OrbitRecord(t.orbit());
            
            type = "PlanetRecord";
        }
    }
    public class PlanetUpdate extends PlanetRecord {
        PlanetUpdate(double x, double y, String name, int index, double energy, int tech) {
            super(x, y, name, index, energy, tech, 0, null);
            
            type = "PlanetUpdate";
        }
    }
    
    
    public class OrbitRecord extends WebSocketHandler.Record {
        public ObjectRecord focus;  // star or planet
        public double p;            // semi-latus rectum
        public double e;            // eccentricity
        public double rotation;     // 
        public double signum;       // 1.0 = counter-clockwise
        public double mu;           // G*M
        public double h;            // angular momentum
        OrbitRecord(Conic.Orbit orbit){
            this.p = orbit.p;
            this.e = orbit.e;
            this.rotation = orbit.rotation;
            this.signum = orbit.signum;
            this.mu = orbit.mu;
            this.h = orbit.h;
            
            String name = orbit.focus.className; 
            this.focus = objectMap.get(name);
            
            type = "OrbitRecord";
        }
    }
    
    
    
    
    
    public class AlienRecord extends WebSocketHandler.Record {//needs fixing
        AlienRecord() {
            
            type = "AlienRecord";
        }
        
    }
    
    public class SpeciesRecord extends WebSocketHandler.Record {
        String domainName;
        String packageName;
        String className;
        int speciesID;
        String fullName;
        String speciesName;
        
        /*SpeciesRecord() {
            type = "SpeciesRecord";
        }*/
        
        SpeciesRecord(AlienSpec as, AlienShapeFactory asf, boolean instantiate) {
            domainName = as.domainName;
            packageName = as.packageName;
            className = as.className;
            speciesID = as.speciesID;
            fullName = as.fullName;
            speciesName = as.speciesName;
            type = "SpeciesRecord";
        }
    }
    public class FightRecord extends WebSocketHandler.Record {
        int x, y;
        FightRecord(int x, int y) {
            this.x = x;
            this.y = y;
            
            type = "FightRecord";
        }
    }
    public class SpawnRecord extends WebSocketHandler.Record { //need fixing
        SpawnRecord() {
            
            type = "SpawnRecord";
        }
        
    }
    public class MoveRecord extends WebSocketHandler.Record { //need fixing
        MoveRecord() {
            
            type = "MoveRecord";
        }
    }
    public class KillRecord extends WebSocketHandler.Record { //need fixing
        KillRecord() {
            
            type = "KillRecord";
        }
        
    }
    public class EnergyRecord extends WebSocketHandler.Record {
        int x, y;
        double energy;
        EnergyRecord(int x, int y, double energy) {
            this.x = x;
            this.y = y;
            this.energy = energy;
            
            type = "EnergyRecord";
        }
    }
    public class TurnRecord extends WebSocketHandler.Record {
        int totalTurns, numAliens;
        long timeTaken;
        double avgTime;
        TurnRecord(int totalTurns, int numAliens, long timeTaken, double avgTime) {
            this.totalTurns = totalTurns;
            this.numAliens = numAliens;
            this.timeTaken = timeTaken;
            this.avgTime = avgTime;
            
            type = "TurnRecord";
        }
    }
    
    
    
    
    
    
    //Record generators:
    
    @Override
    public void showPlanetMove(int oldx, int oldy, int x, int y, String name, int index, double energy, int tech, double time) {
        //make a new objectrecord, throw in there
        //new PlanetUpdate(x, y, name, index, energy, tech);
        
    }
    
    @Override
    public void showMove(AlienSpec as, double oldX, double oldY, double energyAtNewPosition, double energyAtOldPosition, boolean update, Trajectory t, double time) {
        //create new alien record, add to queue
        
    }

    @Override
    public void showAcceleration(int id, Position p, WorldVector worldDirection) {
        //create new thrustrecord, add to queue
        
    }
    
    @Override
    public void showSpawn(AlienSpec as, double energyAtPos, Trajectory t) {
        //create new spawnrecord, add to queue
        
    }

    @Override
    public void showDeath(AlienSpec as, double energyAtPos) {
        //create new deathrecord, add to queue
        
    }
    
    
    
    //DONE
    
    //STATE CHANGES
    @Override
    public void registerSpecies(AlienSpec as, AlienShapeFactory asf, boolean instantiate) {
        speciesState.add(new SpeciesRecord(as, asf, instantiate));
    }
    
    HashMap<Integer, HashMap<Integer, Double>> energyMap = new HashMap<>();
    
    @Override
    public void mapEnergy(int x, int y, double energy) {
        energyState.add(new EnergyRecord(x,y,energy));
    }
    
    @Override
    public void registerStar(int x, int y, String name, int index, double luminosity, double mass) {
        //add star to state
        StarRecord r = new StarRecord(x,y,name, index, luminosity, mass);
        objectMap.put(name, r);
        objectState.add(r);
    }
    @Override
    public void registerPlanet(int x, int y, String name, int index, double energy, int tech, double mass, Trajectory t) {
        //add planet to state
        PlanetRecord r = new PlanetRecord(x,y,name,index,energy,tech,mass,t);
        objectMap.put(name, r);
        objectState.add(r);
    }
    
    //UPDATES
    @Override
    public void showFight(int x, int y) {
        updates.push(new FightRecord(x,y));
    }
    
    
    
    
    
    
    
    
    
    
    @Override
    public void init() {
        //what does this need?
    }

    @Override
    public void shutdown() {
        webSocket.broadcastJSON(new WebSocketHandler.MessageRecord("SHUTDOWN"), this);
    }
    
    @Override
    public void showReady() {
        System.out.println("objects: " + objectState.size());
        webSocket.broadcastJSON(objectState.toArray(), this);
        System.out.println("species: " + speciesState.size());
        webSocket.broadcastJSON(speciesState.toArray(), this);
        //System.out.println("energy: " + energyState.size());
        //webSocket.broadcastJSON(energyState.toArray());       //NO. TOO MANY ENTRIES
        System.out.println("aliens: " + alienState.size());
        webSocket.broadcastJSON(alienState.toArray(), this);
    }

    @Override
    public void showCompletedTurn(int totalTurns, int numAliens, long timeTaken, double avgTech) {
        System.out.println("Does anyone do this?");
        //add turnrecord to queue
        updates.push( new TurnRecord(totalTurns, numAliens, timeTaken, avgTech) );
        webSocket.broadcastJSON(updates.toArray(), this);
        updates.clear();
    }

    @Override
    public void showIdleUpdate(int numAliens) {
        //what does this need?
    }
    
    @Override
    public boolean showContinuePrompt() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void showEngineStateChange(GameState gs) {
        //what does this need?
    }

    @Override
    public void showUpdateAfterRequestedActions() {
        //what does this need?
    }

    @Override
    public void showGameOver() {
        webSocket.broadcastJSON(new WebSocketHandler.MessageRecord("GAMEOVER"), this);
    }

    @Override
    public void debugOut(String s) {
        webSocket.broadcastJSON(new WebSocketHandler.MessageRecord("DEBUG:" + s), this);
    }

    @Override
    public void debugErr(String s) {
        webSocket.broadcastJSON(new WebSocketHandler.MessageRecord("DEBUGERR:" + s), this);
    }

    @Override
    public void setFilter(String s) {
        //what does this need?
    }

    @Override
    public void setChatter(boolean f) {
        //what does this need?
    }
    
    @Override
    public void showAliens(List<AlienSpec> aliens) {
        //what does this need?
    }
    
}
