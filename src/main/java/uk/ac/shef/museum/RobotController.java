/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.museum;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.robokind.api.animation.Animation;
import org.robokind.api.animation.messaging.RemoteAnimationPlayerClient;
import org.robokind.api.common.position.NormalizedDouble;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.messaging.RemoteRobot;
import org.robokind.api.speech.SpeechJob;
import org.robokind.api.speech.messaging.RemoteSpeechServiceClient;
import org.robokind.api.speech.utils.DefaultSpeechJob;
import org.robokind.client.basic.Robokind;
import org.robokind.client.basic.UserSettings;

/**
 *
 * @author samf
 */
public class RobotController {

    public RemoteRobot myRobot;
    Robot.RobotPositionMap myGoalPositions;
    Robot.JointId neck_yaw;
    Robot.JointId neck_pitch;
    RemoteSpeechServiceClient mySpeaker;
    public RemoteAnimationPlayerClient myPlayer;
    DefaultSpeechJob currentSpeechJob = null;
    Animation waveAnim;
    public RobotController(String robotIP) {
      
        String robotID = "myRobot";

        // set respective addresses


        UserSettings.setRobotId(robotID);
        UserSettings.setRobotAddress(robotIP);
        UserSettings.setSpeechAddress(robotIP);
        UserSettings.setAnimationAddress(robotIP);

        waveAnim = Robokind.loadAnimation("wave-anim.xml");
        mySpeaker = Robokind.connectSpeechService();
        myPlayer = Robokind.connectAnimationPlayer();
        myRobot = Robokind.connectRobot();
        myGoalPositions = new org.robokind.api.motion.Robot.RobotPositionHashMap();
        myGoalPositions = myRobot.getDefaultPositions();
        myRobot.move(myGoalPositions, 1000);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(RobotController.class.getName()).log(Level.SEVERE, null, ex);
        }
        myGoalPositions.clear();
    }
    public void addGoalPosition(org.robokind.api.motion.Robot.JointId jointID, NormalizedDouble val) {
        myGoalPositions.put(jointID, val);
    }


    void moveGoalPositions() {
        if (myGoalPositions.isEmpty()) {
            return;
        }
       
        System.out.println(myGoalPositions);
        myRobot.move(myGoalPositions, 200);
        myGoalPositions.clear();
    }
    
    void speak(String text) {
        currentSpeechJob = (DefaultSpeechJob)mySpeaker.speak(text);
        
   }
    
    void stopSpeaking() {
        if (currentSpeechJob!=null) {
          if (currentSpeechJob.getStatus()==DefaultSpeechJob.RUNNING) {
             // currentSpeechJob.cancel();
             // mySpeaker.stop();
               currentSpeechJob = null;
          }
        }
    }
    void setDefaultPositions() {
        myGoalPositions = myRobot.getDefaultPositions();
        myRobot.move(myGoalPositions, 1000);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MuseumApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        myGoalPositions.clear();
    
    }

    void speakWav(String text) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void playAnim(String animName) {
        Animation anim = Robokind.loadAnimation(animName+".xml");
        myPlayer.playAnimation(anim);
    }
    
}
