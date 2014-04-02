package uk.ac.shef.test;


// MikeRecorder.java
// Andrew Davison, March 2012, ad@fivedots.coe.psu.ac.th

// GUI top-level for the microphone recorder

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;


public class MikeRecorderKinect extends JFrame
{
  private static final String INPUT_MIXER = "kinect"; 
                               // "sigmatel";   //"realtek";  // "kinect";

  private static final String OUT_FNM = "out.wav";


  private JButton startBut, stopBut;
  private AudioRecorderKinect recorder;



  public MikeRecorderKinect(String inMixName)
  {
    super("Mike Recorder");

    recorder = new AudioRecorderKinect(inMixName, OUT_FNM);
    makeGUI();

    // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { recorder.stopRecording();
        System.exit(0); 
      }
    });

    pack();
    setResizable(false);
    setLocationRelativeTo(null);  // center the window 
    setVisible(true);
  } // end of MikeRecorder()



  private void makeGUI()
  // start, stop button, volume slider, and meter panel
  {
	  Container c = getContentPane();
	  c.setLayout( new BoxLayout(c, BoxLayout.Y_AXIS) );   

    JPanel butsPanel = new JPanel();

    startBut = new JButton("Start");   
	  startBut.addActionListener( new ActionListener() {
		  public void actionPerformed(ActionEvent e)
		  {  // System.out.println("Started"); 
         recorder.startRecording();
         startBut.setEnabled(false);
         stopBut.setEnabled(true);
      }
    });
    butsPanel.add(startBut);

    stopBut = new JButton("Stop");  
    stopBut.setEnabled(false);
	  stopBut.addActionListener( new ActionListener() {
		  public void actionPerformed(ActionEvent e)
		  {  // System.out.println("Stopped"); 
         recorder.stopRecording();
         stopBut.setEnabled(false);
      }
    });
    butsPanel.add(stopBut);

    c.add(butsPanel);
  }  // end of makeGUI()

  // ----------------------------------------

  public static void main(String[] args) 
  { 
    if (args.length == 0)
      new MikeRecorderKinect(INPUT_MIXER); 
    else if (args.length == 1)
      new MikeRecorderKinect(args[0]); 
    else
      System.out.println("Usage: java MikeRecorder [ partial name of input mixer ]");
  }  // end of main()

} // end of MikeRecorder class


