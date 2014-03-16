/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.museum;

import com.primesense.nite.Point3D;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashMap;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author samf
 */
public class PositionPanel extends Component {
    
    Vector3f vel;
    String text;
    Font font;
    long endTime;
    HashMap<String, Float> vars;
    Point3f position;
    
    public PositionPanel() {
        vel = new Vector3f();
        font = new Font("Serif", Font.PLAIN, 36);
        text = "waiting";
        vars = new HashMap<String, Float>();
        position = new Point3f();
    }
    
     public Point3f convertPoint(com.primesense.nite.Point3D<Float> p) {
        Point3f point = new Point3f();
        point.x = p.getX();
        point.y = p.getY();
        point.z = p.getZ();
        return point;
    }
    
    public void setTimer(long time) {
        endTime = time;
    }
    @Override
    public void paint(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0,0,this.getWidth(), this.getHeight());
       g.setColor(Color.white);
       g.setFont(font);
       int x = (int)Math.round(position.x/10);
       int y = (int)Math.round(position.y/10);
       int z = (int)Math.round(position.z/10);
       
       g.drawString("pos " + x +  " "+ y + " "+ z, 0, 200);
       int count = 0;
       for (String key : vars.keySet()) {
           float val = vars.get(key);
           String out = key+" "+val;
           g.drawString(out, 0, 250+count*50);
           ++count;
       }
       //g.drawString(text, 0,250);
       
       /*String out = "timeRemaining = "+(endTime-System.currentTimeMillis());
       g.drawString(out, 0, 300);
       int count = 0;
       for (String key : vars.keySet()) {
           float val = vars.get(key);
           out = key+" "+val;
           g.drawString(out, 0, 350+count*50);
           ++count;
       }*/
        /* g.setColor(Color.white);
        
       
        if (vel.z<0) {
            g.fillRect(400+(int)vel.z, 100, (int)(-vel.z), 50);
        }
        else {
            g.fillRect(400, 100, (int)(vel.z), 50);   
        }*/
    }
    public void setText(String text) {
        this.text = text;
    }
    
    public void setVar(String key, float val) {
        vars.put(key, val);
    }

   public void setPosition(Point3D<Float> position) {
       this.position = convertPoint(position);
       repaint();
   }
}
