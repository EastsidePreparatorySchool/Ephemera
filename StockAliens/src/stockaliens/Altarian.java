/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockaliens;

import alieninterfaces.*;

// Secure messaging imports
import java.security.Key;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

// Object serialization
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *
 * @author guberti
 */
public class Altarian implements Alien {

    Context ctx;
    int missionId;
    
    // Missions are projects to take over a star
    // First mission is always to take over Sirius
    // Subsequent missions are launched from there
    int[][] missions = {{-2, 164}, {-61, 217}, {222, 61}};
    
    String mode; // Wait, explore, expand, overtake, hold
    String position; // Commander, mC, worker
    
    private static final String ALGO = "AES";
    private static byte[] keyValue;
    
    
    

    final boolean debug = true;

    public Altarian() {
    }

    public void init(Context game_ctx, int id, int parent, String message) {
        ctx = game_ctx;
        mode = "wait";
        position = "commander";
        
        if ("".equals(message)) { // If there is no message, meaning that it is the original Altarian
            mode = "wait";
            position = "commander";
            //keyValue = genKeyValue(); // Generate a random encryption key
            
        }
    }
    

    // Martians move left, right, left, right
    public MoveDir getMove() {

        ctx.debugOut("Move requested,"
                + " E:" + Double.toString(ctx.getEnergy())
                + " T:" + Double.toString(ctx.getTech()));
        switch (mode) {
            case "wait":
                return new MoveDir(0, 0);
            case "explore":
                
                if (ctx.getEnergy() < 6) {
                    // We don't have any energy left to do anything besides defend
                    // Don't move
                    return new MoveDir(0, 0);
                }
                
                // TODO: Add star avoidance code
                //View v = ctx.getView(2);
                if (ctx.getX() > missions[missionId][0]) {
                    return new MoveDir(-1, 0);
                }
                if (ctx.getX() < missions[missionId][0]) {
                    return new MoveDir(1, 0);
                }
                if (ctx.getY() > missions[missionId][1]) {
                    return new MoveDir(0, -1);
                }
                if (ctx.getX() > missions[missionId][1]) {
                    return new MoveDir(0, 1);
                }       
                
            default: 
                return new MoveDir(0, 0);
        }
    }
    
    @SuppressWarnings("restriction")
    public String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = new BASE64Encoder().encode(encVal);
        return encryptedValue;
    }
 
    @SuppressWarnings("restriction")
    public String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }
    private Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }
    public byte[] genKeyValue() {
        int length = 16; // Needs to be 16 for current setup
        Random rand = new Random();
        byte[] kV = new byte[length];
        for (int i = 0; i < length; i++) {
            kV[i] = (byte)ctx.getRandomInt(Integer.MAX_VALUE);
            System.out.print(kV[i]);
        }
        System.out.println();
        return kV;
    }
    
    public Action getAction() {

        ctx.debugOut("Action requested,"
                + " E:" + Double.toString(ctx.getEnergy())
                + " T:" + Double.toString(ctx.getTech()));

        if (ctx.getEnergy() > 100) {
            mode = "explore";
        }
        return new Action (Action.ActionCode.Gain);
        // catch any shenanigans
        /*if (ctx.getEnergy() > 100) {
            try {
                View view = ctx.getView((int) ctx.getTech());
                if (view.getAliensAtPos(ctx.getX(), ctx.getY()).size() > 1) {
                    ctx.debugOut("Uh-oh.There is someone else here."
                            + " E:" + Double.toString(ctx.getEnergy())
                            + " T:" + Double.toString(ctx.getTech())
                            + " X:" + Double.toString(ctx.getX())
                            + " Y:" + Double.toString(ctx.getY()));
                }

                // do we have enough energy?
                if (ctx.getEnergy() < 10) {
                    // no, charge
                    ctx.debugOut("Choosing to gain energy,"
                            + " E:" + Double.toString(ctx.getEnergy())
                            + " T:" + Double.toString(ctx.getTech()));
                    return new Action(Action.ActionCode.Gain);
                }

                // do we have enough tech?
                if (ctx.getTech() < 30 && ctx.getEnergy() > ctx.getTech()) {
                    // no, research
                    ctx.debugOut("Choosing to research"
                            + " E:" + Double.toString(ctx.getEnergy())
                            + " T:" + Double.toString(ctx.getTech()));
                    return new Action(Action.ActionCode.Research);
                }

                // is there another alien on our position?
                if (view.getAliensAtPos(ctx.getX(), ctx.getY()).size() > 1
                        && ctx.getEnergy() > ctx.getFightingCost() + 2) {
                    ctx.debugOut("EXTERMINATE!!!!!"
                            + " E:" + Double.toString(ctx.getEnergy())
                            + " T:" + Double.toString(ctx.getTech())
                            + " X:" + Double.toString(ctx.getX())
                            + " Y:" + Double.toString(ctx.getY()));

                    return new Action(Action.ActionCode.Fight, (int) ctx.getEnergy() - 2 - ctx.getFightingCost());
                }

                if (ctx.getEnergy() > (ctx.getSpawningCost() + 10)) {
                    // no other aliens here, have enough stuff, spawn!
                    ctx.debugOut("DALEKS RULE SUPREME! SPAWNING!"
                            + " E:" + Double.toString(ctx.getEnergy())
                            + " T:" + Double.toString(ctx.getTech()));

                    return new Action(Action.ActionCode.Spawn, 5);
                }
            } catch (Exception e) {
                // do something here to deal with errors
                ctx.debugOut("EXPLAIN?????? " + e.toString());
            }
        }
        ctx.debugOut("Gaining energy"
                + " E:" + Double.toString(ctx.getEnergy())
                + " T:" + Double.toString(ctx.getTech()));
        return new Action(Action.ActionCode.Gain);*/
    }

    @Override
    public void communicate() {
        if (ctx.getEnergy() > 100) {
            try {
                if (ctx.getGameTurn() % 20 == 0) {
                    ctx.broadcastAndListen("I say: Daleks rule supreme!!!!!", 1, true);
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void receive(String[] messages) {
        for (String s : messages) {
            ctx.debugOut(s);
        }
    }

    @Override
    public void beThoughtful() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
