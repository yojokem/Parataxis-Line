package kr.frostq.Axis;

public class Synchronization {
	private Runnable r;
	
	public Synchronization() {}
	
	public static Synchronization sync(Runnable b) {
		Synchronization s = new Synchronization();
		s.r = b;
		return s;
	}
	
	public static boolean processSync(Synchronization s) {
		try {
			s.process();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private void process() {
		synchronized (this) {
			r.run();
		}
	}
}