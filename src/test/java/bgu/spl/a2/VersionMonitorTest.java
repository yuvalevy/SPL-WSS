package bgu.spl.a2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VersionMonitorTest {

	VersionMonitor vm;

	@Before
	public void setUp() throws Exception {
		this.vm = new VersionMonitor();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAwait() {

		Thread waiter = new Thread(() -> {
			try {
				this.vm.await(0);
				this.vm.inc();
			} catch (Exception e) {
				assertTrue(false);
			}
		});

		waiter.start();

		this.vm.inc();

		int expected = 2;

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		int actual = this.vm.getVersion();

		assertEquals(expected, actual);

	}

	@Test
	public void testAwaitRightAway() {

		try {
			this.vm.await(4);
		} catch (InterruptedException e) {
			assertFalse(true);
		}
		assertTrue(true);

	}

	@Test
	public void testGetVersion() {

		int expected = 0;
		int actual = this.vm.getVersion();

		assertEquals(expected, actual);
	}

	@Test
	public void testInc() {

		int expected = 1;

		this.vm.inc();

		int actual = this.vm.getVersion();

		assertEquals(expected, actual);

	}

	@Test
	public void testIncMany() {

		int expected = 0;
		int actual = 0;
		for (int i = 0; i < 10; i++) {

			this.vm.inc();
			expected++;
			actual = this.vm.getVersion();

			assertEquals(expected, actual);

		}

	}

}
