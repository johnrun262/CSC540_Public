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

	// this is the list of commands that can be done to a vendor
	private static enum VendorCmds {ADD, ALL, DELETE, UPDATE, LIST};

	// Constructor
	Vendor(Connection connection){

		// save the connection to the database
		this.connection = connection; 

	}

	/*
	 * Method: exec
	 * 
	 * Execute commands manipulating vendors
	 * 
	 * Input:
	 * args[0] = "vendor"
	 * args[1] = "add" | "all" | "delete" | "update" | "list"
	 * 
	 * Returns:
	 * -1 = unknown report request
	 */

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

			case ALL:
				// Print all vendors in the database

				return (allVendors(args));

			case DELETE:
				// Remove a vendor from the database

				return (deleteVendor(args));

			case UPDATE:
				// Update a vendor already in the database

				return (updateVendor(args));

			case LIST:
				// List information about a vendor already in the the database

				return (listVendor(args));

			} // switch

		} catch (IllegalArgumentException e) {
			usage();
			return -1;
		}

		return 0;

	}

	/*
	 * Method: addVendor
	 * 
	 * Execute the command to create vendors
	 * 
	 * Input:
	 * args[0] = "vendor"
	 * args[1] = "add" 
	 * args[2] = <name>
	 * args[3] = <phone>
	 * args[4] = <address>
	 *
	 * Returns:
	 * -1 = vendor not inserted
	 */
	private int addVendor(String[] args) {
		
		Statement statement = null;
		int newID = 3001;

		// do we have enough parameters to continue?
		if (args.length < 5) {
			System.out.println("Command Missing Parameters - usage: Vendor Add <name> <phone> <address>");
			return -1;
		}

		try {
			// Get the last ID assigned and add one to it to create a new ID for this vendor
			// TODO somehow lock others out of insert to prevent duplicate ID
			String sql = "SELECT MAX(id) AS max FROM Vendor";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			if (result.next()) {
				newID = result.getInt("max");
				newID++;
			}

			// Create and execute the INSERT SQL statement
			// TODO address can be spread over multiple args - may be working with quotes on cmd line
			sql = "INSERT INTO Vendor VALUES ("+ newID +", '"+args[3]+"','"+args[2]+"','"+args[4]+"')";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Vendor was inserted and the ID
			System.out.println("Inserted "+ cnt + " Vendor with ID " + newID + " into Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // addVendor

	/*
	 * Method: allVendors
	 * 
	 * Execute the command to dump vendors
	 * 
	 * Input:
	 * args[0] = "vendor"
	 * args[1] = "all" 
	 *
	 * Returns:
	 * -1 = error processing request
	 */
	private int allVendors(String[] args) {
		
		Statement statement = null;
		int cnt = 0;
		
		try {
			// Select all rows in the vendor table and sort by ID
			String sql = "SELECT * FROM Vendor ORDER BY id";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			// loop through the result set printing attributes
			while (result.next()) {
				cnt++;
				int id = result.getInt("id");
				String name = result.getString("name");
				String phone = result.getString("phone");
				String address = result.getString("address");
				System.out.println(cnt+"\tID: "+id+"\tName: "+name+"\tPhone: "+phone+"\tAddress: "+address);
			}

			System.out.println(cnt+" Row(s) Returned");

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}	

	} // allVendors


	/*
	 * Method: deleteVendor
	 * 
	 * Execute the command to delete a vendor by ID
	 * 
	 * Input:
	 * args[0] = "vendor"
	 * args[1] = "delete" 
	 * args[2] = <id>
	 *
	 * Returns:
	 * -1 = vendor not deleted
	 */
	private int deleteVendor(String[] args) {

		Statement statement = null;
		
		// do we have enough parameters to continue?
		if (args.length < 3) {
			System.out.println("Command Missing Parameters - usage: Vendor Delete <id>");
			return -1;
		}

		try { 

			// Create and execute the DELETE SQL statement
			// TODO validate id numeric
			String sql = "DELETE FROM Vendor WHERE id="+args[2];

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Vendor was inserted and the ID
			System.out.println("Deleted "+ cnt + " Vendor with ID " + args[2] + " from Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // deleteVendor

	/*
	 * Method: listVendor
	 * 
	 * Execute the command to list info about a vendor given ID
	 * 
	 * Input:
	 * args[0] = "vendor"
	 * args[1] = "list"
	 * args[2] = <id> 
	 *
	 * Returns:
	 * -1 = error retrieving vendor
	 */
	private int listVendor(String[] args) {

		Statement statement = null;
		int cnt = 0;
		
		// do we have enough parameters to continue?
		if (args.length < 3) {
			System.out.println("Command Missing Parameters - usage: Vendor List <id>");
			return -1;
		}

		try {
			// Select row in the vendor table with ID
			// TODO is ID numeric?
			String sql = "SELECT * FROM Vendor WHERE id = "+ args[2];

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			// loop through the result set printing attributes
			while (result.next()) {
				cnt++;
				int id = result.getInt("id");
				String name = result.getString("name");
				String phone = result.getString("phone");
				String address = result.getString("address");
				System.out.println("ID: "+id+"\tName: "+name+"\tPhone: "+phone+"\tAddress: "+address);
			}

			System.out.println(cnt+" Row(s) Returned");
			
			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}	

	} // listVendor

	/*
	 * Method: updateVendor
	 * 
	 * Execute the command to update vendor with ID with the given values
	 * 
	 * Input:
	 * args[0] = "vendor"
	 * args[1] = "update"
	 * args[2] = <id> 
	 * args[3] = <name>
	 * args[4] = <phone>
	 * args[5] = <address>
	 *
	 * Returns:
	 * -1 = vendor not updated
	 */
	private int updateVendor(String[] args) {

		Statement statement = null;
		
		// do we have enough parameters to continue?
		if (args.length < 6) {
			System.out.println("Command Missing Parameters - usage: Vendor Update <id> <name> <phone> <address>");
			return -1;
		}

		try {

			// Create and execute the UPDATE SQL statement
			// TODO don't require update of all attributes
			// TODO address may span multiple args
			String sql = "UPDATE Vendor SET name='" + args[3]+"', phone='"+args[4]+"', address='"+args[5]+"' "+
				"WHERE id="+args[2];

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Book was inserted and the ID
			System.out.println("Updated "+ cnt + " Vendor with ID " + args[2] + " in Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // updateVendor
	
	
	private static void usage() {
		System.out.println("Subcommand Required. Legal values:");
		for (VendorCmds t : VendorCmds.values()) {
			System.out.println(t.toString());
		}
	}

}
