package com.qac.device;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Line;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;

import java.util.ArrayList;

public class AudioListener {
	
    public static void main(String[] args) {

        System.out.println("AudioListener is running.");
        AudioListener meter = new AudioListener();
        new Thread(new Recorder(meter)).start();
    }

}

class Recorder implements Runnable {
    final AudioListener meter;
    private int state = 0;
    private int scanLine = 0;
    private int audioAvg = 0;
	
	private int lastAvg = 0;

    private int mode = 0; //1 = Alert Listener

    ArrayList<Integer> scan = new ArrayList<Integer>();

    private int scanInterval = 100;
	private int peakMultiplier = 100;

    Recorder(final AudioListener meter) {
        this.meter = meter;
    }

	public int getCurrentVolume(){
    	return lastAvg;
    }

    @Override
    public void run() {
		
		TargetDataLine line = null;
				
        AudioFormat fmt = new AudioFormat(44100f, 16, 1, true, false);
        final int bufferByteSize = 2048;

		Mixer.Info[] mi = AudioSystem.getMixerInfo();
		
		for(Mixer.Info info: mi){
			Mixer m = AudioSystem.getMixer(info);
			Line.Info[] li = m.getSourceLineInfo();
			//System.out.println(info.getName());
			
			if(info.getName().equals("Microphone (2- USB PnP Audio De")){
				System.out.println("USB Microphone adapter detected!");
				try {
					line = (TargetDataLine) m.getLine(m.getTargetLineInfo()[0]);
					line.open(fmt, bufferByteSize);
				} catch(LineUnavailableException e) {
					System.err.println(e);
					System.out.println("AudioListener was unable to connect to a line.");
					return;
				}
			}
		}

		
        byte[] buf = new byte[bufferByteSize];
        float[] samples = new float[bufferByteSize / 2];

        float lastPeak = 0f;

        line.start();
        for(int b; (b = line.read(buf, 0, buf.length)) > -1;) {

            // convert bytes to samples here
            for(int i = 0, s = 0; i < b;) {
                int sample = 0;

                sample |= buf[i++] & 0xFF; // (reverse these two lines
                sample |= buf[i++] << 8;   // if the format is big endian)

                // normalize to range of +/-1.0f
                samples[s++] = sample / 32768f;
            }

            float rms = 0f;
            float peak = 0f;
            for(float sample : samples) {

                float abs = Math.abs(sample);
                if(abs > peak) {
                    peak = abs;
                }

                rms += sample * sample;
            }

            rms = (float)Math.sqrt(rms / samples.length);

            if(lastPeak > peak) {
                peak = lastPeak * 0.875f;
            }

            
			if(scanLine != scanInterval){

				scan.add((int)(peak * peakMultiplier));
				audioAvg += (peak * peakMultiplier);
			}

			scanLine++;

			if(scanLine == scanInterval){
				audioAvg = audioAvg/scanInterval;
				lastAvg = audioAvg;
				identifyVolume(audioAvg);
				audioAvg = 0;
				scanLine = 0;
				scan.clear();
				line.close();
				return;
				
			}
			

            lastPeak = peak;
        }
    }

    //Determine volume level using audio average
    void identifyVolume(int audioAvg){
    	System.out.println("VOLUME LEVEL: " + audioAvg);
    	//normalizeScan(scan, audioAvg);
    	if (audioAvg < 5) {
        	if(state != 1) {
        		state = 1;
        		System.out.println("VOLUME OFF");
        	}
		}
		else if(audioAvg <30){
        	if(state != 2) {
        		state = 2;
        		System.out.println("VOLUME LOW");
        	}
        }
        else if(audioAvg > 70){
        	if(state != 3) {
        		state = 3;
        		System.out.println("VOLUME HIGH");
        	}
        }
        else{
        	if(state != 4) {
        		state = 4;
        		System.out.println("VOLUME MEDIUM");
        	}
        }
			
    }

    //Removes outliers from data-set
    void normalizeScan(ArrayList<Integer> scan, int audioAvg) {
    	int scanTolerance = 5;
    	int scanAvg = 0;

    	//System.out.println(this.scan.size());
    	
    	for(int i = 0; i < this.scan.size(); i++){
    		 
    		//System.out.println("Here");
    		 
    		if((this.scan.get(i) > audioAvg) && ((audioAvg + scanTolerance) < this.scan.get(i))){
    			this.scan.remove(i);        			
    		}
    		else if((this.scan.get(i) < audioAvg) && ((audioAvg - scanTolerance) > this.scan.get(i))){
    			this.scan.remove(i);	
    		}
    		else {
    			scanAvg = scanAvg + this.scan.get(i);
    		}
    	}

    	//System.out.println(this.scan);
    	scanAvg = scanAvg / this.scan.size();
    	System.out.println("Scan Average: " + scanAvg);
    	System.out.println("Detected Average: " + audioAvg);
    }

}