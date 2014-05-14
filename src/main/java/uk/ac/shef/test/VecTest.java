/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.test;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author samf
 */
public class VecTest {
    public static void main(String args[]) {
        float kinectMarkerDist = 213f;
        float robotUserDist = 200f;
        float kinectRobotDist = 74f;
    
        
        Vector3f kinectUser = new Vector3f(100.0f,0.0f,kinectMarkerDist);
        float angle = (float)Math.acos(robotUserDist/kinectMarkerDist);
        
        
        float kinectRobotZ = (float)Math.sin(angle) * kinectRobotDist;
        float kinectRobotX = (float)Math.cos(angle) * kinectRobotDist;
        System.out.println("angle = "+angle+" x "+kinectRobotX+" z "+kinectRobotZ);
        
        Vector3f kinectRobot = new Vector3f(kinectRobotX,0.0f, kinectRobotZ);
        
        
        
        Vector3f robotUser = new Vector3f();
        
        robotUser.sub(kinectUser, kinectRobot);
        System.out.println("robotUser = "+robotUser);
        
        AxisAngle4f rot = new AxisAngle4f(0.0f, 1000.0f, 0.0f, angle);
        Matrix3f mat = new Matrix3f();
        mat.set(rot);
        mat.transform(robotUser);
     
        System.out.println("robotUser = "+robotUser);
        
        
        /*Vector3f robotUser = new Vector3f();
        robotUser.sub(kinectUser, kinectRobot);
        System.out.println("robotUser = "+robotUser);
        AxisAngle4f rot = new AxisAngle4f(0.0f, -1000.0f, 0.0f, 0.174f);
        Matrix3f mat = new Matrix3f();
        mat.set(rot);
        mat.transform(robotUser);
        System.out.println("robotUser = "+robotUser);
        float opp = 200;
        float hyp = 217;
        float rad = (float)Math.PI/2 - (float)Math.asin(opp/hyp);
        System.out.println("angle = "+rad);
       /* AxisAngle4f rot = new AxisAngle4f(0.0f,-1.0f,0.0f,(float)Math.PI/2);
        Matrix3f mat = new Matrix3f();
        mat.set(rot);
        mat.transform(vec);
        System.out.println("vec = "+vec);
    
    */ }
}
