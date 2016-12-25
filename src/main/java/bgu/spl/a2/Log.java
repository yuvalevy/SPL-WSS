package bgu.spl.a2;

public class Log {

	private static void log(String msg) {

		synchronized (System.out) {
			System.out.println("[" + Thread.currentThread().getId() + "] " + msg);
		}

	}

}
