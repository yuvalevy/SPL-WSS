/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;

import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.a2.WorkStealingThreadPool;

/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {

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
	}

	/**
	 * Begin the simulation Should not be called before
	 * attachWorkStealingThreadPool()
	 */
	public static ConcurrentLinkedQueue<Product> start() {
		// TODO flag - is attachWorkStealingThreadPool() called
		return null;
	}
}
