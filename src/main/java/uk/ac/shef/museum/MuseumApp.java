/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.museum;

import com.primesense.nite.JointType;
import com.primesense.nite.Point3D;
import com.primesense.nite.Skeleton;
import com.primesense.nite.SkeletonState;
import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.util.data.audio.AudioPlayer;

/**
 *
 * @author samf
 */
class MuseumApp {

    long startTime;
    DecimalFormat df;
    DecimalFormat posSpeak;
    PrintStream out;
    UserTracker mTracker;
    PositionPanel posPanel;
    int state = START;
    final static int START = 0;
    MaryInterface marytts;
    long lastSpeak;
    long lastUpdate;
    Point3f centerPoint;

    public MuseumApp(UserTracker tracker, PositionPanel panel) {
        posPanel = panel;
        mTracker = tracker;
        lastUpdate = lastSpeak = startTime = System.currentTimeMillis();
        df = new DecimalFormat("#.##");
        centerPoint = new Point3f(300, 630, 2000);
        try {
            marytts = new LocalMaryInterface();
            Set<String> voices = marytts.getAvailableVoices();
            marytts.setVoice(voices.iterator().next());
            out = new PrintStream("out-log.txt");
            posSpeak = new DecimalFormat("#");
        } catch (Exception ex) {
            Logger.getLogger(UserViewer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void speak(String text) {
        try {
            AudioInputStream audio = marytts.generateAudio(text);
            AudioPlayer player = new AudioPlayer(audio);
            player.start();
            // player.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        out.println(report);


    }

    void update(List<UserData> users) {
        for (UserData user : users) {
            //addReport(user); 
            if (state == START) {
                boolean inZone = inPlayZone(user);

            }
        }
    }

    boolean inPlayZone(UserData user) {
        boolean ret = false;
        if (user.isNew()) {
            mTracker.startSkeletonTracking(user.getId());
        } else {
            short id = user.getId();
            long now = System.currentTimeMillis();
            float totalElapsed = (float) (now - startTime) / 1000;

            Skeleton skeleton = user.getSkeleton();
            SkeletonState skelState = skeleton.getState();
            if (skelState == SkeletonState.TRACKED) {
             com.primesense.nite.SkeletonJoint joint = skeleton.getJoint(JointType.HEAD);

                    Point3D<Float> position = joint.getPosition();
                    Point3f pos = convertPoint(position);
                    Vector3f dist = new Vector3f();
                    dist.x = pos.x-centerPoint.x;
                    dist.y = pos.z-centerPoint.z;
                    float x = position.getX();
                    float y = position.getY();
                    float z = position.getZ();
               if (now -lastSpeak > 2000) {
                   float d = dist.length();
                   if (d<500) {
                       speak("In");
                   } else {
                       speak("Out");
                   }
                   lastSpeak = now;
               }

                if (now - lastUpdate > 200) {
                    //speak(" x "+ df.format(x/1000) + " y "+df.format(y/1000)+" z "+df.format(z/1000));
                    //speak(" x "+ (int)Math.round(x/10) + " y "+(int)Math.round(y/10)+" z "+(int)Math.round(z/10));
                    posPanel.setPosition(position);
                    posPanel.setVar("dist from center", dist.length());
                    out.println("User " + id + " time " + df.format(totalElapsed)
                            + " x " + (int) Math.round(x / 10) + " y " + (int) Math.round(y / 10) + " z " + (int) Math.round(z / 10));
                    lastUpdate = now;
                }

            } else {
                // not yet tracked
            }

        }

        return ret;

    }
    
    public Point3f convertPoint(com.primesense.nite.Point3D<Float> p) {
        Point3f point = new Point3f();
        point.x = p.getX();
        point.y = p.getY();
        point.z = p.getZ();
        return point;
    }
    
}
