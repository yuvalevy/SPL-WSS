/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.test;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;

class MergeSubArray extends Task<int[]> {

	private final int higherIndex;
	private final int lowerIndex;
	private final int[] array;
	private int[] resultArray;
	private int[] tempArray;
	private MergeSubArray subLeft;
	private MergeSubArray subRight;

	public MergeSubArray(int[] array, int lowerIndex, int higherIndex) {
		// System.out.println("[ " + lowerIndex + " , " + higherIndex + " ]");
		this.array = array;
		this.resultArray = new int[array.length];
		this.tempArray = new int[array.length];
		this.lowerIndex = lowerIndex;
		this.higherIndex = higherIndex;

	}

	@Override
	protected void start() {

		if (this.lowerIndex < this.higherIndex) {
			int middle = this.lowerIndex + ((this.higherIndex - this.lowerIndex) / 2);

			this.subLeft = new MergeSubArray(this.array, this.lowerIndex, middle);
			this.subRight = new MergeSubArray(this.array, middle + 1, this.higherIndex);

			spawn(this.subLeft, this.subRight);

			ArrayList<MergeSubArray> tasks = new ArrayList<MergeSubArray>();
			tasks.add(this.subLeft);
			tasks.add(this.subRight);

			whenResolved(tasks, () -> {
				// System.out.println("resolved range [ " + this.lowerIndex + "
				// , " + this.higherIndex + " ]");
				// Now merge both sides
				mergeParts(middle);
				// System.out.println("MERGED left right: [ " + this.lowerIndex
				// + " , " + this.higherIndex + " ]"
				// + Arrays.toString(this.resultArray));
				complete(this.resultArray);
			});
		} else if (this.lowerIndex == this.higherIndex) {

			// System.out.println("resolved range [ " + this.lowerIndex + " , "
			// + this.higherIndex + " ]");

			this.resultArray[this.lowerIndex] = this.array[this.lowerIndex];
			complete(this.resultArray);

		}

		// doMergeSort(middle + 1, this.higherIndex);
		// Now merge both sides
		// mergeParts(lowerIndex, middle, higherIndex);
	}

	private void mergeParts(int middle) {

		int[] left = this.subLeft.getResult().get();
		int[] right = this.subRight.getResult().get();

		// synchronized (System.out) {
		//
		// System.out.println("merging ranges [ " + this.lowerIndex + " , " +
		// this.higherIndex + " ]");
		// System.out.println("left: " + Arrays.toString(left));
		// System.out.println("right: " + Arrays.toString(right));
		// }

		// coping origin array
		for (int i = this.lowerIndex; i <= this.higherIndex; i++) {

			if (i <= middle) {
				this.tempArray[i] = left[i];
			} else {
				this.tempArray[i] = right[i];
			}
		}

		// System.out.println("ranged left right: [ " + this.lowerIndex + " , "
		// + this.higherIndex + " ]"
		// + Arrays.toString(this.tempArray));

		int i = this.lowerIndex;
		int j = middle + 1;
		int k = this.lowerIndex;

		// populating the sorted array in resultArray
		while ((i <= middle) && (j <= this.higherIndex)) {
			if (this.tempArray[i] <= this.tempArray[j]) {
				this.resultArray[k] = this.tempArray[i];
				i++;
			} else {
				this.resultArray[k] = this.tempArray[j];
				j++;
			}
			k++;
		}

		while (i <= middle) {
			this.resultArray[k] = this.tempArray[i];
			k++;
			i++;
		}
		while (j <= this.higherIndex) {
			this.resultArray[k] = this.tempArray[j];
			k++;
			j++;
		}

	}

}

public class MergeSort extends Task<int[]> {

	private final int[] array;
	private int[] resultArray;
	private int[] tempArray;
	private MergeSubArray subLeft;
	private MergeSubArray subRight;
	private int lowerIndex;
	private int higherIndex;

	public MergeSort(int[] array) {
		this.array = array;

		this.resultArray = new int[array.length];
		this.tempArray = new int[array.length];
		this.lowerIndex = 0;
		this.higherIndex = this.array.length - 1;
	}

	public static void main(String[] args) throws InterruptedException {

		for (int i = 0; i < 10; i++) {

			WorkStealingThreadPool pool = new WorkStealingThreadPool(10);
			int n = 10000;
			int[] array = new Random().ints(n, 0, n * n).toArray();
			// System.out.println(Arrays.toString(array));

			MergeSort task = new MergeSort(array);

			CountDownLatch l = new CountDownLatch(1);
			pool.start();
			pool.submit(task);
			task.getResult().whenResolved(() -> {
				// warning - a large print!! - you can remove this line if you
				// wish
				// System.out.println(Arrays.toString(task.getResult().get()));
				l.countDown();
			});

			l.await();
			pool.shutdown();

			System.out.println("--------------------");
			System.out.println(test(task.getResult().get()));
		}
	}

	public static boolean test(int[] array) {

		for (int i = 0; i < (array.length - 1); i++) {
			if (array[i] > array[i + 1]) {
				System.out.println("############");
				return false;
			}
		}

		return true;
	}

	@Override
	protected void start() {

		if (this.lowerIndex < this.higherIndex) {
			int middle = this.lowerIndex + ((this.higherIndex - this.lowerIndex) / 2);

			this.subLeft = new MergeSubArray(this.array, this.lowerIndex, middle);
			// Below step sorts the left side of the array
			// doMergeSort(lowerIndex, middle);

			this.subRight = new MergeSubArray(this.array, middle + 1, this.higherIndex);
			// Below step sorts the right side of the array
			// doMergeSort(middle + 1, higherIndex);

			spawn(this.subLeft, this.subRight);

			ArrayList<MergeSubArray> tasks = new ArrayList<MergeSubArray>();
			tasks.add(this.subLeft);
			tasks.add(this.subRight);

			whenResolved(tasks, () -> {

				// Now merge both sides
				mergeParts(middle);
				complete(this.resultArray);
			});

		}
	}

	private void mergeParts(int middle) {

		int[] left = this.subLeft.getResult().get();
		int[] right = this.subRight.getResult().get();

		// coping origin array
		for (int i = this.lowerIndex; i <= this.higherIndex; i++) {

			if (i <= middle) {
				this.tempArray[i] = left[i];
			} else {
				this.tempArray[i] = right[i];
			}
		}

		int i = this.lowerIndex;
		int j = middle + 1;
		int k = this.lowerIndex;

		// populating the sorted array in resultArray
		while ((i <= middle) && (j <= this.higherIndex)) {
			if (this.tempArray[i] <= this.tempArray[j]) {
				this.resultArray[k] = this.tempArray[i];
				i++;
			} else {
				this.resultArray[k] = this.tempArray[j];
				j++;
			}
			k++;
		}

		while (i <= middle) {
			this.resultArray[k] = this.tempArray[i];
			k++;
			i++;
		}

		while (j <= this.higherIndex) {
			this.resultArray[k] = this.tempArray[j];
			k++;
			j++;
		}

	}
}
