package bgu.spl.a2.test;

class Def {

	public void subscibe(Runnable run) {

		run.run();
	}
}

public class TempTask {

	static int num = 0;

	Def df;
	int cur;

	public TempTask() {
		this.cur = num;
		num++;
		this.df = new Def();
	}

	public static void main(String[] args) {

		TempTask t = new TempTask();
		System.out.println(t.cur);
		t.test();
	}

	public void notify1() {
		System.out.println(this.cur);
	}

	public void subscribe(Runnable callback) {

		this.df.subscibe(callback);

	}

	public void test() {
		TempTask test = new TempTask();
		System.out.println(test.cur);

		test.subscribe(() -> {
			notify1();
		});
	}

}
