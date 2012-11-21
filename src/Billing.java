/*****************************************************************************************
 * 
 * Billing.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Billing.java creates payments to a vendor and bills for a customer.
 * 
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;



public class Billing {

	private Connection connection = null;
	private Statement statement = null;
	private ResultSet result = null;

	private static enum BillingCmds {CUSTOMER, VENDOR};

	// Constructor
	Billing(Connection connection){

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
			switch (BillingCmds.valueOf(args[1].toUpperCase())) {
			case CUSTOMER:
				// Creating a Customer Bill

				// TODO

				break;

			case VENDOR:
				// Creating a Vendor Payment

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
		for (BillingCmds t : BillingCmds.values()) {
			System.out.println(t.toString());
		}
	}

}
