import processing.video.*;
import java.lang.ProcessBuilder;
import java.io.InputStreamReader;

public class LogStreamReader implements Runnable {

    private BufferedReader reader;

    public LogStreamReader(InputStream is) {
        this.reader = new BufferedReader(new InputStreamReader(is));
    }

    public void run() {
        try {
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Currently needed to allow 6144MB of memory

int videoWidth = 1280;
int videoHeight = 720;
int videoFrameRate = 30;
int recordSeconds = 20;

int videoFrameCount = recordSeconds * videoFrameRate;
int recordFrameCounter = 0;
int playbackFrameCounter = 0;
PImage[] recordedFrames;
long[] recordedFrameTimestamp;
PImage[] motionDetectedFrames;
PImage initialRecordFrame;

Capture cam;

void resetDataStructs() {
  recordedFrames = new PImage[videoFrameCount];
  motionDetectedFrames = new PImage[videoFrameCount];
  recordedFrameTimestamp = new long[videoFrameCount];
  
  initialRecordFrame = createImage(videoWidth, videoHeight, RGB);
  for (int i = 0; i < videoFrameCount; ++i) {
    println("Allocating memory for frame " + (i+1) + "/" + videoFrameCount);
    recordedFrames[i] = createImage(videoWidth, videoHeight, RGB);
    motionDetectedFrames[i] = createImage(videoWidth, videoHeight, RGB);
    recordedFrameTimestamp[i] = 0l; 
  }
  
  recordFrameCounter = playbackFrameCounter = 0;
}

void setup(){
  
  ProcessBuilder pb = new ProcessBuilder(
      "/Users/jesse/personal/Dropbox/projects/pinewood-derby-timer/starting-pistol/starting-pistol",
      "server", "127.0.0.1"
  );
  
  pb.redirectErrorStream(true);
  Process p = null;
  try {
    p = pb.start();
  } catch (Exception e) {
   println(e); 
  }
  InputStream s = p.getInputStream();
  
  
  LogStreamReader reader = new LogStreamReader(s);
  Thread thread = new Thread(reader);
  thread.start();

  resetDataStructs();
  
  size(videoWidth, videoHeight);
  frameRate(videoFrameRate);
    
  String[] cameras = Capture.list();
  if (cameras.length == 0) {
    println("There are no cameras available for capture");
    exit();
  } else {
    for (int i = 0; i < cameras.length; ++i)
      println(cameras[i]);
  }
  
  //cam = new Capture(this, width, height, "FaceTime HD Camera (Built-in)", 30);
  cam = new Capture(this, videoWidth, videoHeight, "Microsoft® LifeCam HD-3000", videoFrameRate);
  cam.start();
}

void draw() {
  if (recordFrameCounter == videoFrameCount) {
  } else {
    if (cam.available() == true) {
      cam.read(); 
    }
    recordedFrameTimestamp[recordFrameCounter] = System.currentTimeMillis();
    recordedFrames[recordFrameCounter].copy(
      cam,
      0, 0, videoWidth, videoHeight,
      0, 0, videoWidth, videoHeight
    );
    set(0, 0, cam);
    ++recordFrameCounter; 
  }
  /*
  if (frameCounter == frameCount) {
    if (playbackFrameCounter == frameCount) {
      playbackFrameCounter = 0;
    }
      
    currentFrame = motionFrames[playbackFrameCounter];
    set(0, 0, currentFrame);
    playbackFrameCounter++;
    
    
    
  } else {
   if (cam.available() == true) {
    cam.read();
   }
  
   currentFrame = frames[frameCounter]; 
   currentFrame.copy(cam, 0, 0, width, height, 0, 0, width, height);
   currentFrame.updatePixels();
 
   set(0, 0, cam);
   frameCounter++;
   
   if (frameCounter == frameCount) {
     noLoop();
     motionFrames = calculateMotion(frames);
     loop();
   }
  }
  */
}

/*
PImage[] calculateMotion(PImage[] frames) {
  PImage[] motionFrames = new PImage[frames.length];
  for (int i = 0; i < frameCount; ++i) {
    motionFrames[i] = createImage(width, height, RGB);  
  }
  motionFrames[0].copy(frames[0], 0, 0, width, height, 0, 0, width, height);
  
  PImage currentFrame;
  PImage previousFrame;
  for (int t = 1; t < frameCount; ++t) {
    previousFrame = frames[t - 1];
    currentFrame = frames[t];
    for (int x = 0; x < width; ++x) {
       for (int y = 0; y < height; ++y) {
         int loc = x + y * width;
         color current = currentFrame.pixels[loc];
         color previous = previousFrame.pixels[loc];
         
         float r1 = red(current); float g1 = green(current); float b1 = blue(current);
        float r2 = red(previous); float g2 = green(previous); float b2 = blue(previous);
        float diff = dist(r1,g1,b1,r2,g2,b2);
        
          if (diff > 50)
            motionFrames[t].pixels[loc] = color(0);
          else
            motionFrames[t].pixels[loc] = color(255);
       } 
    }
  }
  
  return motionFrames;
}

PImage currentFrame;
*/




