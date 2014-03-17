package finishlinecam;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

public class WinnerProcessor {

	private final PApplet applet;
	private final Camera camera;
	private final RegionManager regionManager;
	
	private long startTime;

	private Point[][] laneLeaders;
	
	private PImage[] motionDiffs;
	private int playIndex = 0;
	private boolean stepPlay = false;
	private boolean playOriginal = false;
	
	private int finishLineX = -1;
	private int lane1TopY = -1;
	private int lane1BottomY = -1;
	private int lane2TopY = -1;
	private int lane2BottomY = -1;
	private boolean canDetermineWinner = false;
	
	private int lane1FinishIndex;
	private int lane2FinishIndex;
	
	public WinnerProcessor(PApplet applet, Camera camera, RegionManager regionManager) {
		this.applet = applet;
		this.camera = camera;
		this.regionManager = regionManager;
	}
	
	public void calc(long startTime) {
		final int frameCount = camera.getIndex();
		
		motionDiffs = new PImage[frameCount];
		laneLeaders = new Point[2][];
		laneLeaders[0] = new Point[frameCount];
		laneLeaders[1] = new Point[frameCount];
		
		final int width = camera.getWidth();
		final int height = camera.getHeight();
	
		motionDiffs[0] = null;
		laneLeaders[0][0] = new Point(-1,-1);
		laneLeaders[1][0] = new Point(-1,-1);
		
		for (int i = 1; i < frameCount; ++i) {
			motionDiffs[i] = applet.createImage(width, height, PConstants.RGB);
			laneLeaders[0][i] = new Point(-1,-1);
			laneLeaders[1][i] = new Point(-1,-1);
		}
		
		this.startTime = startTime;
		
		int currentPixels[];
		int previousPixels[];
		int motionDiffPixels[];
		int pixelLoc;
		int currentColor;
		int previousColor;
		Point lastLane1Leader = null;
		Point lastLane2Leader = null;
		
		Rect finish = regionManager.getRegionRect(Region.FINISH_LINE);
		if (finish != null)
			finishLineX = finish.topLeft.x + finish.width;
		
		Rect lane1 = regionManager.getRegionRect(Region.LANE1);
		if (lane1 != null) {
			lane1TopY = lane1.topLeft.y;
			lane1BottomY = lane1TopY + lane1.height;
		}
		
		Rect lane2 = regionManager.getRegionRect(Region.LANE2);
		if (lane2 != null) {
			lane2TopY = lane2.topLeft.y;
			lane2BottomY = lane2TopY + lane2.height;
		}
		
		canDetermineWinner = finish != null && lane1 != null && lane2 != null;
		lane1FinishIndex = lane2FinishIndex = -1;
		
		for (int i = 1; i < frameCount; ++i) {
			
			previousPixels = camera.getFrame(i - 1).pixels;
			currentPixels = camera.getFrame(i).pixels;
			motionDiffPixels = motionDiffs[i].pixels;
			
			for (int x = 0; x < width; ++x) {
				for (int y = 0; y < height; ++y) {
					pixelLoc = x + y * width;
					previousColor = previousPixels[pixelLoc];
					currentColor = currentPixels[pixelLoc];
					
					float r1 = applet.red(currentColor);
					float g1 = applet.green(currentColor);
					float b1 = applet.blue(currentColor);
					
					float r2 = applet.red(previousColor);
					float g2 = applet.green(previousColor);
					float b2 = applet.blue(previousColor);
					
					float diff = PApplet.dist(r1,g1,b1,r2,g2,b2);
					
					if (diff < 100) {
						motionDiffPixels[pixelLoc] = 0x00FFFFFF;
					} else {
						
						if (y >= lane1TopY && y <= lane1BottomY) {
							if (laneLeaders[0][i].x == -1) {
								laneLeaders[0][i].x = x;
								laneLeaders[0][i].y = y;
								lastLane1Leader = laneLeaders[0][i];
								
								if (canDetermineWinner && lane1FinishIndex == -1 && lastLane1Leader.x <= finishLineX) {
									lane1FinishIndex = i;
								}
							}
						}
						
						if (y >= lane2TopY && y <= lane2BottomY) {
							if (laneLeaders[1][i].x == -1) {
								laneLeaders[1][i].x = x;
								laneLeaders[1][i].y = y;
								lastLane2Leader = laneLeaders[1][i];
								
								if (canDetermineWinner && lane2FinishIndex == -1 && lastLane2Leader.x <= finishLineX) {
									lane2FinishIndex = i;
								}
							}
						}
						
						motionDiffPixels[pixelLoc] = 0;
					}
				}
			}
			
			if (laneLeaders[0][i].x == -1 && lastLane1Leader != null) laneLeaders[0][i] = lastLane1Leader;
			if (laneLeaders[1][i].x == -1 && lastLane2Leader != null) laneLeaders[1][i] = lastLane2Leader;
			
			motionDiffs[i].updatePixels();
		}
		
		playIndex = 0;
	}
	
