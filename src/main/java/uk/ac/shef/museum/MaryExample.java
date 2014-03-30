/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.museum;

/**
 *
 * @author samf
 */
import java.io.File;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.util.data.audio.AudioPlayer;


public class MaryExample {

	/**
	 * @param args
	 */
    
     static void localWav(String text) {
        try {
            File soundFile = new File( "AllAlone.wav" );
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream( soundFile );
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	public static void main(String[] args) {
            try {
		/*MaryInterface marytts = new LocalMaryInterface();
		Set<String> voices = marytts.getAvailableVoices();
		marytts.setVoice(voices.iterator().next());
                
                for (float i=0;i<1;i+=0.01) {
                    AudioInputStream audio = marytts.generateAudio("Number "+i);
                    AudioPlayer player = new AudioPlayer(audio);
                    player.start();
                    player.join();
                }
                System.exit(0);*/
                localWav("");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
	}

}