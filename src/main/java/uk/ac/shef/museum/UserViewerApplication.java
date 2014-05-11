package uk.ac.shef.museum;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JFrame;

import org.openni.Device;
import org.openni.OpenNI;

import org.openni.*;
import com.primesense.nite.*;
import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class UserViewerApplication {

    private JFrame mFrame;
    private UserViewer mViewer;
    private boolean mShouldRun = true;
    KinectVideoRecorder kinectVideoRecorder;
    boolean kinectRecording;        

    public UserViewerApplication(Device device, UserTracker tracker) {
        HashMap<String, String> configs = ReadConfig.readConfig();
        kinectRecording = Boolean.parseBoolean(configs.get("kinect-recording"));
        mFrame = new JFrame("NiTE User Tracker Viewer");
        PositionPanel positionPanel = new PositionPanel();
        if (kinectRecording) {
            kinectVideoRecorder = new KinectVideoRecorder(device);
            mViewer = new UserViewer(tracker, positionPanel, kinectVideoRecorder);
        }
        else {
            mViewer = new UserViewer(tracker, positionPanel, null);
            
        }
        
        
        JPanel panel = new JPanel(new GridLayout(1,2));
        panel.add(mViewer);
        panel.add(positionPanel);

        // register to key events
        mFrame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent arg0) {}
            
            @Override
            public void keyReleased(KeyEvent arg0) {}
            
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    mShouldRun = false;
                }
            }
        });
        
        // register to closing event
        mFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                mShouldRun = false;
            }
        });
  
     
        positionPanel.setSize(800,800);

 //       mFrame.setSize(mViewer.getWidth(), mViewer.getHeight());
         
        mViewer.setSize(800, 800);
       // mFrame.add("Center", mViewer);
       // mFrame.setSize(mViewer.getWidth(), mViewer.getHeight());
        //mFrame.setSize(panel.getWidth(), panel.getHeight());
          mFrame.add(panel);
         mFrame.setSize(1600, 800);
        //mFrame.add(mViewer);
        //mFrame.setSize(mViewer.getWidth(), mViewer.getHeight());
        
        mFrame.setVisible(true);
    }

    void run() {
        while (mShouldRun) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //kinectVideoRecorder.stop();
        if (kinectRecording) {
            mViewer.stopRecording();
        }
        mFrame.dispose();
        System.exit(0);
    }

    public static void main(String s[]) {
        // initialize OpenNI and NiTE
    	OpenNI.initialize();
        NiTE.initialize();
        
        List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
        if (devicesInfo.size() == 0) {
            JOptionPane.showMessageDialog(null, "No device is connected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Device device = Device.open(devicesInfo.get(0).getUri());
        UserTracker tracker = UserTracker.create();
        final UserViewerApplication app = new UserViewerApplication(device, tracker);
        app.run();
    }
}
