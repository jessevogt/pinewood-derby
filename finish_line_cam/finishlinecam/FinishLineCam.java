package finishlinecam;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;

@SuppressWarnings("serial")
public class FinishLineCam extends PApplet {
	private Camera cam;
	private RegionManager regionManager;
	
	private static interface Action {
		void act();
	}
	
	private Map<Integer, Action> keyboardActions;
	
	@Override
	public void setup() {
		int width = 1280;
		int height = 800;
		int frameRate = 30;
		int secondsToRecordInSegment = 1;
	
		size(width, height, P2D);
		
		cam = new Camera(this, width, height, frameRate, secondsToRecordInSegment);
		regionManager = new RegionManager(this);
		
		keyboardActions = new HashMap<Integer, Action>();
		// 0
		keyboardActions.put(Integer.valueOf(48), new Action() {
			@Override public void act() {
				regionManager.setRegionToDefine(Region.FINISH_LINE);
			}
		});
		// 1
		keyboardActions.put(Integer.valueOf(49), new Action() {
			@Override public void act() {
				regionManager.setRegionToDefine(Region.LANE1);
			}
		});
		// 2
		keyboardActions.put(Integer.valueOf(50), new Action() {
			@Override public void act() {
				regionManager.setRegionToDefine(Region.LANE2);
			}
		});
		// SPACE
		keyboardActions.put(Integer.valueOf(32), new Action() {
			@Override public void act() {
				regionManager.setRegionToDefine(null);
			}
		});
		
		cam.start();
	}

	@Override
	public void draw() {
		cam.snap(false);
		regionManager.draw();
	}

	@Override
	public void keyPressed() {
		Action action = keyboardActions.get(Integer.valueOf(keyCode));
		if (action != null) {
			action.act();
		}
	}

	@Override
	public void mousePressed() {
		if (regionManager.isDefiningRegion())
			regionManager.finishDefiningRegion();
		else
			regionManager.startDefiningRegion();
	}
}
