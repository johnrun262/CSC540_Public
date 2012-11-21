/*****************************************************************************************
 * 
 * Purchase.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Purchase.java processes purchases for books from a vendor.
 * 
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;



public class Purchase {

	private Connection connection = null;
	private Statement statement = null;
	private ResultSet result = null;

	private static enum PurchaseCmds {CREATE, DELETE, LIST, UPDATE};

	// Constructor
	Purchase(Connection connection){

		this.connection = connection; 

		try {

			// Create a statement instance that will be sending
			// your SQL statements to the DBMS
			this.statement = connection.createStatement();

		} catch(Throwable oops) {
			oops.printStackTrace();
		}
	}



	public int exec(String[] args){

		if (args.length < 2){
			usage();
			return -1;
		}

		try {
			switch (PurchaseCmds.valueOf(args[1].toUpperCase())) {
			case CREATE:
				// create a new purchase from a vendor

				// TODO

				break;

			case DELETE:
				// delete a purchase made from a vendor

				// TODO

				break;

			case LIST:
				// list purchases made from a vendor

				// TODO

				break;

			case UPDATE:
				// Update a purchase made from a vendor

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
		for (PurchaseCmds t : PurchaseCmds.values()) {
			System.out.println(t.toString());
		}
	}

}
