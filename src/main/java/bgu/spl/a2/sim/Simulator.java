/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	private static Warehouse warehouse;

	/**
	 * attach a WorkStealingThreadPool to the Simulator, this
	 * WorkStealingThreadPool will be used to run the simulation
	 *
	 * @param myWorkStealingThreadPool
	 *            - the WorkStealingThreadPool which will be used by the
	 *            simulator
	 */
	public static void attachWorkStealingThreadPool(WorkStealingThreadPool myWorkStealingThreadPool) {

	}

	public static void main(String[] args) {

		// Gson gson = new Gson();

		warehouse = new Warehouse();
		parseJson();

	}

	/**
	 * Begin the simulation Should not be called before
	 * attachWorkStealingThreadPool()
	 */
	public static ConcurrentLinkedQueue<Product> start() {
		// TODO flag - is attachWorkStealingThreadPool() called
		return null;
	}

	private static void createManufactoringTask(JsonReader jsonReader) throws IOException {

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

		ManufactoringTask task = new ManufactoringTask(startId, product, warehouse);
		// TODO: add to list including count
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

	private static void createPool(JsonReader jsonReader) throws IOException {

		int num = jsonReader.nextInt();
		pool = new WorkStealingThreadPool(num);
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

	private static void createWave(JsonReader jsonReader) throws IOException {

		jsonReader.beginArray();

		while (jsonReader.hasNext()) {
			createManufactoringTask(jsonReader);
		}

		jsonReader.endArray();
	}

	private static void createWaves(JsonReader jsonReader) throws IOException {

		jsonReader.beginArray();

		while (jsonReader.hasNext()) {
			createWave(jsonReader);
		}

		jsonReader.endArray();

	}

	private static void parseJson() {

		try {

			JsonReader jsonReader = new JsonReader(new FileReader("c:\\Temp\\simulation[2].json"));

			jsonReader.beginObject();

			while (jsonReader.hasNext()) {

				String name = jsonReader.nextName();
				if (name.equals("threads")) {

					createPool(jsonReader);
				} else if (name.equals("tools")) {

					createTools(jsonReader);

				} else if (name.equals("plans")) {

					createPlans(jsonReader);

				} else if (name.equals("waves")) {

					createWaves(jsonReader);

				}
			}

			jsonReader.endObject();
			jsonReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
