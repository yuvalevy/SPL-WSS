package bgu.spl.a2;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;

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

	private LinkedList<Triple<Thread, ConcurrentLinkedDeque<Task<?>>, VersionMonitor>> processorsInfo;
	private int nthreads;
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

		latch = new CountDownLatch(nthreads);
		this.nthreads = nthreads;
		this.processorsInfo = new LinkedList<Triple<Thread, ConcurrentLinkedDeque<Task<?>>, VersionMonitor>>();

		Triple<Thread, ConcurrentLinkedDeque<Task<?>>, VersionMonitor> triple;
		Processor p;
		Thread first;
		ConcurrentLinkedDeque<Task<?>> second;
		VersionMonitor third;

		for (int i = 0; i < nthreads; i++) {

			p = new Processor(i, this);
			first = new Thread(p);
			second = new ConcurrentLinkedDeque<Task<?>>();
			third = new VersionMonitor();

			triple = new Triple<Thread, ConcurrentLinkedDeque<Task<?>>, VersionMonitor>(first, second, third);
			this.processorsInfo.add(triple);
		}

	}

	/**
	 * submits a task to be executed by a processor belongs to this thread pool
	 *
	 * @param task
	 *            the task to execute
	 */
	public void submit(Task<?> task) {
		submitById(new Random().nextInt(nthreads), task);
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

		processorsInfo.get(id).getSecond().addLast(task);
		processorsInfo.get(id).getThird().inc();
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

		for (Triple<Thread, ConcurrentLinkedDeque<Task<?>>, VersionMonitor> triple : processorsInfo) {
			triple.getFirst().interrupt();
		}

		this.latch.await();
	}

	/**
	 * start the threads belongs to this thread pool
	 */
	public void start() {

		for (Triple<Thread, ConcurrentLinkedDeque<Task<?>>, VersionMonitor> triple : processorsInfo) {
			triple.getFirst().start();
		}
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
	 * Returns monitor by id
	 * 
	 * @param id
	 * @return
	 */
	VersionMonitor getMonitor(int id) {

		return processorsInfo.get(id).getThird();
	}

	int getNthreads() {
		return this.nthreads;
	}

	public CountDownLatch getLatch() {
		return this.latch;
	}

}

class Triple<T, U, V> {

	private final T first;
	private final U second;
	private final V third;

	public Triple(T first, U second, V third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public T getFirst() {
		return first;
	}

	public U getSecond() {
		return second;
	}

	public V getThird() {
		return third;
	}
}
