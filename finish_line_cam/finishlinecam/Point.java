package finishlinecam;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Point implements Serializable {
	public int x;
	public int y;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