	private Point getLastLeader(int leaderIndex, int index) {
		Point leader;
		for (int i = index; i >= 1; --i) {
			leader = laneLeaders[leaderIndex][index];
			if (leader.x != -1) return leader;
		}
		return null;
	}
	
	private String formatIndex(int lane, int index) {
		return String.format("[%d:t=%d,x=%d]", lane+1, camera.getTimestamp(index) - startTime, laneLeaders[lane][index].x);
	}
	
	public void play() {
		PImage frame;
		if (playOriginal)
			frame = camera.getFrame(playIndex);
		else
			frame = motionDiffs[playIndex];
		
		if (frame != null) {
			applet.set(0, 0, frame);
		
			applet.fill(0, 0, 0, 100);
			int left = camera.getWidth() - 300;
			applet.rect(left, 5, 295, 60);
			applet.fill(255, 255, 255);
			applet.text(String.format("%d/%d t=%d", playIndex, motionDiffs.length, camera.getTimestamp(playIndex) - startTime), left+5, 20);
			
			if (canDetermineWinner && lane1FinishIndex != -1 && lane2FinishIndex != -1) {
				String winner = null;
				if (camera.getTimestamp(lane1FinishIndex) < camera.getTimestamp(lane2FinishIndex)) {
					winner = "Lane 1";
				} else if (camera.getTimestamp(lane1FinishIndex) > camera.getTimestamp(lane2FinishIndex)) {
					winner = "Lane 2";
				} else {
					if (laneLeaders[0][lane1FinishIndex].x < laneLeaders[1][lane2FinishIndex].x) {
						winner = "Lane 1";
					} else if (laneLeaders[0][lane1FinishIndex].x > laneLeaders[1][lane2FinishIndex].x) {
						winner = "Lane 2";
					} else {
						winner = "TIE?";
					}
				}
				applet.text("Winner: "+winner, left+5, 40);	
				applet.text(String.format("%s %s", formatIndex(0, lane1FinishIndex), formatIndex(1, lane2FinishIndex)), left+5, 60);
			} else {
				applet.text(String.format("Invalid. det:%s idx1:%d idx2:%d", canDetermineWinner, lane1FinishIndex, lane2FinishIndex), left+5, 40);
			}
		}
		
		Point leader1;
		leader1 = getLastLeader(0, playIndex);
		if (leader1 != null) {
			applet.stroke(255, 0, 0);
			applet.line(leader1.x, 0, leader1.x, camera.getHeight());
			applet.stroke(0, 0, 0);
		}
	
		Point leader2;
		leader2 = getLastLeader(1, playIndex);
		if (leader2 != null) {
			applet.stroke(0, 255, 0);
			applet.line(leader2.x, 0, leader2.x, camera.getHeight());
			applet.stroke(0, 0, 0);
		}
		
		if (!stepPlay)
			++playIndex;
		
		if (playIndex == motionDiffs.length)
			playIndex = 0;
	}
	
	public void toggleManualStep() {
		stepPlay = !stepPlay;
	}
	
	public void togglePlayOriginal() {
		playOriginal = !playOriginal;
	}
	
	public void stepForward() {
		playIndex++;
		if (playIndex == motionDiffs.length)
			playIndex = 0;
	}
	
	public void stepBackward() {
		playIndex--;
		if (playIndex < 0)
			playIndex = motionDiffs.length - 1;
	}
}
