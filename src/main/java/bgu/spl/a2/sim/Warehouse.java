package bgu.spl.a2.sim;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.Tool;

/**
 * A class representing the warehouse in your simulation
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 */
public class Warehouse {

	private HashMap<String, ManufactoringPlan> plans;
	private HashMap<String, Tool> toolsObjects;
	private HashMap<String, AtomicInteger> toolsAmount;
	private HashMap<String, ConcurrentLinkedQueue<Deferred<Tool>>> toolsDeferres;

	/**
	 * Constructor
	 */
	public Warehouse() {

		this.plans = new HashMap<String, ManufactoringPlan>();
		this.toolsObjects = new HashMap<String, Tool>();
		this.toolsAmount = new HashMap<String, AtomicInteger>();
		this.toolsDeferres = new HashMap<String, ConcurrentLinkedQueue<Deferred<Tool>>>();
	}

	/**
	 * Tool acquisition procedure Note that this procedure is non-blocking and
	 * should return immediately
	 *
	 * @param type
	 *            - string describing the required tool
	 * @return a deferred promise for the requested tool
	 */
	public Deferred<Tool> acquireTool(String type) {

		Tool tool = this.toolsObjects.get(type);

		synchronized (tool) {

			Deferred<Tool> deferred = new Deferred<Tool>();

			if (this.toolsAmount.get(type).get() > 0) {

				deferred.resolve(tool);
				this.toolsAmount.get(type).decrementAndGet();

			} else {

				this.toolsDeferres.get(type).add(deferred);
			}
			return deferred;
		}

	}

	/**
	 * Store a ManufactoringPlan in the warehouse for later retrieval
	 *
	 * @param plan
	 *            - a ManufactoringPlan to be stored
	 */
	public void addPlan(ManufactoringPlan plan) {

		this.plans.put(plan.getProductName(), plan);
	}

	/**
	 * Store a qty Amount of tools of type tool in the warehouse for later
	 * retrieval
	 *
	 * @param tool
	 *            - type of tool to be stored
	 * @param qty
	 *            - amount of tools of type tool to be stored
	 */
	public void addTool(Tool tool, int qty) {

		String type = tool.getType();
		this.toolsAmount.put(type, new AtomicInteger(qty));
		this.toolsDeferres.put(type, new ConcurrentLinkedQueue<Deferred<Tool>>());
		this.toolsObjects.put(type, tool);

	}

	/**
	 * Getter for ManufactoringPlans
	 *
	 * @param product
	 *            - a string with the product name for which a ManufactoringPlan
	 *            is desired
	 * @return A ManufactoringPlan for product
	 */
	public ManufactoringPlan getPlan(String product) {
		return this.plans.get(product);
	}

	/**
	 * Tool return procedure - releases a tool which becomes available in the
	 * warehouse upon completion.
	 *
	 * @param tool
	 *            - The tool to be returned
	 */
	public void releaseTool(Tool tool) {

		synchronized (tool) {

			String toolType = tool.getType();

			if (this.toolsDeferres.get(toolType).size() > 0) {

				Deferred<Tool> d = this.toolsDeferres.get(toolType).poll();
				d.resolve(tool);

			} else {
				this.toolsAmount.get(toolType).incrementAndGet();
			}
		}

	}

}
