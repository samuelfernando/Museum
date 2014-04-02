package uk.ac.shef.test;


// AudioRecorder.java
// Andrew Davison, March 2012, ad@fivedots.coe.psu.ac.th

/* Records microphone data and stores it at CD quality 
   in the specified wav file.

   A microphone feeds its incoming audio data into a mixer, 
   which places the input data in a target data line, from
   which it is read into the application.
 */

import java.io.*;
import java.util.*;
import javax.sound.sampled.*;


public class AudioRecorderKinect extends Thread
{
  private static final String MIKE_NAME = "Primary Sound Capture Driver";  // Windows
  // private static final String MIKE_NAME = "Built-in Microphone";        // MAC OS X
  // private static final String MIKE_NAME = "plughw:0,0";                 // Linux


  private String outFnm;
  private AudioFormat audioFormat;
  private TargetDataLine tdLine = null;      // audio input line 
  private volatile boolean isRunning  = true;


  public AudioRecorderKinect(String inMixName, String fnm)
  {
    outFnm = fnm;

    // record at CD quality: PCM 44.1 kHz, 16 bit, stereo, signed, little endian, 
    audioFormat =  new AudioFormat( 44100.0F, 16, 2, true, false);
                   // sampleRate, sampleSizeInBits, channels, signed, is bigEndian?

    // initialize the target line coming from the named microphone with the audio format
    tdLine = initMike(inMixName, audioFormat);
    if (tdLine == null) {
      System.out.println("No suitable target line input mixer found");
      System.exit(1);
    }
  }  // end of AudioRecorder()



  private TargetDataLine initMike(String nm, AudioFormat audioFormat)
  /* initialize a suitable target line for the microphone. Try at most three
     approaches -- search for the supplied name, search for MIKE_NAME,
     and search for anything.
  */
  {
    System.out.println("Looking for target line input mixer with partial name \"" + nm + "\"");
    TargetDataLine tdLine = AudioUtilsKinect.getTargetLine(nm, audioFormat);

    if (tdLine == null) {
      System.out.println("\nLooking for standard mike: \"" + MIKE_NAME + "\"");
      tdLine = AudioUtilsKinect.getTargetLine(MIKE_NAME, audioFormat);
    }

    if (tdLine == null) {
      System.out.println("\nLooking for any target line input mixer");
      tdLine = AudioUtilsKinect.getTargetLine("", audioFormat);   // i.e. ignore name constraint
    }

    return tdLine;
  }  // end of initMike()


  public void startRecording()
  // called from the "Start" button in the GUI
  { this.start();  }



  public void run()
  {
    byte[] audioBytes = recordBytes();

    // convert bytes to audio stream
    ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
    AudioInputStream ais = new AudioInputStream(bais, audioFormat,
                                      audioBytes.length / audioFormat.getFrameSize());

    // report audio duration
    long millisecs = (long) ((ais.getFrameLength() * 1000) /
                                               audioFormat.getFrameRate());
    System.out.println("Audio length: " + (millisecs / 1000.0) + " secs");

    saveAudio(ais, outFnm);

    System.exit(0);
  }  // end of run()


  private byte[] recordBytes()
  {
    // create byte stream for holding recorded input
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    // create data buffer for holding input audio
    byte[] data = new byte[tdLine.getBufferSize() / 5];

    // read the data from the target data line, write to byte stream
    int numBytesRead = 0;
    tdLine.start();    // begin audio capture
    while (isRunning) {
      if ((numBytesRead = tdLine.read(data, 0, data.length)) == -1)
        break;
      out.write(data, 0, numBytesRead);
      // System.out.println("read bytes: " + numBytesRead);
    }

    // end of the recording - stop and close the target data line
    tdLine.stop();   
    tdLine.close();

    // return audio as a byte array
    byte audioBytes[] = out.toByteArray();
    // System.out.println("total audio bytes length: " + audioBytes.length);
    return audioBytes;
  }  // end of recordBytes()


  public void stopRecording()
  // called from the "Stop" button in the GUI
  // stop capturing data from the microphone
  { isRunning = false; }



  private void saveAudio(AudioInputStream ais, String outFnm)
  // save audio to a WAV file
  {
    try {
      AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(outFnm));
      System.out.println("Audio written to " + outFnm);
    }
    catch (IOException e) 
    {  System.out.println("Unable to write audio to " + outFnm); }
  }  // end of saveAudio()

}  // end of AudioRecorder class


