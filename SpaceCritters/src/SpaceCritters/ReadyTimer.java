/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import java.util.TimerTask;

/**
 *
 * @author gunnar
 */
public class ReadyTimer extends TimerTask{
   // this method performs the task
   @Override
   public void run() {
       Utilities.runSafe(()->SpaceCritters.field.renderField());
   }    

}
