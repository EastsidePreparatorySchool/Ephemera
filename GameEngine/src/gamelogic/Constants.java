/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
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

    public static String gameMode = "Brawl"; // Brawl, Play, or a specific mission file
    public static int maxTurns = 20000; // in Brawl mode, stop after 20000 turns
    public static int randSeed = 1; // rand seed, controls random decision sequence in the game, set to 0 for fresh
    public static boolean iSaidNoFighting = false; // for "play" mode, fights are useless
    public static int researchTechCap = 30; // can't attain more tech than this by research alone
    public static int energyCap = 1000; // can't accumulate more energy than this
    public static int perSpeciesCap = 1000; // won't allow more aliens of a species than this
    public static int perSpeciesSpawnCap = 2000; // won't allow more aliens of a species than this
    public static boolean chatter = false; // false suppresses alien debugOut
    public static int safeZoneRadius = 10; // distance from Sol in which there is no fighting, period
    public static int fightingCost = 5; // fighting will cost you 5 on top of invested power
    public static int spawningCost = 100; // spawning cost + 100
    public static int defenseCost = 2; // defending costs + 2
    public static int viewCost = 2; // views cost 2 + size
    public static double energyGainReducer = 30; // harvested energy will be divided by this factor
    public static double starEnergyPerLuminosity = 5; // factor for placing energy into space around star
    public static boolean autoStart = false; // start without splashscreen or start button
    public static int emptySpaceEnergy = 1; // how much can be gained in empty space, can be negative (mission mode)

    public static double deathThreshold = 5; // if you lose a fight by more than this much, you die
    public static String filters = "death; violation";
    public static boolean pauseOnError = true; // debugErr stops the show
    public static boolean searchParentForAliens = true; // searches whole user folder in addition to aliens folder
    public static int shapeComplexityLimit = 5000; // max number of faces for custom aliens shapes

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
