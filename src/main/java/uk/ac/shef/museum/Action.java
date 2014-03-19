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
    HANDS_BEHIND("Put your hands behind you", "you should have put your hands behind you"), 
    HANDS_IN_AIR("Put your hands in the air", "you should have put your hands in the air"),
    NONE("None", "None");
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
