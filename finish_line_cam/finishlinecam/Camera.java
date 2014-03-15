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
	private boolean startCapturing = false;
	
	private Runnable onCaptureComplete;
	
	public Camera(PApplet applet, int width, int height, int frameRate, int segmentSeconds) {
		this.applet = applet;
		this.width = width;
		this.height = height;
		
		String[] cams = Capture.list();
		for (int i = 0; i < cams.length; ++i) {
			PApplet.println(cams[i]);
		}
		
		frameCount = frameRate * segmentSeconds;
		frames = new PImage[frameCount];
		frameTimestamps = new long[frameCount];
		
		// [FaceTime HD Camera (Built-in)]
		// [Microsoft� LifeCam HD-3000]
		// [Microsoft® LifeCam HD-3000]
		cam = new Capture(applet, width, height, "Microsoft® LifeCam HD-3000", frameRate);
		
		for (int i = 0; i < frameCount; ++i) {
			frames[i] = applet.createImage(width, height, PConstants.RGB);
			frameTimestamps[i] = 0l;
		}
	}
	
	public void start() {
		cam.start();
	}
	
	public void snap() {
		if (cam.available()) {
			cam.read();
			
			if (startCapturing && index < frameCount) {
				frames[index].copy(cam, 0,0, width, height, 0, 0, width, height);
				frameTimestamps[index] = System.currentTimeMillis();
				++index;
	
				if (index == frameCount) {
					startCapturing = false;

					if (onCaptureComplete != null) {
						onCaptureComplete.run();
						onCaptureComplete = null;
					}

					PApplet.println("Capture time: " + (frameTimestamps[index - 1] - frameTimestamps[0]));
				}
			} 

			cam.loadPixels();
			applet.set(0, 0, cam);
		}
	}
	
	public void setStartCapturing(Runnable onCaptureComplete) {
		if (startCapturing) {
			PApplet.println("Stop capturing");
			startCapturing = false;
			this.onCaptureComplete = null;
		} else {
			PApplet.println("Start capturing");
			startCapturing = true;
			this.onCaptureComplete = onCaptureComplete;
			index = 0;
		}
	}
	public boolean isCapturing() { return startCapturing; }
	
	public int getIndex() { return index; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public PImage getFrame(int idx) { return frames[idx]; }
	public long getTimestamp(int idx) { return frameTimestamps[idx]; }
}