package bgu.spl.a2.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.Task;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.Tool;

class ManufactoringTask extends Task<Product> {

	private String sproduct;
	private Warehouse warehouse;
	private long startId;
	private AtomicLong newId;

	void incId(long add) {
		newId.addAndGet(add);
	}

	public ManufactoringTask(long startId, String product, Warehouse warehouse) {
		this.warehouse = warehouse;
		this.sproduct = product;
		this.startId = startId;
		this.newId = new AtomicLong(0);
	}

	@Override
	protected void start() {

		ManufactoringPlan plan = warehouse.getPlan(sproduct);
		String[] parts = plan.getParts();

		List<ManufactoringTask> tasks = new ArrayList<ManufactoringTask>();

		for (int i = 0; i < parts.length; i++) {
			tasks.add(new ManufactoringTask(startId + 1, parts[i], warehouse));
		}

		spawn(tasks.toArray(new ManufactoringTask[parts.length]));

		// When all parts are manufactured, this callback is called
		whenResolved(tasks, () -> {

			Product product = new Product(startId, sproduct);

			for (ManufactoringTask task : tasks) {

				Product pro = task.getResult().get();
				product.addPart(pro);
			}

			for (String stool : plan.getTools()) {

				Deferred<Tool> acq = warehouse.acquireTool(stool);
				// When this tool is acquired, this callback is called
				acq.whenResolved(() -> {

					Tool tool = acq.get();

					incId(tool.useOn(product));

					warehouse.releaseTool(tool);

				});
			}

			product.setFinalId(this.newId.get() + this.startId);
			complete(product);

		});
	}
}
