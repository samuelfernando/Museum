/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.museum;

import com.primesense.nite.Skeleton;
import com.primesense.nite.SkeletonState;
import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import java.util.List;
import java.util.Random;

/**
 *
 * @author samf
 */
class MuseumApp {

    VisitorState state = VisitorState.START;
    PlayState playState = PlayState.PLAY_START;
    Action currentAction = Action.NONE;
    Random rand;
    MuseumUtils mu;
    int score = 0;
    boolean simonSays;
 long lastRequest;
   
    public MuseumApp(UserTracker tracker, PositionPanel panel) {
        mu = new MuseumUtils(tracker, panel);
        rand = new Random();
    }

    Action chooseAction() {
        /*      Class c = Action.class;
         int x = rand.nextInt(c.getEnumConstants().length);
         return (Action)c.getEnumConstants()[x];*/
        return Action.WAVE;
    }

    boolean doesSimonSay() {
        double r = rand.nextDouble();
        if (r>0.5) {
            return true;
        }
        return false;
    }

    void update(List<UserData> users) {
        for (UserData user : users) {
            //addReport(user); 
            if (state == VisitorState.START) {
                boolean inZone = mu.inPlayZone(user);
                if (inZone) {
                    state = VisitorState.GREET;
                }
            }
            if (state == VisitorState.GREET) {
                mu.speak("Hello human. Welcome to my game.");

                state = VisitorState.WAIT_FOR_TRACK;
            }
            if (state == VisitorState.WAIT_FOR_TRACK) {
                Skeleton skeleton = user.getSkeleton();
                SkeletonState skelState = skeleton.getState();
                if (skelState == SkeletonState.TRACKED) {
                    state = VisitorState.START_GAME;
                } else {
                    if (mu.timeSinceLastSpeak() > 5000) {
                        mu.speak("I can't see you yet. Please wave your arms at me.");
                    }
                }

            }
            if (state == VisitorState.START_GAME) {
                if (mu.timeSinceLastSpeak() > 5000) {
                    mu.speak("Ok that's great I can see you. Let's start the game.");
                    state = VisitorState.PLAYING_GAME;
                }
            }
            if (state == VisitorState.PLAYING_GAME) {
                if (playState == PlayState.PLAY_START) {
                    if (mu.timeSinceLastSpeak() > 5000) {
                        currentAction = chooseAction();
                        simonSays = doesSimonSay();
                        makeRequest();
                        playState = PlayState.ACTION_GIVEN;

                    }
                }
                if (playState == PlayState.ACTION_GIVEN) {
                    mu.addSkeleton(user.getSkeleton());

                    if (timeSinceRequest() > 7000) {
                        playState = PlayState.EVALUATION;
                    }
                }
                if (playState == PlayState.EVALUATION) {
                    checkRequest();
                    playState = PlayState.PLAY_START;
                }
            }
            mu.makeLog(user);
        }
    }

    boolean checkRequest() {
        boolean ret = false;
        String toSpeak = "";
        if (simonSays) {
            if (currentAction == Action.WAVE) {
                ret = mu.hasUserWaved();
                if (ret) {
                    toSpeak = "Yes, you got that right!";
                    ++score;
                } else {
                    toSpeak = "No you got that wrong " + currentAction.getError() + ".";
                }
            }

        } else {
            if (currentAction == Action.WAVE) {
                ret = !mu.hasUserWaved();
                if (ret) {
                    toSpeak = "Yes well done, I didn't say Simon says.";
                    ++score;
                } else {
                    toSpeak = "No you got it wrong, I didn't say Simon says.";
                }
            }
        }

        toSpeak += " Your score is " + score;
        mu.speak(toSpeak);
        return ret;

    }
     void makeRequest() {
        String toSpeak = "";
        if (simonSays) {
            toSpeak+="Simon says";
        }
        toSpeak+=" "+currentAction.getCommand();
        mu.speak(toSpeak);
        lastRequest = System.currentTimeMillis();
    }
     
      long timeSinceRequest() {
        long now = System.currentTimeMillis();
        return now - lastRequest;
    
    }
   
   
}
