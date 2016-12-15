package bgu.spl.a2.test;

public class TestWait {
	public static void main(String[] args) {

		Test1 t = new Test1();

		Thread th = new Thread(t);
		th.start();

		System.out.println("After");

		synchronized (t) {

			System.out.println(th.isAlive());

			System.out.println(th.getState());

			t.notifyAll();
			System.out.println("Notified");
		}

		System.out.println("AfteAfterr");

	}
}
