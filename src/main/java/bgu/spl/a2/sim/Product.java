package bgu.spl.a2.sim;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a product produced during the simulation.
 */
public class Product {

	private long finalId;
	List<Product> products;
	private String name;
	private long startId;

	/**
	 * Constructor
	 *
	 * @param startId
	 *            - Product start id
	 * @param name
	 *            - Product name
	 */
	public Product(long startId, String name) {

		this.name = name;
		this.startId = startId;
		this.finalId = startId;
		this.products = new ArrayList<Product>();
	}

	/**
	 * Add a new part to the product
	 *
	 * @param p
	 *            - part to be added as a Product object
	 */
	public void addPart(Product p) {
		this.products.add(p);
	}

	/**
	 * @return The product final ID as a long. final ID is the ID the product
	 *         received as the sum of all UseOn();
	 */
	public long getFinalId() {
		return finalId;
	}

	/**
	 * Sets the final id
	 * 
	 * @param id
	 *            the new id
	 */
	public void setFinalId(long id) {
		this.finalId = id;
	}

	/**
	 * @return The product name as a string
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return Returns all parts of this product as a List of Products
	 */
	public List<Product> getParts() {
		return this.products;
	}

	/**
	 * @return The product start ID as a long. start ID should never be changed.
	 */
	public long getStartId() {
		return this.startId;
	}

}
