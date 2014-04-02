package uk.ac.shef.test;


// AudioUtils.java
// Andrew Davison, Jan 2012, ad@fivedots.coe.psu.ac.th

/* This same class library is used in all the examples in
   chapter 15: Mixer Details, MikeRecorder, 
   Beamforming, and Spoken Kinect Breakout
*/ 

import javax.sound.sampled.*;


public class AudioUtilsKinect
{
  // kinds of lines
  private static Line.Info sdInfo = new Line.Info(SourceDataLine.class);
  private static Line.Info tdInfo = new Line.Info(TargetDataLine.class);
  private static Line.Info clipInfo = new Line.Info(Clip.class);
  private static Line.Info portInfo = new Line.Info(Port.class);


  public static boolean isSourceDataLine(Mixer mixer)
  // used for playback of audio
  { return mixer.isLineSupported(sdInfo);  }

  public static boolean isTargetDataLine(Mixer mixer)
  // used to capture incoming audio
  { return mixer.isLineSupported(tdInfo);  }

  public static boolean isClip(Mixer mixer)
  { return mixer.isLineSupported(clipInfo);  }

  public static boolean isPort(Mixer mixer)
  { return mixer.isLineSupported(portInfo);  }


  public static boolean isInputPort(Mixer mixer)
  // a port on a source line is for recording controls
  { 
    Line.Info[] sli = mixer.getSourceLineInfo();    // sources for the mixer
    for (Line.Info lineInfo : sli) {
      if (lineInfo instanceof Port.Info)
        return true;
    }
    return false;
  }  // end of isInputPort()


  public static boolean isOutputPort(Mixer mixer)
  // a port on a target line is for playback controls
  { 
    Line.Info[] tli = mixer.getTargetLineInfo();  // targets for the mixer
    for (Line.Info lineInfo : tli) {
      if (lineInfo instanceof Port.Info)
        return true; 
    }
    return false;
  }  // end of isOutputPort()



  public static boolean containsName(Mixer mixer, String nm)
  /* is this a mixer with nm in its name, ignoring case */
  {
    Mixer.Info mi = mixer.getMixerInfo();
    String name = mi.getName().toLowerCase();
    return name.contains(nm.toLowerCase());
  }  // end of containsName()



  public static boolean isNamedTargetDataLine(Mixer mixer, String nm)
  // is this a TargetDataLine containing nm
  { return ( mixer.isLineSupported(tdInfo) && containsName(mixer, nm)); }



  public static void printInfo(int idNum, Mixer mixer)
  // Print name and description of the mixer
  {
    Mixer.Info mi = mixer.getMixerInfo();
    System.out.println("" + idNum + ". Name: " +  mi.getName() );
    System.out.println("   Description: " + mi.getDescription() );
  }  // end of printInfo()



  public static TargetDataLine getTargetLine(String nm, AudioFormat audioFormat)
  /* Return the target line for the mixer with a name containing
     nm, with the specified audio format */
  {
    Mixer mixer = getNamedTargetLine(nm);
    if (mixer == null) {
      System.out.println("No target line mixer found for \"" + nm + "\"");
      return null;
    }
    System.out.println("Recording from \"" + mixer.getMixerInfo().getName() + "\"");

    TargetDataLine tdLine = null;
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
    try {
      tdLine = (TargetDataLine) mixer.getLine(info);
      tdLine.open(audioFormat);
    }
    catch (LineUnavailableException e) {
      System.out.println("Unable to access the capture line");
    }
    return tdLine;
  }  // end of getTargetLine()




  public static Mixer getNamedTargetLine(String nm)
  /* look at all the mixers, and return the first one that contains nm
     in its name and is a TargetDataLine
  */
  { 
    for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
      Mixer mixer = AudioSystem.getMixer(mi);
      if (isNamedTargetDataLine(mixer, nm))
        return AudioSystem.getMixer(mi);
    }
    return null;
  }  // end of getNamedTargetLines()



}  // end of AudioUtils class