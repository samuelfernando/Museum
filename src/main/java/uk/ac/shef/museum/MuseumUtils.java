/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.museum;

import com.primesense.nite.JointType;
import com.primesense.nite.Point3D;
import com.primesense.nite.Skeleton;
import com.primesense.nite.SkeletonJoint;
import com.primesense.nite.SkeletonState;
import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.util.data.audio.AudioPlayer;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.JointId;
import org.robokind.api.motion.messaging.RemoteRobot;
import org.robokind.api.speech.utils.DefaultSpeechJob;

/**
 *
 * @author samf
 */
public class MuseumUtils {

    long lastSpeak;
    long lastUpdate;
    Point3f centerPoint;
    Stack<Skeleton> logPos;
    long startTime;
    DecimalFormat df;
    DecimalFormat posSpeak;
    PrintStream skeletonOut;
    UserTracker mTracker;
    PositionPanel posPanel;
    MaryInterface marytts;
    long endOfSpeech;
    boolean robotActive;
    RobotController robotController;
    boolean playWav;
    HashMap<String, String> textMap;
    boolean robotSpeechPendingComplete;
    AudioPlayer audioPlayer;
    SimpleDateFormat dateFormat;
    PrintStream robotOut;
    boolean faceExpr;
        float matFront;
    int matNoX;
    int matNoY;
    float matSize;

    
    public MuseumUtils() {
            HashMap<String, String> configs = ReadConfig.readConfig();
       
       robotSpeechPendingComplete = false;
       playWav = false;
       String wavConf = configs.get("wav-tts");
        if (wavConf!=null && wavConf.equals("wav")) {
            playWav = true;
        }
        faceExpr = Boolean.parseBoolean(configs.get("face-expr"));
        textMap = new HashMap<String, String>();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss.SSS");
        try {
            BufferedReader r = new BufferedReader(new FileReader("textMap.txt"));
            String line;
            while ((line=r.readLine())!=null) {
                String[] split = line.split("\t");
                //textMap.put(split[0], split[1]);
            }
        } catch (Exception ex) {
            Logger.getLogger(MuseumUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        endOfSpeech = lastUpdate = lastSpeak = startTime = System.currentTimeMillis();
        df = new DecimalFormat("#.##");
        centerPoint = new Point3f(0, 1320, 2000);
        logPos = new Stack<Skeleton>();
        long currentTime = System.currentTimeMillis();
                matFront = Float.parseFloat(configs.get("mat-front"));
        matSize = Float.parseFloat(configs.get("mat-size"));
        matNoX = Integer.parseInt(configs.get("mat-dimno-x"));
        matNoY = Integer.parseInt(configs.get("mat-dimno-y"));

        try {
            marytts = new LocalMaryInterface();
            Set<String> voices = marytts.getAvailableVoices();
            marytts.setVoice(voices.iterator().next());
            String timeNow = dateFormat.format(new Date(currentTime));
            skeletonOut = new PrintStream("C:\\Users\\samf\\Documents\\NetBeansProjects\\skeleton-logs\\skeletonOut-"+timeNow+".txt");
            robotOut = new PrintStream("C:\\Users\\samf\\Documents\\NetBeansProjects\\robot-logs\\robotOut-"+timeNow+".txt");
                
            
            robotActive = Boolean.parseBoolean(configs.get("robot-active"));
            if (robotActive) {
                robotController = new RobotController(configs.get("ip"), robotOut);
                }
            posSpeak = new DecimalFormat("#");
        } catch (Exception ex) {
            Logger.getLogger(UserViewer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public MuseumUtils(UserTracker tracker, PositionPanel panel) {
        this();
        posPanel = panel;
        mTracker = tracker;
    
    }

    void stopTrackingAllOtherUsers(List<UserData> users, short id) {
        for (UserData user : users) {
            if (user.getId() != id) {
                mTracker.stopSkeletonTracking(user.getId());
            }
        }
    }

    boolean inPlayZone(UserData user) {
        boolean ret = false;

        long now = System.currentTimeMillis();

        Skeleton skeleton = user.getSkeleton();
        SkeletonState skelState = skeleton.getState();
        if (skelState == SkeletonState.TRACKED) {
            //System.out.println("Tracking user");
            com.primesense.nite.SkeletonJoint joint = skeleton.getJoint(JointType.HEAD);

            Point3D<Float> position = joint.getPosition();
            Point3f pos = convertPoint(position);
            Vector3f dist = new Vector3f();
            dist.x = pos.x - centerPoint.x;
            dist.y = pos.z - centerPoint.z;
            //if (now - lastSpeak > 2000) {
                float d = dist.length();
                if (d < 850) {
                    //speak("In");
                    ret = true;
                } else {
              //      System.out.println("Outside zone");
                    // speak("Out");
                }
                lastSpeak = now;
            //}


        } else {
            //System.out.println("Not tracking user");
            // not yet tracked
        }



        return ret;

    }
    
  boolean newPlayZone(UserData user) {
        boolean ret = false;

        long now = System.currentTimeMillis();

        Skeleton skeleton = user.getSkeleton();
        SkeletonState skelState = skeleton.getState();
        if (skelState == SkeletonState.TRACKED) {
            //System.out.println("Tracking user");
            com.primesense.nite.SkeletonJoint joint = skeleton.getJoint(JointType.HEAD);

            Point3D<Float> position = joint.getPosition();
            Point3f kinectUser = convertPoint(position);
            //Point3f robotUser = kinectUser;
            //System.out.println("robotUser = " + robotUser);

            float x = kinectUser.x;
            float z = kinectUser.z;
            
            float matBack = matFront + matSize*matNoY;
            float matLeft = -matSize/2 * matNoX;
            float matRight = matSize/2 * matNoX;
 
            if (matLeft<=x && x<=matRight && matFront<=z && z<=matBack) {
                if (speechFinished()) {
                    //speak("In");
                }
                ret = true;
            }
            else {
                if (speechFinished()) {
                    //speak("Out");
                }
                ret = false;
            }

            /*Vector3f dist = new Vector3f();
             dist.x = pos.x - centerPoint.x;
             dist.y = pos.z - centerPoint.z;
             //if (now - lastSpeak > 2000) {
             float d = dist.length();
             if (d < 850) {
             //speak("In");
             ret = true;
             } else {
             //      System.out.println("Outside zone");
             // speak("Out");
             }
             lastSpeak = now;
             //}

             */
        } else {
            //System.out.println("Not tracking user");
            // not yet tracked
        }



        return ret;


    }


    void speak(String text) {               
        if (robotActive) {
            if (playWav) {
                robotWav(text);
            }
            else {
                robotSpeak(text);
            }
        }
        else {
            if (playWav) {
                localWav(text);
            }
            else {
                localSpeak(text);
            }
        }
    }
    
    void localWav(String text) {
        try {
            long now = System.currentTimeMillis();

            File soundFile = new File( "AllAlone.wav" );
            AudioInputStream audio = AudioSystem.getAudioInputStream( soundFile );
            AudioFormat format = audio.getFormat();
            long audioFileLength = audio.getFrameLength();

            //int frameSize = format.getFrameSize();
            float frameRate = format.getFrameRate();
            float durationInSeconds = (audioFileLength / frameRate);
            long length = (long) (durationInSeconds * 1000);
           
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
            lastSpeak = now;
            endOfSpeech = now + length + 500;
   
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void robotSpeak(String text) {
        robotController.speak(text);
    }
    void robotWav(String text) {
        robotController.speakWav(text);
    }
    
    void localSpeak(String text) {
        try {

            AudioInputStream audio = marytts.generateAudio(text);
            AudioFormat format = audio.getFormat();
            long audioFileLength = audio.getFrameLength();

            //int frameSize = format.getFrameSize();
            float frameRate = format.getFrameRate();
            float durationInSeconds = (audioFileLength / frameRate);
            long length = (long) (durationInSeconds * 1000);
            audioPlayer = new AudioPlayer(audio);
            audioPlayer.start();

            // player.join();
            System.out.println("length = " + length);
            long now = System.currentTimeMillis();
            lastSpeak = now;
            endOfSpeech = now + length + 500;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void makeRequest(String text, Action action) {
        if (robotActive) {
            robotController.playAnim(action.getAnim());
            robotController.speak(text);
        }
        else {
            localSpeak(text);
        }
    
    }
    void stopSpeaking() {
        if (robotActive) {
            robotController.stopSpeaking();
        } else {
            if (audioPlayer!=null && audioPlayer.isAlive()) {
                audioPlayer.cancel();
            }
        }
    }
    long timeSinceLastSpeak() {
        long now = System.currentTimeMillis();
        return now - lastSpeak;
    }

    boolean speechFinished() {
        long now = System.currentTimeMillis();

        if (robotActive) {
            if (robotController.currentSpeechJob==null) {
                if (robotSpeechPendingComplete) {
                    if (now>endOfSpeech) {
                        robotSpeechPendingComplete = false;
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                else {
                    return true;
                }
            }
            else {
                boolean ret = (robotController.currentSpeechJob.getStatus()==DefaultSpeechJob.COMPLETE);
                if (ret) {
                    this.robotSpeechPendingComplete = true;
                    robotController.currentSpeechJob = null;
                    endOfSpeech = now+500;
                }
                return false;
                
            }
        } else {
            if (audioPlayer!=null) {
                if (audioPlayer.isAlive()) {
                    return (now > endOfSpeech);
                }
                else {
                    return true;
                }
            }
            else {
                return now>endOfSpeech;
               // return true;
            }
            
        }
    }

    void addSkeleton(Skeleton skeleton) {
        logPos.push(skeleton);
    }

    public Point3f convertPoint(com.primesense.nite.Point3D<Float> p) {
        Point3f point = new Point3f();
        point.x = p.getX();
        point.y = p.getY();
        point.z = p.getZ();
        return point;
    }

    void makeLog(UserData user, VisitorState visitorState, PlayState playState) {
        
        long now = System.currentTimeMillis();
        com.primesense.nite.SkeletonJoint joint = user.getSkeleton().getJoint(JointType.HEAD);

        Point3D<Float> position = joint.getPosition();
        Point3f pos = convertPoint(position);

        Vector3f dist = new Vector3f();
        dist.x = pos.x - centerPoint.x;
        dist.y = pos.z - centerPoint.z;
        float x = position.getX();
        float y = position.getY();
        float z = position.getZ();
        //float totalElapsed = (float) (now - startTime) / 1000;

        if (now - lastUpdate > 200) {
            //speak(" x "+ df.format(x/1000) + " y "+df.format(y/1000)+" z "+df.format(z/1000));
            //speak(" x "+ (int)Math.round(x/10) + " y "+(int)Math.round(y/10)+" z "+(int)Math.round(z/10));
            posPanel.setPosition(position);
            posPanel.setVar("dist from center", dist.length());
            //out.println("User " + user.getId() + " time " + df.format(totalElapsed)+ " x " + (int) Math.round(x / 10) + " y " + (int) Math.round(y / 10) + " z " + (int) Math.round(z / 10));
            lastUpdate = now;
        }
        Date date = new Date(now);
        String timeNow = dateFormat.format(date);
        robotOut.println(timeNow+"\tVisitorState\t"+visitorState+"\tPlayState\t"+playState);
         
        for (SkeletonJoint skelJoint : user.getSkeleton().getJoints()) {
            skeletonOut.println("User"+user.getId()+"\t"+timeNow+"\t"+skelJoint.getJointType()+"\t"+skelJoint.getPosition().getX()+"\t"+skelJoint.getPosition().getY()+"\t"+skelJoint.getPosition().getZ()+"\t"+skelJoint.getOrientation().getW()+"\t"+skelJoint.getOrientation().getX()+"\t"+skelJoint.getOrientation().getY()+"\t"+skelJoint.getOrientation().getZ()+"\t"+skelJoint.getPositionConfidence()+"\t"+skelJoint.getOrientationConfidence());//+"\t"+user.getSkeleton().getJoints());
        }
       /*if (robotActive) {
            RemoteRobot robot = robotController.myRobot;
            Robot.RobotPositionMap robotPositions = robot.getGoalPositions();
            Set<JointId> robotJoints = robotPositions.keySet();
            for (JointId robotJoint : robotJoints) {
                robotOut.println("RobotPos\t"+timeNow+"\t"+robotJoint+"\t"+robotPositions.get(robotJoint));
            }
            
        }*/
        
    }

    boolean checkAction(Action action) {
        if (action == Action.WAVE) {
            return hasUserWaved();
        }
        else if (action == Action.JUMP) {
            return hasUserJumped();
        }
        else if (action == Action.HANDS_UP) {
            return hasUserPutHandsUp();
        }
        return false;
    }
    
    boolean hasUserJumped() {
        float avgSpeed = 0.0f;
        int count = 0;
        Point3f oldhp = new Point3f();
        while (!logPos.isEmpty()) {
            Skeleton skel = logPos.pop();
            com.primesense.nite.SkeletonJoint head = skel.getJoint(JointType.HEAD);
            Point3f hp = convertPoint(head.getPosition());
            
            if (count > 0) {
                Vector3f vel = new Vector3f();
                vel.sub(hp, oldhp);
                avgSpeed += vel.y*vel.y;
            }
            oldhp = hp;
            ++count;
        }
        avgSpeed = avgSpeed / count;
        int speed = (int) Math.round(avgSpeed * 10);
        //speak("Speed was " + speed);
        return (avgSpeed > 50);
    }
    boolean hasUserPutHandsUp() {
        float avgYPos = 0.0f;
        int count = 0;
        
        while (!logPos.isEmpty()) {
            Skeleton skel = logPos.pop();
            com.primesense.nite.SkeletonJoint leftHand = skel.getJoint(JointType.LEFT_HAND);
            com.primesense.nite.SkeletonJoint rightHand = skel.getJoint(JointType.RIGHT_HAND);
            com.primesense.nite.SkeletonJoint leftHip = skel.getJoint(JointType.LEFT_HIP);
            com.primesense.nite.SkeletonJoint rightHip = skel.getJoint(JointType.RIGHT_HIP);
 
            Point3f lhand = convertPoint(leftHand.getPosition());
            Point3f rhand = convertPoint(rightHand.getPosition());
            Point3f lhip = convertPoint(leftHip.getPosition());
            Point3f rhip = convertPoint(rightHip.getPosition());
            Vector3f lhdist = new Vector3f();
            lhdist.sub(lhand, lhip);
            Vector3f rhdist = new Vector3f();
            rhdist.sub(rhand, rhip);
            avgYPos += lhdist.y;
            avgYPos += rhdist.y;
            ++count;
        }
        avgYPos = avgYPos / count;
        int yPos = (int) Math.round(avgYPos);
        //speak("Position equals " + yPos);
        return (avgYPos > 0);
        //return false;
    }

    boolean hasUserWaved() {
        float avgSpeed = 0.0f;
        int count = 0;
        Point3f oldlh = new Point3f();
        Point3f oldrh = new Point3f();
        while (!logPos.isEmpty()) {
            Skeleton skel = logPos.pop();
            com.primesense.nite.SkeletonJoint leftHand = skel.getJoint(JointType.LEFT_HAND);
            com.primesense.nite.SkeletonJoint rightHand = skel.getJoint(JointType.RIGHT_HAND);
            Point3f lh = convertPoint(leftHand.getPosition());
            Point3f rh = convertPoint(rightHand.getPosition());

            if (count > 0) {
                Vector3f lhvel = new Vector3f();
                lhvel.sub(lh, oldlh);
                Vector3f rhvel = new Vector3f();
                rhvel.sub(rh, oldrh);
                avgSpeed += lhvel.length();
                avgSpeed += rhvel.length();
            }
            oldlh = lh;
            oldrh = rh;
            ++count;
        }
        avgSpeed = avgSpeed / count;
        int speed = (int) Math.round(avgSpeed * 10);
        //speak("Speed was " + speed);
        return (avgSpeed > 15);
    }

    void addReport(UserData user) {
        String report;
        short id = user.getId();
        float totalElapsed = (float) (System.currentTimeMillis() - startTime) / 1000;

        report = "User " + id + " time " + df.format(totalElapsed);
        if (user.isNew()) {
            // start skeleton tracking
            //mTracker.startSkeletonTracking(id);

            report += " New";
        } else {
            report += " Existing";
        }
        if (user.isLost()) {
            //mTracker.stopSkeletonTracking(id);
            report += " Lost";
        }
        if (user.isVisible()) {
            report += " Visible";
        } else {
            //mTracker.stopSkeletonTracking(id);
            report += " NotVisible";
        }
        skeletonOut.println(report);


    }

    public static void main(String args[]) {
        MuseumUtils mu = new MuseumUtils();
        //mu.robotSpeak("simon says wave your hands");
       // mu.robotSpeak("simon says jump up and down");
        // mu.robotSpeak("simon says put your hands up!");
       // mu.robotSpeak("No you got that wrong.");
      //   mu.robotSpeak("Yes you got that right!");
        
        System.exit(0);
    }

    void giveFeedback(String toSpeak, Emotion emotion) {
        if (robotActive) {
            if (faceExpr) {
                if (emotion==Emotion.Positive) {
                    robotController.playAnim("victory");
                }
                else if (emotion==Emotion.Negative) {
                    robotController.playAnim("disappointed");
                }
            }
            
        }
        speak(toSpeak);
    
    }

    void resetExpression() {
        if (robotActive) {
            if (faceExpr) {
                robotController.setDefaultPositions();
            }
        }
    }

 }
