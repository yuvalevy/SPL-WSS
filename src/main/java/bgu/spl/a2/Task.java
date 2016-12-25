package bgu.spl.a2;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * an abstract class that represents a task that may be executed using the
 * {@link WorkStealingThreadPool}
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 * @param <R>
 *            the task result type
 */
public abstract class Task<R> {

	private boolean isStarted = false;

	private Processor handler;
	private Deferred<R> defrred = new Deferred<R>();
	private Runnable resolvedCallback;

	/**
	 *
	 * start/continue handling the task
	 *
	 * this method should be called by a processor in order to start this task
	 * or continue its execution in the case where it has been already started,
	 * any sub-tasks / child-tasks of this task should be submitted to the queue
	 * of the handler that handles it currently
	 *
	 * IMPORTANT: this method is package protected, i.e., only classes inside
	 * the same package can access it - you should *not* change it to
	 * public/private/protected
	 *
	 * @param handler
	 *            the handler that wants to handle the task
	 */
	final void handle(Processor handler) {

		this.handler = handler;

		if (!this.isStarted) {
			this.isStarted = true;
			start();
		} else {
			this.resolvedCallback.run();
		}
	}

	/**
	 * resolve the internal result - should be called by the task derivative
	 * once it is done.
	 *
	 * @param result
	 *            - the task calculated result
	 */
	protected final void complete(R result) {

		this.defrred.resolve(result);
	}

	/**
	 * This method schedules a new task (a child of the current task) to the
	 * same processor which currently handles this task.
	 *
	 * @param task
	 *            the task to execute
	 */
	protected final void spawn(Task<?>... task) {

		this.handler.addToQueue(task);
	}

	/**
	 * start handling the task - note that this method is protected, a handler
	 * cannot call it directly but instead must use the
	 * {@link #handle(bgu.spl.a2.Processor)} method
	 */
	protected abstract void start();

	/**
	 * add a callback to be executed once *all* the given tasks results are
	 * resolved
	 *
	 * Implementors note: make sure that the callback is running only once when
	 * all the given tasks completed.
	 *
	 * @param tasks
	 * @param callback
	 *            the callback to execute once all the results are resolved
	 */
	protected final void whenResolved(Collection<? extends Task<?>> tasks, Runnable callback) {

		this.resolvedCallback = callback;

		// In case whenResolved is called more than once
		AtomicInteger count = new AtomicInteger(tasks.size());

		for (Task<?> task : tasks) {
			task.getResult().whenResolved(
					/**
					 * Decreases the results counter by one. When counter is 0,
					 * it means that all sub-tasks finished and it is time to
					 * return the task to the processor
					 */
					() -> {

						count.decrementAndGet();
						if (count.get() == 0) {
							spawn(this);
						}
					});
		}
	}

	/**
	 * @return this task deferred result
	 */
	public final Deferred<R> getResult() {

		return this.defrred;
	}

}
