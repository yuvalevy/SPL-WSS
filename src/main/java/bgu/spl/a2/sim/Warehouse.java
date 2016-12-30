package bgu.spl.a2.sim;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.Tool;

class WarehouseTool {

	private Tool tool;
	private AtomicInteger toolAmount;
	private ConcurrentLinkedQueue<Deferred<Tool>> toolDeferres;

	WarehouseTool(Tool tool, int qty) {

		this.tool = tool;
		this.toolAmount = new AtomicInteger(qty);
		this.toolDeferres = new ConcurrentLinkedQueue<Deferred<Tool>>();

	}

	Tool getTool() {
		return this.tool;
	}

	int getAmount() {
		return this.toolAmount.get();
	}

	void increment() {
		this.toolAmount.incrementAndGet();
	}

	void decrement() {
		this.toolAmount.decrementAndGet();
	}

	void addDeferred(Deferred<Tool> deferred) {
		this.toolDeferres.add(deferred);
	}

	int getDeferredCount() {
		return this.toolDeferres.size();
	}

	Deferred<Tool> pollDeferred() {
		return this.toolDeferres.poll();
	}

}

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
	private HashMap<String, WarehouseTool> tools;

	/**
	 * Constructor
	 */
	public Warehouse() {

		this.tools = new HashMap<String, WarehouseTool>();

		this.plans = new HashMap<String, ManufactoringPlan>();
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

		WarehouseTool warehouseTool = this.tools.get(type);
		Tool tool = warehouseTool.getTool();

		synchronized (tool) {

			Deferred<Tool> deferred = new Deferred<Tool>();

			if (warehouseTool.getAmount() > 0) {

				deferred.resolve(tool);
				warehouseTool.decrement();

			} else {

				warehouseTool.addDeferred(deferred);
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
		WarehouseTool wht = new WarehouseTool(tool, qty);

		this.tools.put(type, wht);
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
			WarehouseTool warehouseTool = this.tools.get(toolType);

			if (warehouseTool.getDeferredCount() > 0) {

				Deferred<Tool> d = warehouseTool.pollDeferred();
				d.resolve(tool);

			} else {
				warehouseTool.increment();
			}
		}

	}

}
