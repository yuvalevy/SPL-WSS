/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import com.google.gson.stream.JsonReader;

import bgu.spl.a2.WorkStealingThreadPool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.GCDScrewdriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;

/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {

	private static WorkStealingThreadPool pool;
	private static Warehouse warehouse = new Warehouse();

	private static ArrayList<ArrayList<ManufactoringTask>> waves = new ArrayList<ArrayList<ManufactoringTask>>();
	private static ArrayList<CountDownLatch> latches = new ArrayList<CountDownLatch>();

	public Simulator(WorkStealingThreadPool pool) {

		Simulator.attachWorkStealingThreadPool(pool);

	}

	/**
	 * attach a WorkStealingThreadPool to the Simulator, this
	 * WorkStealingThreadPool will be used to run the simulation
	 *
	 * @param myWorkStealingThreadPool
	 *            - the WorkStealingThreadPool which will be used by the
	 *            simulator
	 */
	public static void attachWorkStealingThreadPool(WorkStealingThreadPool myWorkStealingThreadPool) {

		pool = myWorkStealingThreadPool;
	}

	public static void main(String[] args) {

		int threads = parseJson(args[0]);

		WorkStealingThreadPool pool = new WorkStealingThreadPool(threads);
		Simulator.attachWorkStealingThreadPool(pool);
		ConcurrentLinkedQueue<Product> queue = Simulator.start();

		try {
			pool.shutdown();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		try {
			FileOutputStream fout = new FileOutputStream("result.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(queue);
			oos.close();
			fout.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Begin the simulation Should not be called before
	 * attachWorkStealingThreadPool()
	 */
	public static ConcurrentLinkedQueue<Product> start() {

		ConcurrentLinkedQueue<Product> mainProducts = new ConcurrentLinkedQueue<Product>();

		if (pool == null) {
			return null;
		}

		pool.start();

		for (int i = 0; i < waves.size(); i++) {

			for (ManufactoringTask task : waves.get(i)) {

				pool.submit(task);
			}

			try {
				latches.get(i).await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (ManufactoringTask task : waves.get(i)) {

				mainProducts.add(task.getResult().get());
			}

		}
		return mainProducts;
	}

	private static void createManufactoringTask(ArrayList<ManufactoringTask> list, JsonReader jsonReader)
			throws IOException {

		jsonReader.beginObject();

		long startId = 0;
		String product = null;
		long qty = 0;

		while (jsonReader.hasNext()) {

			String nextName = jsonReader.nextName();

			if (nextName.equals("product")) {
				product = jsonReader.nextString();
			} else if (nextName.equals("qty")) {
				qty = jsonReader.nextLong();
			} else if (nextName.equals("startId")) {
				startId = jsonReader.nextLong();
			}
		}

		for (int i = 0; i < qty; i++) {
			list.add(new ManufactoringTask(startId + i, product, warehouse));
		}

		jsonReader.endObject();
	}

	private static void createPlan(JsonReader jsonReader) throws IOException {

		jsonReader.beginObject();

		String product = null;
		String[] parts = null;
		String[] tools = null;

		while (jsonReader.hasNext()) {

			String nextName = jsonReader.nextName();

			if (nextName.equals("product")) {

				product = jsonReader.nextString();

			} else if (nextName.equals("tools")) {

				tools = createStringArray(jsonReader);

			} else if (nextName.equals("parts")) {

				parts = createStringArray(jsonReader);

			}
		}

		ManufactoringPlan plan = new ManufactoringPlan(product, parts, tools);
		warehouse.addPlan(plan);

		jsonReader.endObject();
	}

	private static void createPlans(JsonReader jsonReader) throws IOException {

		jsonReader.beginArray();

		while (jsonReader.hasNext()) {
			createPlan(jsonReader);
		}

		jsonReader.endArray();

	}

	private static String[] createStringArray(JsonReader jsonReader) throws IOException {

		jsonReader.beginArray();

		ArrayList<String> array = new ArrayList<String>();
		while (jsonReader.hasNext()) {
			array.add(jsonReader.nextString());
		}

		jsonReader.endArray();
		return array.toArray(new String[array.size()]);
	}

	private static void createTool(JsonReader jsonReader) throws IOException {

		jsonReader.beginObject();

		String type = "";
		int qty = 0;

		while (jsonReader.hasNext()) {

			String nextName = jsonReader.nextName();

			if (nextName.equals("tool")) {
				type = jsonReader.nextString();
			} else if (nextName.equals("qty")) {
				qty = jsonReader.nextInt();
			}
		}

		char begin = type.charAt(0);
		Tool tool = null;

		switch (begin) {
		case 'g': // gs-driver
			tool = new GCDScrewdriver();
			break;

		case 'n': // np-hammer
			tool = new NextPrimeHammer();
			break;

		case 'r': // rs-pliers
			tool = new RandomSumPliers();
			break;

		}

		jsonReader.endObject();

		warehouse.addTool(tool, qty);

	}

	private static void createTools(JsonReader jsonReader) throws IOException {

		jsonReader.beginArray();

		while (jsonReader.hasNext()) {
			createTool(jsonReader);
		}

		jsonReader.endArray();
	}

	private static void createWave(ArrayList<ManufactoringTask> list, JsonReader jsonReader) throws IOException {

		jsonReader.beginArray();

		while (jsonReader.hasNext()) {
			createManufactoringTask(list, jsonReader);
		}

		jsonReader.endArray();
	}

	private static void createWaves(JsonReader jsonReader) throws IOException {

		jsonReader.beginArray();

		while (jsonReader.hasNext()) {

			ArrayList<ManufactoringTask> list = new ArrayList<ManufactoringTask>();
			createWave(list, jsonReader);

			CountDownLatch latch = new CountDownLatch(list.size());

			whenResolve(latch, list);

			waves.add(list);
			latches.add(latch);
		}

		jsonReader.endArray();

	}

	private static int parseJson(String jsonParse) {

		int threads = 0;

		try (JsonReader jsonReader = new JsonReader(new FileReader(jsonParse))) {

			jsonReader.beginObject();

			while (jsonReader.hasNext()) {

				String name = jsonReader.nextName();

				if (name.equals("threads")) {

					threads = jsonReader.nextInt();

				} else if (name.equals("tools")) {

					createTools(jsonReader);

				} else if (name.equals("plans")) {

					createPlans(jsonReader);

				} else if (name.equals("waves")) {

					createWaves(jsonReader);

				}
			}

			jsonReader.endObject();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return threads;
	}

	private static void whenResolve(CountDownLatch latch, ArrayList<ManufactoringTask> list) {

		for (ManufactoringTask task : list) {
			task.getResult().whenResolved(() -> {

				latch.countDown();

			});
		}

	}
}
