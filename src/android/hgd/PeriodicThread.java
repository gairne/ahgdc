package android.hgd;

public class PeriodicThread extends Thread {

	private long period;
	private WorkerThread worker;
	
	public PeriodicThread(long period, WorkerThread worker) {
		this.period = period;
		this.worker = worker;
	}
	
	@Override
	public void run() {
		while (true) {
			loop();
		}
	}
	
	public void loop() {
		try {
			while(true) {
				Thread.sleep(period);
				worker.getPlaylist();
			}
		} catch (InterruptedException e) {
			
		}
		
	}
}
