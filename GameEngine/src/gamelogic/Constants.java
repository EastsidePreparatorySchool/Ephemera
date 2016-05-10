/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import java.lang.reflect.Field;

/**
 *
 * @author gmein
 */
//
// to set these from ephemera_initial_config.csv, use
// element type CONSTANT
// element class for name
// element state for value
//
public class Constants {

    public static int randSeed = 1; // rand seed, controls random decision sequence in the game, set to 0 for fresh
    public static int researchTechCap = 30; // can't attain more tech than this by research alone
    public static int energyCap = 1000; // can't accumulate more energy than this
    public static int perSpeciesCap = 1000; // won't allow more aliens of a species than this
    public static int perSpeciesSpawnCap = 2000; // won't allow more aliens of a species than this
    public static boolean chatter = false; // false suppresses alien debugOut
    public static int safeZoneRadius = 10; // Number of tiles out of 0,0 that should be protected
    // sZS = 0 gives a 1x1 sz, sZS = 1 gives a 3x3 sz, sZS = 5 gives a 11x11 sz
    public static int fightingCost = 5; // fighting will cost you 5 on top of invested power
    public static int spawningCost = 100; // spawning cost + 100
    public static int defenseCost = 2; // defending costs + 2
    public static int viewCost = 2; // views cost 2 + size
    public static double energyGainReducer = 30; // harvested energy will be divided by this factor
    public static double starEnergyPerLuminosity = 5; // factor for placing energy into space around star
    public static boolean autoStart = false; // start without splashscreen or start button

    public static double deathThreshold = 5; // if you lose a fight by more than this much, you die
    public static String filters = "death; violation";
    public static boolean pauseOnError = true; // debugErr stops the show

    /*
    ** Not truly adjustable yet - do not change these, do not put in config file
     */
    final public static int width = 501; // grid width 
    final public static int height = 501; // grid height

    public static Object getValue(String variable) {
        try {
            for (Field field : Constants.class.getDeclaredFields()) {
                if (variable.equalsIgnoreCase(field.getName())) {
                    return field.get(Constants.class);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

}
