package bgu.spl.a2.sim.tools;

import java.util.Random;

import bgu.spl.a2.sim.Product;

public class RandomSumPliers implements Tool {

	private final String toolType = "rs-pliers";

	@Override
	public String getType() {
		return toolType;
	}

	@Override
	public long useOn(Product p) {

		long value = 0;
		for (Product part : p.getParts()) {
			value += Math.abs(act(part.getFinalId()));

		}
		return value;
	}

	private long act(long id) {
		Random r = new Random(id);
		long sum = 0;
		for (long i = 0; i < id % 10000; i++) {
			sum += r.nextInt();
		}

		return sum;
	}

}
