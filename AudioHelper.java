package com.qac.device;

import java.lang.Thread;

public class AudioHelper {
	AudioListener meter = new AudioListener();
	Recorder rec = new Recorder(meter);
	
	Thread t = new Thread(rec);
	
    public static void main(String[] args) {
		
	}
	
	public AudioHelper(){
		t.start();
	}
	
	public void scan(){
		try {
            Thread.sleep((int)5000);
        } catch (InterruptedException e) {
        }
	}
	
	public String getCurrentVolume(){
		return "" + rec.getCurrentVolume();
	}
	
	public void kill(){
		t.interrupt();
	}
}