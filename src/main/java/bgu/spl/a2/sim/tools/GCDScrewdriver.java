package bgu.spl.a2.sim.tools;

import java.math.BigInteger;

import bgu.spl.a2.sim.Product;

public class GCDScrewdriver implements Tool {

	private final String toolType = "gs-driver";

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

		BigInteger b1 = BigInteger.valueOf(id);
		BigInteger b2 = BigInteger.valueOf(reverse(id));
		long value = (b1.gcd(b2)).longValue();

		return value;
	}

	private long reverse(long num) {

		long reversenum = 0;

		while (num != 0) {
			reversenum = reversenum * 10;
			reversenum = reversenum + num % 10;
			num = num / 10;
		}

		return reversenum;
	}
}
