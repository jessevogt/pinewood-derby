package finishlinecam;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;

@SuppressWarnings("serial")
public class FinishLineCam extends PApplet {
	private Camera cam;
	private RegionManager regionManager;
	private WinnerProcessor winnerProcessor;
	private StartingPistol startingPistol;
	
	private static enum Mode {
		DEFINE_REGION, RECORDING, PLAYBACK;
	}
	private Mode currentMode = Mode.DEFINE_REGION;
	
	private Map<Mode, Map<Integer, Runnable>> modeKeyboardActions;
	private Map<Integer, Runnable> globalKeyboardActions;
	
	@Override
	public void setup() {
//		int videoWidth = 1280;
//		int videoHeight = 720;
		int videoWidth = 1280/2;
		int videoHeight = 720/2;
//		int videoWidth = 640;
//		int videoHeight = 400;
		int videoFrameRate = 30;
		int secondsToRecordInSegment = 5;
	
		size(videoWidth, videoHeight, P2D);
		frameRate(60);
		
		cam = new Camera(this, videoWidth, videoHeight, videoFrameRate, secondsToRecordInSegment);
		regionManager = new RegionManager(this, videoWidth, videoHeight);
		winnerProcessor = new WinnerProcessor(this, cam, regionManager);
		startingPistol = new StartingPistol();
		
		globalKeyboardActions = new HashMap<Integer, Runnable>();
		globalKeyboardActions.put(Integer.valueOf('D'), new Runnable() {
			@Override public void run() {
				println("Switching to region definition mode");
				currentMode = Mode.DEFINE_REGION;
			}
		});
		globalKeyboardActions.put(Integer.valueOf('R'), new Runnable() {
			@Override public void run() {
				println("Switching to recording mode");
				currentMode = Mode.RECORDING;
			}
		});
		globalKeyboardActions.put(Integer.valueOf('P'), new Runnable() {
			@Override public void run() {
				println("Switching to playback mode");
				currentMode = Mode.PLAYBACK;
			}
		});
		
		modeKeyboardActions = new HashMap<Mode, Map<Integer,Runnable>>();
		
		// Region Actions
		Map<Integer, Runnable> regionActions = new HashMap<Integer, Runnable>();
		modeKeyboardActions.put(Mode.DEFINE_REGION, regionActions);
		
		regionActions.put(Integer.valueOf('3'), new Runnable() {
			@Override public void run() {
				println("In region define mode. Switching to finish line definition");
				regionManager.setRegionToDefine(Region.FINISH_LINE);
			}
		});
		regionActions.put(Integer.valueOf('1'), new Runnable() {
			@Override public void run() {
				println("In region define mode. Switching to lane 1 definition");
				regionManager.setRegionToDefine(Region.LANE1);
			}
		});
		regionActions.put(Integer.valueOf('2'), new Runnable() {
			@Override public void run() {
				println("In region define mode. Switching to lane 2 definition");
				regionManager.setRegionToDefine(Region.LANE2);
			}
		});
		regionActions.put(Integer.valueOf('0'), new Runnable() {
			@Override public void run() {
				println("In region define mode. Clearing current region definition");
				regionManager.setRegionToDefine(null);
			}
		});
		regionActions.put(Integer.valueOf('S'), new Runnable() {
			@Override public void run() {
				println("Saving current regions to disk");
				regionManager.saveRegions();
			}
		});
		regionActions.put(Integer.valueOf('L'), new Runnable() {
			@Override public void run() {
				println("Loading previous regions from disk");
				regionManager.loadRegions();
			}
		});
		
		// Recording actions
		Map<Integer, Runnable> recordingActions = new HashMap<Integer, Runnable>();
		modeKeyboardActions.put(Mode.RECORDING, recordingActions);
		
		recordingActions.put(Integer.valueOf(' '), new Runnable() {
			@Override public void run() {
				startRace();
			}
		});
		
		// Playback actions
		Map<Integer, Runnable> playbackActions = new HashMap<Integer, Runnable>();
		modeKeyboardActions.put(Mode.PLAYBACK, playbackActions);
		
		// Left arrow
		playbackActions.put(Integer.valueOf(37), new Runnable() {
			@Override public void run() {
				winnerProcessor.stepBackward();
			}
		});
		// Right arrow
		playbackActions.put(Integer.valueOf(39), new Runnable() {
			@Override public void run() {
				winnerProcessor.stepForward();
			}
		});
		playbackActions.put(Integer.valueOf(' '), new Runnable() {
			@Override public void run() {
				winnerProcessor.toggleManualStep();
			}
		});
		playbackActions.put(Integer.valueOf(38), new Runnable() {
			@Override public void run() {
				winnerProcessor.togglePlayOriginal();
			}
		});
		playbackActions.put(Integer.valueOf(40), new Runnable() {
			@Override public void run() {
				winnerProcessor.togglePlayOriginal();
			}
		});
		
		cam.start();
		
		startingPistol.listen(this);
	}

	@Override
	public void draw() {
		if (currentMode == Mode.RECORDING) {
			cam.snap();
		} else {
			if (currentMode == Mode.PLAYBACK) {
				winnerProcessor.play();
			} else {
				cam.snap();
			}
			regionManager.draw();
		}
	}
	

	@Override
	public void keyPressed() {
		Integer keyPressInteger = Integer.valueOf(keyCode);
		println("key pressed:"+keyPressInteger);
		Runnable action = globalKeyboardActions.get(keyPressInteger);
		if (action != null) {
			action.run();
		} else {
			Map<Integer, Runnable> modeActions = modeKeyboardActions.get(currentMode);
			if (modeActions != null) {
				action = modeActions.get(keyPressInteger);
				if (action != null) action.run();
			}
		}
	}

	@Override
	public void mousePressed() {
		if (regionManager.isDefiningRegion())
			regionManager.finishDefiningRegion();
		else
			regionManager.startDefiningRegion();
	}
	
	public void startRace() {
		final long recordStartTime = System.currentTimeMillis();
		cam.setStartCapturing(new Runnable() {
			@Override public void run() {
				winnerProcessor.calc(recordStartTime);
				currentMode = Mode.PLAYBACK;
			}
		});
	}
}
