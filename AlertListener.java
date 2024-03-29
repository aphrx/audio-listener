package com.qac.device;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;

import java.util.ArrayList;

public class AlertListener {
	
    public static void main(String[] args) {

        System.out.println("AudioListener is running.");
        AlertListener meter = new AlertListener();
        new Thread(new AlertRecorder(meter)).start();
    }

}

class AlertRecorder implements Runnable {
    final AlertListener meter;
    private int state = 0;
    private int scanLine = 0;
    private int audioAvg = 0;
	
	private int lastAvg = 0;

    private int mode = 1; //1 = Alert Listener

    ArrayList<Integer> scan = new ArrayList<Integer>();

    private int scanInterval = 100;
	private int peakMultiplier = 100;

    AlertRecorder(final AlertListener meter) {
        this.meter = meter;
    }

	public int getCurrentVolume(){
    	return lastAvg;
    }

    @Override
    public void run() {
        AudioFormat fmt = new AudioFormat(44100f, 16, 1, true, false);
        final int bufferByteSize = 2048;

        TargetDataLine line;
        try {
            line = AudioSystem.getTargetDataLine(fmt);
            line.open(fmt, bufferByteSize);
        } catch(LineUnavailableException e) {
            System.err.println(e);
			System.out.println("AlertListener was unable to connect to a line.");
            return;
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



			//insert alert listener stuff here
			
			
			if(scanLine != scanInterval){

	            	scan.add((int)(peak * peakMultiplier));
	            	audioAvg += (peak * peakMultiplier);
	            }

	            scanLine++;

	            if(scanLine == scanInterval){
	            	audioAvg = audioAvg/scanInterval;
					lastAvg = audioAvg;
	            	audioAvg = 0;
	    			scanLine = 0;
	    			scan.clear();
					line.close();
	            	return;
	            }

            lastPeak = peak;
        }
    }
}
