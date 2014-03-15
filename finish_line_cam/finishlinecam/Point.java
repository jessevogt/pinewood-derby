package finishlinecam;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Point implements Serializable {
	public final int x;
	public final int y;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
