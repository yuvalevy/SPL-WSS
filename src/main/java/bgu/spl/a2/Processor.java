package bgu.spl.a2;

/**
 * this class represents a single work stealing processor, it is
 * {@link Runnable} so it is suitable to be executed by threads.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 *
 */
public class Processor implements Runnable {

	private final WorkStealingThreadPool pool;
	private final int id;
	private final int nprocessors;
	private int victimId;
	private VersionMonitor monitor;

	/**
	 * constructor for this class
	 *
	 * IMPORTANT: 1) this method is package protected, i.e., only classes inside
	 * the same package can access it - you should *not* change it to
	 * public/private/protected
	 *
	 * 2) you may not add other constructors to this class nor you allowed to
	 * add any other parameter to this constructor - changing this may cause
	 * automatic tests to fail..
	 *
	 * @param id
	 *            - the processor id (every processor need to have its own
	 *            unique id inside its thread pool)
	 * @param pool
	 *            - the thread pool which owns this processor
	 */
	Processor(int id, WorkStealingThreadPool pool) {
		this.id = id;
		this.pool = pool;
		this.nprocessors = pool.getNthreads();
		this.victimId = (id + 1) % this.nprocessors;
		this.monitor = this.pool.getMonitor();

	}

	/**
	 * Add the tasks to the processor's queue
	 *
	 * @param tasks
	 */
	void addToQueue(Task<?>... tasks) {

		for (Task<?> task : tasks) {
			this.pool.submitById(this.id, task);
		}
	}

	@Override
	public void run() {

		Task<?> currentTask = null;

		while (!Thread.currentThread().isInterrupted()) {

			int version = this.monitor.getVersion();
			currentTask = this.pool.getNextTask(this.id);

			if (currentTask != null) {

				currentTask.handle(this);

			} else {

				steal(version);

			}
		}

		this.pool.getLatch().countDown();
	}

	/**
	 * Steals half of the tasks from another processors
	 */
	private void steal(int version) {

		if (!this.pool.steal(this.id, this.victimId)) {

			try {

				this.monitor.await(version);

			} catch (InterruptedException e) {

				Thread.currentThread().interrupt();
			}
		}

		this.victimId = (this.victimId + 1) % this.nprocessors;

		if (this.victimId == this.id) {
			this.victimId = (this.victimId + 1) % this.nprocessors;
		}
	}
}
