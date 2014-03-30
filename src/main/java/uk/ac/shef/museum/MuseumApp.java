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

    VisitorState state = VisitorState.NOTHINGNESS;
    PlayState playState = PlayState.PLAY_START;
    Action currentAction = Action.NONE;
    Random rand;
    MuseumUtils mu;
    int score = 0;
    boolean simonSays;
    long lastRequest;
    boolean announcedTracking;
    UserData activeUser;
    boolean greeted;
    long endOfRequest;
    boolean requestMade;

    public MuseumApp(UserTracker tracker, PositionPanel panel) {
        rand = new Random();
        mu = new MuseumUtils(tracker, panel);
        requestMade = false;
    }

    Action chooseAction() {
        /*      Class c = Action.class;
         int x = rand.nextInt(c.getEnumConstants().length);
         return (Action)c.getEnumConstants()[x];*/
        return Action.WAVE;
    }

    boolean doesSimonSay() {
        double r = rand.nextDouble();
        if (r < 0.8) {
            return true;
        }
        return false;
    }

    boolean isAnyoneTracking(List<UserData> users) {
        boolean isAnyoneTracking = false;
        for (UserData user : users) {
            if (user.getSkeleton().getState() == SkeletonState.TRACKED) {
                isAnyoneTracking = true;
            }
        }
        return isAnyoneTracking;
    }

    void update(List<UserData> users) {

        if (state == VisitorState.NOTHINGNESS) {
            if (!users.isEmpty()) {
                state = VisitorState.BODIES;
                for (UserData user : users) {
                    mu.mTracker.startSkeletonTracking(user.getId());
                }
            } else if (mu.speechFinished()) {
                mu.speak("I am all alone");
            }
        }

        if (state == VisitorState.BODIES) {
            if (users.isEmpty()) {
                state = VisitorState.NOTHINGNESS;
            }
            if (isAnyoneTracking(users)) {
                state = VisitorState.TRACKING;
            } else {
                for (UserData user : users) {
                    //if (user.isNew()) {
                    if (user.getSkeleton().getState() == SkeletonState.NONE) {
                        mu.mTracker.startSkeletonTracking(user.getId());
                    }
                   //}
                }
           
                if (mu.speechFinished()) {
                    mu.speak("I can see somebody, but not clearly yet."
                            + " You will need to stand still and wave your arms at me.");
                }
            }
        }

        if (state == VisitorState.TRACKING) {
           // System.out.println("Tracking state");
            if (!isAnyoneTracking(users)) {
                state = VisitorState.BODIES;
            }
            else if (anyoneInZone(users)) {
                state = VisitorState.INZONE_START_GAME;
            } 
            else if (mu.speechFinished()) {
                    for (UserData user : users) {
                    //if (user.isNew()) {
                        mu.mTracker.startSkeletonTracking(user.getId());
                   //}
                }
           
                mu.speak("I can see you. But you have to get into the zone if you want to play.");
            }
        }

        if (state == VisitorState.INZONE_START_GAME) {
            //System.out.println("In zone start game");

            if (!anyoneInZone(users)) {
                state = VisitorState.TRACKING;
            }
            if (!greeted && mu.speechFinished()) {
               // System.out.println("Greeting");

                mu.speak("Hello human. Let us start the game. If I say Simon Says you must do the action. Otherwise do not.");
                activeUser = getActiveUser(users);
                for (UserData user : users) {
                    if (user.getId() != activeUser.getId()) {
                        mu.mTracker.stopSkeletonTracking(user.getId());
                    }
                }
                greeted = true;
                mu.makeLog(activeUser);

            }

            if (greeted && mu.speechFinished()) {
                state = VisitorState.PLAYING_GAME;
            }
        }



        if (state == VisitorState.PLAYING_GAME) {
            activeUser = getActiveUser(users);
            if (activeUser == null) {
                state = VisitorState.GOODBYE;
            } else {
                if (playState == PlayState.PLAY_START) {
                                 // System.out.println("Play start");

                    if (mu.speechFinished()) {
                        currentAction = chooseAction();
                        simonSays = doesSimonSay();
                        makeRequest();
                        playState = PlayState.ACTION_GIVEN;
                            
                    }
                }
                if (playState == PlayState.ACTION_GIVEN) {
                   //                                    System.out.println("action given");

                    long now = System.currentTimeMillis();
                    if (mu.speechFinished()) {
                        mu.addSkeleton(activeUser.getSkeleton());
                        if (!requestMade) {
                            endOfRequest = now;
                            requestMade = true;
                        }
                    }
                    if (requestMade && now - endOfRequest > 3000) {
                        playState = PlayState.EVALUATION;
                        requestMade = false;
                    }
                }
                if (playState == PlayState.EVALUATION) {
                 //   System.out.println("evaluation");
                    if (mu.speechFinished()) {                                   
                        checkRequest();
                        playState = PlayState.PLAY_START;
                    }
                }
                mu.makeLog(activeUser);
            }


        }


        if (state==VisitorState.GOODBYE) {
            if (mu.speechFinished()) {
                mu.speak("Goodbye human. Your final score was "+score);
                 playState = PlayState.PLAY_START;
                 state = VisitorState.NOTHINGNESS;
                score = 0;
                greeted = false;
                //activeUser = null;
           }
        }
        /*if (!anyoneInZone && mu.timeSinceLastSpeak() > 6000) {
         mu.speak("I'm ready to play. If you want to play, then enter the play zone and wave your arms.");
         }*/
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
                    toSpeak = "Yes well done, I did not say Simon says.";
                    ++score;
                } else {
                    toSpeak = "No you got it wrong, I did not say Simon says.";
                }
            }
        }

        toSpeak += " Your score is " + score;
        
        mu.speak(toSpeak);
        return ret;

    }

    void makeRequest() {
        long now = System.currentTimeMillis();
        String toSpeak = "";
        if (simonSays) {
            toSpeak += "Simon says";
        }
        toSpeak += " " + currentAction.getCommand();
        
        mu.speak(toSpeak);
        
        //endOfRequest = now + mu.speak(toSpeak);
        lastRequest = now;

    }

    long timeSinceRequest() {
        long now = System.currentTimeMillis();
        return now - lastRequest;

    }

    boolean anyoneInZone(List<UserData> users) {
        boolean anyoneInZone = false;
        for (UserData user : users) {
            if (mu.inPlayZone(user)) {
                anyoneInZone = true;
            }
        }
        return anyoneInZone;
    }

    UserData getActiveUser(List<UserData> users) {
        UserData chosenUser = null;
        for (UserData user : users) {
            if (mu.inPlayZone(user)) {
                chosenUser = user;
            }
        }
        return chosenUser;
    }
}
