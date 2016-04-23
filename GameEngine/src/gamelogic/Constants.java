/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

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
    public static int width = 500; // grid width 
    public static int height = 500; // grid height
    public static int researchTechCap = 30; // can't attain more tech than this by research alone
    public static int energyCap = 500; // can't accumulate more energy than this
    public static int perSpeciesCap = 1000; // won't allow more spawns than this
    public static boolean chatter = false; // false suppresses alien debugOut
    public static int safeZoneSize = 10; // Number of tiles out of 0,0 that should be protected
                                        // sZS = 0 gives a 1x1 sz, sZS = 1 gives a 3x3 sz, sZS = 5 gives a 11x11 sz
    public static int fightingCost = 5; // fighting will cost you 5 on top of invested power
    public static int spawningCost = 100; // spawning cost + 100
    public static int defenseCost = 2; // defending costs + 2
    public static double energyGainReducer = 30; // harvested energy will be divided by this factor
    public static double starEnergyPerLuminosity= 5; // factor for placing energy into space around star

}
