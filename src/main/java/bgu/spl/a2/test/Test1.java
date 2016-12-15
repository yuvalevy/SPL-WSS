package bgu.spl.a2.test;

public class Test1 implements Runnable {

	public synchronized void func() {
		try {
			System.out.println("try1");
			wait();
			System.out.println("try2");
		} catch (InterruptedException e) {
			System.out.println("catch");
			e.printStackTrace();
		}

		System.out.println("Finish func");
	}

	@Override
	public void run() {
		func();
	}
}