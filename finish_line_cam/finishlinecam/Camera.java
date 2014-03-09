package finishlinecam;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.video.Capture;

public class Camera {
	private final PApplet applet;
	private final int width;
	private final int height;
	
	private final int frameCount;
	private final PImage[] frames;
	private final long[] frameTimestamps;
	
	private final Capture cam;
	
	private int index = 0;
	
	public Camera(PApplet applet, int width, int height, int frameRate, int segmentSeconds) {
		this.applet = applet;
		this.width = width;
		this.height = height;
		
		frameCount = frameRate * segmentSeconds;
		frames = new PImage[frameCount];
		frameTimestamps = new long[frameCount];
		
		cam = new Capture(applet, width, height, frameRate);
		
		reset();
	}
	
	public void reset() {
		for (int i = 0; i < frameCount; ++i) {
			frames[i] = applet.createImage(width, height, PConstants.RGB);
			frameTimestamps[i] = 0l;
		}
		index = 0;
	}
	
	public void start() {
		cam.start();
	}
	
	public void snap(boolean capture) {
		if (cam.available()) {
			cam.read();
			cam.loadPixels();
		}
		
		if (capture && index < frameCount) {
			frames[index].copy(cam, 0,0, width, height, 0, 0, width, height);
			frameTimestamps[index] = System.currentTimeMillis();
			++index;
		}

		applet.set(0, 0, cam);
	}
}
