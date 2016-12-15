package bgu.spl.a2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeferredTest {

	Deferred<Integer> def;
	Integer result1, result2;

	@Before
	public void setUp() throws Exception {
		this.def = new Deferred<Integer>();
		this.result1 = -1;
		this.result2 = -1;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGet() {

		try {
			this.def.get();
		} catch (IllegalStateException e) {

		} catch (Exception e) {
			fail("Worng exception");
		}

	}

	@Test
	public void testIsResolved() {

		boolean expected = false;
		boolean actual = this.def.isResolved();

		assertEquals(expected, actual);

	}

	@Test
	public void testResolve() {

		Integer iExpected = 5;
		boolean bExpected = true;

		this.def.resolve(iExpected);

		Integer iActual = this.def.get();
		boolean bActual = this.def.isResolved();

		assertEquals(iExpected, iActual);
		assertEquals(bExpected, bActual);

	}

	@Test
	public void testResolveTwice() {

		this.def.resolve(5);

		try {
			this.def.resolve(5);
		} catch (IllegalStateException e) {

		} catch (Exception e) {
			fail("Worng exception");
		}

	}

	@Test
	public void testWhenResolved() {

		Integer iExpected = 10;
		Integer resultExpected = 11;
		boolean bExpected = true;

		this.def.whenResolved(() -> {

			this.result1 = resultExpected;

		});

		this.def.resolve(iExpected);

		Integer iActual = this.def.get();
		Integer resultActual = this.result1;
		boolean bActual = this.def.isResolved();

		assertEquals(resultExpected, resultActual);
		assertEquals(iExpected, iActual);
		assertEquals(bExpected, bActual);

	}

	@Test
	public void testWhenResolvedAfterResolved() {

		Integer iExpected = 10;
		Integer resultExpected = 11;
		boolean bExpected = true;

		this.def.resolve(iExpected);

		this.def.whenResolved(() -> {

			this.result1 = resultExpected;

		});

		Integer iActual = this.def.get();
		Integer resultActual = this.result1;
		boolean bActual = this.def.isResolved();

		assertEquals(resultExpected, resultActual);
		assertEquals(iExpected, iActual);
		assertEquals(bExpected, bActual);

	}

	@Test
	public void testWhenResolvedNoResult() {

		Integer resultExpected = -1;
		boolean bExpected = false;

		this.def.whenResolved(() -> {

			this.result1++;

		});

		this.def.whenResolved(() -> {

			this.result1++;

		});

		try {
			this.def.get();
		} catch (IllegalStateException e) {

		} catch (Exception e) {
			fail("Worng exception");
		}

		Integer result1Actual = this.result1;

		boolean bActual = this.def.isResolved();

		assertEquals(resultExpected, result1Actual);
		assertEquals(bExpected, bActual);

	}

	@Test
	public void testWhenResolvedNullSubscriber() {

		Integer iExpected = 10;
		boolean bExpected = true;

		this.def.whenResolved(null);

		this.def.resolve(iExpected);

		Integer iActual = this.def.get();
		boolean bActual = this.def.isResolved();

		assertEquals(iExpected, iActual);
		assertEquals(bExpected, bActual);

	}

	@Test
	public void testWhenResolvedTwoSimilarSubscribes() {

		Integer iExpected = 10;
		Integer resultExpected = 1;
		boolean bExpected = true;

		this.def.whenResolved(() -> {

			this.result1++;

		});

		this.def.whenResolved(() -> {

			this.result1++;

		});

		this.def.resolve(iExpected);

		Integer iActual = this.def.get();
		Integer result1Actual = this.result1;
		boolean bActual = this.def.isResolved();

		assertEquals(resultExpected, result1Actual);
		assertEquals(iExpected, iActual);
		assertEquals(bExpected, bActual);

	}

	@Test
	public void testWhenResolvedTwoSubscribes() {

		Integer iExpected = 10;
		Integer resultExpected = 12;
		boolean bExpected = true;

		this.def.whenResolved(() -> {

			this.result1 = resultExpected;

		});

		this.def.whenResolved(() -> {

			this.result2 = resultExpected;

		});

		this.def.resolve(iExpected);

		Integer iActual = this.def.get();
		Integer result1Actual = this.result1;
		Integer result2Actual = this.result2;
		boolean bActual = this.def.isResolved();

		assertEquals(resultExpected, result1Actual);
		assertEquals(resultExpected, result2Actual);
		assertEquals(iExpected, iActual);
		assertEquals(bExpected, bActual);

	}
}
