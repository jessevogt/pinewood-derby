package finishlinecam;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Rect implements Serializable {
	final Point topLeft;
	final int width;
	final int height;
	
	public Rect(Point topLeft, int width, int height) {
		this.topLeft = topLeft;
		this.width = width;
		this.height = height;
	}
}
