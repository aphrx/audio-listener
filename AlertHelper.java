package com.qac.device;

import java.lang.Thread;

public class AlertHelper {
	AlertListener meter = new AlertListener();
	AlertRecorder rec = new AlertRecorder(meter);
	
	Thread t = new Thread(rec);
	
    public static void main(String[] args) {
		
	}
	
	public AlertHelper(){
		t.start();
	}
	
	public void scan(){
		try {
            Thread.sleep((int)5000);
        } catch (InterruptedException e) {
        }
	}
	
	public String getAlertResponse(){
		
		if(rec.getCurrentVolume() > 0){
			System.out.println("Alert Helper Result: " + rec.getCurrentVolume());
			return "ON";
		}
		System.out.println("Alert Helper Result: " + rec.getCurrentVolume());
		return "OFF";	
	}
	
	public void kill(){
		t.interrupt();
	}
}