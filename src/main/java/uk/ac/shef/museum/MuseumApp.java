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
    Action currentAction;
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
    KinectVideoRecorder kinectVideoRecorder;
    int userCount;
    
    public MuseumApp(UserTracker tracker, PositionPanel panel, KinectVideoRecorder recorder) {
        rand = new Random();
        mu = new MuseumUtils(tracker, panel);
        requestMade = false;
        kinectVideoRecorder = recorder;
        userCount = 0;
    }

    Action chooseAction() {
        Class c = Action.class;
        int x = rand.nextInt(c.getEnumConstants().length);
        return (Action) c.getEnumConstants()[x];
        //return Action.HANDS_UP;
    }

    boolean doesSimonSay() {
        double r = rand.nextDouble();
        if (r < 0.5) {
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
             //   mu.stopSpeaking();
            } else if (mu.speechFinished()) {
                //mu.speak("I am all alone");
            }
        }

        if (state == VisitorState.BODIES) {
            if (users.isEmpty()) {
                state = VisitorState.NOTHINGNESS;
               // mu.stopSpeaking();
            } else {
                if (isAnyoneTracking(users)) {
                    state = VisitorState.TRACKING;
                 //   mu.stopSpeaking();
                } else {
                    for (UserData user : users) {
                        //if (user.isNew()) {
                        if (user.getSkeleton().getState() == SkeletonState.NONE) {
                            mu.mTracker.startSkeletonTracking(user.getId());
                        }
                        //}
                    }

                    if (mu.speechFinished()) {
                        //mu.speak("I can see somebody, but not clearly yet." + " You will need to stand still and wave your arms at me.");
                    }
                }
            }
        }

        if (state == VisitorState.TRACKING) {
            // System.out.println("Tracking state");
            if (!isAnyoneTracking(users)) {
                state = VisitorState.BODIES;
               // mu.stopSpeaking();
            } else if (anyoneInZone(users)) {
                state = VisitorState.INZONE_START_GAME;
               // mu.stopSpeaking();
            } else if (mu.speechFinished()) {
                for (UserData user : users) {
                    //if (user.isNew()) {
                    mu.mTracker.startSkeletonTracking(user.getId());
                    //}
                }

                //mu.speak("I can see you. But you have to get into the zone if you want to play.");
            }
        }

        if (state == VisitorState.INZONE_START_GAME) {
            //System.out.println("In zone start game");

            if (!anyoneInZone(users)) {
                state = VisitorState.TRACKING;
                //mu.stopSpeaking();
            }
            if (!greeted && mu.speechFinished()) {
                // System.out.println("Greeting");

                mu.speak("Hello! Are you ready to play with me? Let's play Simon Says!"
                        + " If I say Simon Says you must do the action. Otherwise do not.");
                ++userCount;
                activeUser = getActiveUser(users);
                kinectVideoRecorder.start("User"+activeUser.getId());
                for (UserData user : users) {
                    if (user.getId() != activeUser.getId()) {
                        mu.mTracker.stopSkeletonTracking(user.getId());
                    }
                }
                //while (!mu.speechFinished()) {}
                greeted = true;

            }

            if (greeted && mu.speechFinished()) {
                state = VisitorState.PLAYING_GAME;
            }
            activeUser = getActiveUser(users);

            if (activeUser!=null) {
                mu.makeLog(activeUser);
            }
            else {
                state = VisitorState.GOODBYE;
            }

        }



        if (state == VisitorState.PLAYING_GAME) {
            activeUser = getActiveUser(users);
            if (activeUser == null) {
               // mu.stopSpeaking();
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


        if (state == VisitorState.GOODBYE) {
            if (mu.speechFinished()) {
                mu.speak("Goodbye! I had fun playing with you. Your final score was " + score);
                kinectVideoRecorder.stop();
                playState = PlayState.PLAY_START;
                state = VisitorState.NOTHINGNESS;
                score = 0;
                greeted = false;
                //do {

                //} while (!mu.speechFinished());
                activeUser = null;
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
            ret = mu.checkAction(currentAction);
            if (ret) {
                toSpeak = "Yes, you got that right!";
                ++score;
            } else {
                toSpeak = "No you got that wrong " + currentAction.getError() + ".";
            }

        } else {
            ret = !mu.checkAction(currentAction);

            if (ret) {
                toSpeak = "Yes well done, I did not say Simon says.";
                ++score;
            } else {
                toSpeak = "No you got it wrong, I did not say Simon says.";
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

        //mu.speak(toSpeak);
        mu.makeRequest(toSpeak, currentAction);
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

    void stopRecording() {
        kinectVideoRecorder.stop();
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
