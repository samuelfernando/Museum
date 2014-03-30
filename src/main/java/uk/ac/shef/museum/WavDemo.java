/*
 *  Copyright 2014 by The Friendularity Project (www.friendularity.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package uk.ac.shef.museum;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import org.jflux.impl.messaging.rk.JMSAvroRecordSender;
import org.jflux.impl.messaging.rk.JMSBytesMessageSender;
import org.jflux.impl.messaging.rk.utils.ConnectionManager;
import org.mechio.impl.audio.config.WavPlayerConfigRecord;

/**
 * WAV Player demo.
 * @author Amy Jessica Book <jgpallack@gmail.com>
 */
public class WavDemo 
{
    private static Connection theConnection;
    private static Session theSession;
    private static Destination theDestination;
    
    public static void main(String[] args)
    {
        try {
            // The WAV must be on the robot in order to play it
            // Replace this bogus path with the actual one
            // A decent file for testing is /usr/share/sounds/alsa/Front_Left.wav
            //String wavPath = "/path/to/wav/on/robot.wav";
            String wavPath = "/usr/share/sounds/alsa/Front_Left.wav";
            
            // Replace this with the IP address of the robot
            String ipAddress = "192.168.0.101";
            
            WavPlayerConfigRecord config = configureStart(wavPath);
            connect(ipAddress);
            JMSAvroRecordSender<WavPlayerConfigRecord> sender = makeSender();
            sender.sendRecord(config);
            Thread.sleep(5000);
            disconnect();
        } catch (InterruptedException ex) {
            Logger.getLogger(WavDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static WavPlayerConfigRecord configureStart(String wavPath) {
        WavPlayerConfigRecord.Builder builder =
                WavPlayerConfigRecord.newBuilder();
        
        // Set the WAV path
        builder.setWavLocation(wavPath);
        
        // Will initiate playback if player ID ends with _start
        // Will kill playback if player ID ends with _stop
        builder.setWavPlayerId("testPlayer_start");
        
        // The rest of these fields are unused, but let's initialize them anyway
        builder.setStartDelayMillisec(0);
        builder.setStartTimeMicrosec(0);
        builder.setStopTimeMicrosec(0);
        
        return builder.build();
    }
    
    private static WavPlayerConfigRecord configureStop(String wavPath) {
        // Not used in this demo, but use this to stop playback in progress
        
        WavPlayerConfigRecord.Builder builder =
                WavPlayerConfigRecord.newBuilder();
        
        // Set the WAV path
        builder.setWavLocation(wavPath);
        
        // Will initiate playback if player ID ends with _start
        // Will kill playback if player ID ends with _stop
        builder.setWavPlayerId("testPlayer_stop");
        
        // The rest of these fields are unused, but let's initialize them anyway
        builder.setStartDelayMillisec(0);
        builder.setStartTimeMicrosec(0);
        builder.setStopTimeMicrosec(0);
        
        return builder.build();
    }
    
    private static void connect(String ipAddress) {
        // Generate the Qpid objects we'll need in order to send a message
        
        // This is where the player looks for config messages
        String destString =
                "wavPlayerEvent; {create: always, node: {type: topic}}";
        
        try {
            theConnection = ConnectionManager.createConnection(
                    "admin", "admin", "client1", "test",
                    "tcp://" + ipAddress + ":5672");
            theDestination = ConnectionManager.createDestination(destString);
            try {
                theSession =
                        theConnection.createSession(
                                false, Session.CLIENT_ACKNOWLEDGE);
                theConnection.start();
            } catch(JMSException ex) {
                System.out.println(
                        "Unable to create Session: " + ex.getMessage());
            }
        } catch(Exception e) {
            System.out.println("Connection error: " + e.getMessage());
            
            disconnect();
        }
    }
    
    private static void disconnect() {
        // Kill the session and connection, in that order
        
        if(theSession != null) {
            try {
                theSession.close();
            } catch(JMSException ex) {
            }
        }

        if(theConnection != null) {
            try {
                theConnection.close();
            } catch(JMSException ex) {
            }
        }

        theConnection = null;
        theDestination = null;
        theSession = null;
    }
    
    private static JMSAvroRecordSender<WavPlayerConfigRecord> makeSender() {
        // Make a record sender to send the WAV player config
        
        JMSBytesMessageSender msgSender = new JMSBytesMessageSender();
        msgSender.setSession(theSession);
        msgSender.setDestination(theDestination);
        msgSender.openProducer();
        
        return new JMSAvroRecordSender<WavPlayerConfigRecord>(msgSender);
    }
}