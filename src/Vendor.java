/*****************************************************************************************
 * 
 * Vendor.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Vendor.java maintains information about vendors.
 * 
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;



public class Vendor {

	private Connection connection = null;
	private Statement statement = null;
	private ResultSet result = null;

	private static enum VendorCmds {ADD, DELETE, UPDATE, LIST};

	// Constructor
	Vendor(Connection connection){

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
			switch (VendorCmds.valueOf(args[1].toUpperCase())) {
			case ADD:
				// Add a new vendor to the database

				return (addVendor(args));

				break;

			case DELETE:
				// Remove a vendor from the database

				// TODO

				break;

			case UPDATE:
				// Update a vendor already in the database

				// TODO

				break;

			case LIST:
				// List information about a vendor already in the the database

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
		for (VendorCmds t : VendorCmds.values()) {
			System.out.println(t.toString());
		}
	}

}
