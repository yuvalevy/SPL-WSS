package bgu.spl.a2.test;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;

public class SumMatrix extends Task<int[]> {
	private int[][] array;

	public SumMatrix(int[][] array) {
		this.array = array;
	}

	protected void start() {
		List<Task<Integer>> tasks = new ArrayList<>();
		int rows = array.length;
		for (int i = 0; i < rows; i++) {
			SumRow newTask = new SumRow(array, i);
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

	public static void main(String[] args) {

		WorkStealingThreadPool pool = new WorkStealingThreadPool(2);
		int[][] array = new int[5][2];

		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				array[i][j] = i;
			}
		}

		SumMatrix myTask = new SumMatrix(array);

		pool.submit(myTask);
		pool.start();

		try {
			Thread.sleep(150);
			pool.shutdown();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int[] js = myTask.getResult().get();

		for (int i = 0; i < array.length; i++) {
			System.out.println("row: " + i + ": " + js[i]);
		}
	}
}
