package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.a2.VersionMonitor;

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
	public void testGetVersion() {
		
		int expected = 0;
		int actual = vm.getVersion();

		assertEquals(expected, actual);
	}

	@Test
	public void testInc() {
		
		int expected = 1;
		
		vm.inc();
		
		int actual = vm.getVersion();

		assertEquals(expected, actual);
		
	}

	@Test
	public void testIncMany() {
		
		int expected = 0;
		int actual = 0 ;
		for (int i = 0; i < 10; i++) {
			
			vm.inc();
			expected++;
			actual = vm.getVersion();

			assertEquals(expected, actual);
			
		}
		
	}
	
	@Test
	public void testAwaitRightAway() {

		try {
			vm.await(4);
		} catch (InterruptedException e) {
			assertFalse(true);
		}
		assertTrue(true);

	}

	@Test
	public void testAwait() {

		
		Thread waiter = new Thread(()->{
			try {
				this.vm.await(0);
				vm.inc();
			} catch (Exception e) {
				assertTrue(false);
			}
		});
		
		waiter.start();

		vm.inc();

		int expected = 2;
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int actual = vm.getVersion();
		
		assertEquals(expected, actual);
		
	}
	
}
