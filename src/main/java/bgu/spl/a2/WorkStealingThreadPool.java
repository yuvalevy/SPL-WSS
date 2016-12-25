package bgu.spl.a2;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;

class Pair<T, U> {

	private final T first;
	private final U second;

	public Pair(T first, U second) {
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return this.first;
	}

	public U getSecond() {
		return this.second;
	}
}

/**
 * represents a work stealing thread pool - to understand what this class does
 * please refer to your assignment.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class WorkStealingThreadPool {

	private LinkedList<Pair<Thread, ConcurrentLinkedDeque<Task<?>>>> processorsInfo;

	private int nthreads;
	private VersionMonitor monitor;
	private CountDownLatch latch;

	/**
	 * creates a {@link WorkStealingThreadPool} which has nthreads
	 * {@link Processor}s. Note, threads should not get started until calling to
	 * the {@link #start()} method.
	 *
	 * Implementors note: you may not add other constructors to this class nor
	 * you allowed to add any other parameter to this constructor - changing
	 * this may cause automatic tests to fail..
	 *
	 * @param nthreads
	 *            the number of threads that should be started by this thread
	 *            pool
	 */
	public WorkStealingThreadPool(int nthreads) {

		this.latch = new CountDownLatch(nthreads);
		this.nthreads = nthreads;
		this.processorsInfo = new LinkedList<Pair<Thread, ConcurrentLinkedDeque<Task<?>>>>();
		this.monitor = new VersionMonitor();

		Pair<Thread, ConcurrentLinkedDeque<Task<?>>> triple;
		Processor p;
		Thread first;
		ConcurrentLinkedDeque<Task<?>> second;

		for (int i = 0; i < nthreads; i++) {

			p = new Processor(i, this);
			first = new Thread(p);
			second = new ConcurrentLinkedDeque<Task<?>>();

			triple = new Pair<Thread, ConcurrentLinkedDeque<Task<?>>>(first, second);
			this.processorsInfo.add(triple);
		}

	}

	/**
	 * Returns the monitor
	 *
	 */
	VersionMonitor getMonitor() {

		return this.monitor;
	}

	/**
	 * Returns the first Task in the queue of the processor 'id'
	 *
	 * @param id
	 * @return
	 */
	Task<?> getNextTask(int id) {

		return this.processorsInfo.get(id).getSecond().pollFirst();
	}

	int getNthreads() {
		return this.nthreads;
	}

	/**
	 * Takes max of half the size of the victimId processor's tasks and gives it
	 * to the thiefId processor, then returns true If there are no tasks to
	 * steal, returns false.
	 *
	 * @param thiefId
	 * @param victimId
	 * @return
	 */
	boolean steal(int thiefId, int victimId) {

		ConcurrentLinkedDeque<Task<?>> thief = this.processorsInfo.get(thiefId).getSecond();
		ConcurrentLinkedDeque<Task<?>> victim = this.processorsInfo.get(victimId).getSecond();

		int tasksCount = victim.size() / 2;

		if (tasksCount == 0) {
			return false;
		} else {

			for (; tasksCount != 0; tasksCount--) {
				Task<?> task = victim.pollLast();
				if (task == null) {
					break;
				}
				thief.addLast(task);
			}

			return true;
		}
	}

	/**
	 * Adds @param task to the processor with @param id at his top(last) of the
	 * queue
	 *
	 * @param id
	 *            Id of the processor
	 * @param task
	 *            The task to be added
	 */
	void submitById(int id, Task<?> task) {

		this.processorsInfo.get(id).getSecond().addLast(task);
		this.monitor.inc();
	}

	public CountDownLatch getLatch() {
		return this.latch;
	}

	/**
	 * closes the thread pool - this method interrupts all the threads and wait
	 * for them to stop - it is returns *only* when there are no live threads in
	 * the queue.
	 *
	 * after calling this method - one should not use the queue anymore.
	 *
	 * @throws InterruptedException
	 *             if the thread that shut down the threads is interrupted
	 * @throws UnsupportedOperationException
	 *             if the thread that attempts to shutdown the queue is itself a
	 *             processor of this queue
	 */
	public void shutdown() throws InterruptedException {

		for (Pair<Thread, ConcurrentLinkedDeque<Task<?>>> pair : this.processorsInfo) {
			pair.getFirst().interrupt();
		}

		this.latch.await();
	}

	/**
	 * start the threads belongs to this thread pool
	 */
	public void start() {

		for (Pair<Thread, ConcurrentLinkedDeque<Task<?>>> pair : this.processorsInfo) {
			pair.getFirst().start();
		}
	}

	/**
	 * submits a task to be executed by a processor belongs to this thread pool
	 *
	 * @param task
	 *            the task to execute
	 */
	public void submit(Task<?> task) {
		submitById(new Random().nextInt(this.nthreads), task);
	}

}
