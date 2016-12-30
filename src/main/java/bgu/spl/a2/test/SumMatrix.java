package bgu.spl.a2.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;

public class SumMatrix extends Task<int[]> {
	private int[][] array;

	public SumMatrix(int[][] array) {
		this.array = array;
	}

	public static void main(String[] args) {

		for (int j1 = 0; j1 < 150; j1++) {

			WorkStealingThreadPool pool = new WorkStealingThreadPool(3);
			int[][] array = new int[1000][100];

			Random r = new Random();
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[i].length; j++) {
					array[i][j] = r.nextInt(1000);
				}
			}

			SumMatrix myTask = new SumMatrix(array);

			pool.submit(myTask);
			pool.start();

			CountDownLatch l = new CountDownLatch(1);

			myTask.getResult().whenResolved(() -> {
				l.countDown();
			});

			try {
				l.await();
				pool.shutdown();
				int[] js = myTask.getResult().get();

				System.out.println(test(array, js));
				// for (int i = 0; i < array.length; i++) {
				// System.out.println("row: " + i + ": " + js[i]);
				// }
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}

	public static boolean test(int[][] array, int[] result) {

		for (int i = 0; i < array.length; i++) {
			int sum = 0;
			for (int j = 0; j < array[i].length; j++) {
				sum += array[i][j];
			}
			if (sum != result[i]) {
				System.out.println("######");
				return false;
			}
		}

		return true;

	}

	@Override
	protected void start() {
		List<Task<Integer>> tasks = new ArrayList<>();
		int rows = this.array.length;
		for (int i = 0; i < rows; i++) {
			SumRow newTask = new SumRow(this.array, i);
			spawn(newTask);
			tasks.add(newTask);
		}

		whenResolved(tasks, () -> {

			int[] res = new int[rows];
			for (int j = 0; j < rows; j++) {
				res[j] = tasks.get(j).getResult().get();
			}
			complete(res);
		});
	}
}