/*****************************************************************************************
 * 
 * Sale.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Sale.java manages sales to a customer
 * 
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;



public class Sale {

	private Connection connection = null;

	// this is the list of commands that can be done to a sale
	private static enum SaleCmds {ADD, DELETE, LIST, UPDATE};

	// Constructor
	Sale(Connection connection){

		this.connection = connection; 

	}


	public int exec(String[] args){

		if (args.length < 2){
			usage();
			return -1;
		}

		try {
			switch (SaleCmds.valueOf(args[1].toUpperCase())) {
			case ADD:
				// create a new sale to a customer

				// TODO

				break;

			case DELETE:
				// delete a sale

				// TODO

				break;

			case LIST:
				// list a sale to a customer

				// TODO

				break;

			case UPDATE:
				// Update a sale to a customer

				// TODO

				break;

			} // switch

		} catch (IllegalArgumentException e) {
			usage();
			return -1;
		}

		return 0;
		
	}

	private static void usage() {
		System.out.println("Subcommand Required. Legal values:");
		for (SaleCmds t : SaleCmds.values()) {
			System.out.println(t.toString());
		}
	}

}
