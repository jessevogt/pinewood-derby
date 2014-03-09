package finishlinecam;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessReader implements Runnable {

	private BufferedReader reader;

	public ProcessReader(InputStream is) {
		this.reader = new BufferedReader(new InputStreamReader(is));
	}

	public void run() {
		try {
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				System.out.println(System.currentTimeMillis());
				System.out.println("------");
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
