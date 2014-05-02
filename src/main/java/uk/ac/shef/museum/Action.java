/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.museum;

/**
 *
 * @author samf
 */
public enum Action {
    WAVE("Wave your hands", "you should have waved your hands", "wave-anim"),
    JUMP("Jump up and down", "you should have jumped", "jump-anim"),
    HANDS_UP("Put your hands up", "you should have put your hands up", "hands-up-anim");
    //NONE("None", "None");
    String command;
    String error;
    String anim;
    Action(String command, String error, String anim) {
        this.command = command;
        this.error = error;
        this.anim = anim;
    }
    String getCommand() {
        return command;
    }
    String getError() {
        return error;
    }
    String getAnim() {
        return anim;
    }
}
