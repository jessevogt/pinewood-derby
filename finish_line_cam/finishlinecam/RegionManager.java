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
	
	private Map<Region, Rect> regions;

	public RegionManager(PApplet applet) {
		this.applet = applet;
		
		HashMap<Region, Rect> regions = new HashMap<Region, Rect>();
		for (Region region : Region.values())
			regions.put(region, null);
		this.regions = regions;
	}
	
	public void setRegionToDefine(Region region) {
		regionToDefine = region;
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
			Rect rect = new Rect(regionToDefineStartPoint,
					applet.mouseX - regionToDefineStartPoint.x, applet.mouseY - regionToDefineStartPoint.y);
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
		
		Region region;
		Rect rect;
		for (Map.Entry<Region, Rect> entry  : regions.entrySet()) {
			rect = entry.getValue();
			if (rect != null) {
				region = entry.getKey();
				
				applet.noFill();
				applet.stroke(region.red, region.green, region.blue);
				applet.rect(rect.topLeft.x, rect.topLeft.y, rect.width, rect.height);
				
				applet.fill(region.red, region.green, region.blue);
				applet.text(region.toString(), rect.topLeft.x, rect.topLeft.y);
			}
		}
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