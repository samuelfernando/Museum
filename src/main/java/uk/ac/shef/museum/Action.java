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
    WAVE("Wave your hands", "you should have waved your hands"),
    JUMP("Jump up and down", "you should have jumped"),
    HANDS_UP("Put your hands up", "you should have put your hands up");
    //NONE("None", "None");
    String command;
    String error;
    Action(String command, String error) {
        this.command = command;
        this.error = error;
    }
    String getCommand() {
        return command;
    }
    String getError() {
        return error;
    }
}
