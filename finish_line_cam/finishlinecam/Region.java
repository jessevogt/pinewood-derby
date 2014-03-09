package finishlinecam;

public enum Region {
	FINISH_LINE	(0x00, 0xFF, 0x00),
	LANE1		(0xFF, 0x00, 0x00),
	LANE2		(0x00, 0x00, 0xFF)
	;
	
	public final int red;
	public final int blue;
	public final int green;
	
	private Region(final int r, final int g, final int b) {
		this.red = r;
		this.green = g;
		this.blue = b;
	}
}