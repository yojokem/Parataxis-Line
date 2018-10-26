package kr.frostq.Axis;

import java.util.Vector;

public class ThreadManager {
	public static Thread managingThread;
	public static Vector<Thread> threads = new Vector<Thread>();
	
	public static void startManaging() {
		System.out.println("[ThreadManager] Started managing..");
		managingThread = createThread(manage(), "ComplexLauncher | ThreadManager | Thread Managing");
	}
	
	public static void stopManaging() {
		System.out.println("[ThreadManager] Stopped managing..");
		if(managingThread != null && managingThread.isAlive() && !managingThread.isInterrupted())
			managingThread.interrupt();
	}
	
	public static void runThread(Thread thr, int ...priority) {
		if(thr.isAlive()) return;
		
		thr.start();
		if(priority.length != 0) thr.setPriority(priority[0]);
	}
	
	protected static boolean update = false;
	
	public static Thread createThread(Runnable run, String name) {
		System.out.println("[ThreadManager] Created thread: '" + name + "'");
		Thread t = new Thread(run, name);
		threads.add(t);
		update = true;
		return t;
	}
	
	public static boolean interruptThread(Thread thr) {
		thr.interrupt();
		return thr.isInterrupted();
	}
	
	private static Runnable manage() {
		return () -> {
			if(update) {
				update = false;
				
				System.out.println("[ThreadManager] Update!");
				System.out.println("[ThreadManager] Thread Active Count: " + Thread.activeCount());
			}
		};
	}
}