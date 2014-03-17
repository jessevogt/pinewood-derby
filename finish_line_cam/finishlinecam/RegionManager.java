package finishlinecam;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;

public class RegionManager {
	
	private final PApplet applet;
	
	private Region regionToDefine;
	private Point regionToDefineStartPoint;
	
	private final int cameraWidth;
	private final int cameraHeight;
	
	private Map<Region, Rect> regions;

	public RegionManager(PApplet applet, int width, int height) {
		this.applet = applet;
		
		this.cameraWidth = width;
		this.cameraHeight = height;
		
		HashMap<Region, Rect> regions = new HashMap<Region, Rect>();
		for (Region region : Region.values())
			regions.put(region, null);
		this.regions = regions;
	}
	
	public void setRegionToDefine(Region region) {
		regionToDefine = region;
		regionToDefineStartPoint = null;
	}
	
	public boolean isDefiningRegion() {
		return regionToDefine != null && regionToDefineStartPoint != null;
	}
	
	public void startDefiningRegion() {
		if (regionToDefine != null) {
			regionToDefineStartPoint = new Point(applet.mouseX, applet.mouseY);
		}
	}
	
	public void finishDefiningRegion() {
		if (isDefiningRegion()) {
			Point p;
			int width, height;
			if (regionToDefine == Region.FINISH_LINE) {
				p = new Point(0,0);
				height = cameraHeight;
				width = Math.max(regionToDefineStartPoint.x, applet.mouseX);
			} else {
				p = new Point(0, Math.min(applet.mouseY, regionToDefineStartPoint.y));
				width = cameraWidth;
				height = Math.max(applet.mouseY, regionToDefineStartPoint.y) - p.y;
			}
			
			Rect rect = new Rect(p, width, height);
			regions.put(regionToDefine, rect);
			regionToDefine = null;
			regionToDefineStartPoint = null;
		}
	}
	
	public void draw() {
		if (isDefiningRegion()) {
			applet.noFill();
			applet.stroke(regionToDefine.red, regionToDefine.green, regionToDefine.blue);
			applet.rect(
				regionToDefineStartPoint.x, regionToDefineStartPoint.y,
				applet.mouseX - regionToDefineStartPoint.x, applet.mouseY - regionToDefineStartPoint.y
			);
			
			applet.fill(regionToDefine.red, regionToDefine.green, regionToDefine.blue);
			applet.text(regionToDefine.toString(), regionToDefineStartPoint.x, regionToDefineStartPoint.y);
		}
		
		Rect lane1 = regions.get(Region.LANE1);
		if (lane1 != null) drawRegion(Region.LANE1, lane1);
		Rect lane2 = regions.get(Region.LANE2);
		if (lane2 != null) drawRegion(Region.LANE2, lane2);
		Rect finish = regions.get(Region.FINISH_LINE);
		if (finish != null) drawRegion(Region.FINISH_LINE, finish);
		
	}
	
	private void drawRegion(Region region, Rect rect) {
		applet.noStroke();
		applet.fill(region.red, region.green, region.blue, 40);
		applet.rect(rect.topLeft.x, rect.topLeft.y, rect.width, rect.height);

		applet.fill(region.red, region.green, region.blue);
		applet.text(region.toString(), rect.topLeft.x + 10, (rect.topLeft.y + rect.height / 2));
	}
	
	public void saveRegions() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("regions.data"));
			out.writeObject(regions);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Rect getRegionRect(Region region) {
		return regions.get(region);
	}
	
	@SuppressWarnings("unchecked")
	public void loadRegions() {
		try {
			FileInputStream in = new FileInputStream("regions.data");
			ObjectInputStream reader = new ObjectInputStream(in);
			regions = (Map<Region, Rect>)reader.readObject();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}