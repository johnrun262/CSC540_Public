/*****************************************************************************************
 * 
 * Customer.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Customer.java maintains information about customers.
 * 
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;



public class Customer {

	private Connection connection = null;


	// this is the list of commands that can be done to a customer
	private static enum CustomerCmds {ADD, ALL, DELETE, UPDATE, LIST};

	// Constructor
	Customer(Connection connection){

		this.connection = connection; 

	}



	public int exec(String[] args){

		if (args.length < 2){
			usage();
			return -1;
		}

		try {
			switch (CustomerCmds.valueOf(args[1].toUpperCase())) {
			case ADD:
				// Add a new customer to the database

				return(addCustomer(args));

			case ALL:
				// List all Customers in the database

				return(allCustomers(args));

			case DELETE:
				// Remove a customer from the database

				return(deleteCustomer(args));

			case UPDATE:
				// Update a customer already in the database

				return(updateCustomer(args));

			case LIST:
				// List information about a customer already in the the database

				return(listCustomer(args));
				
			} // switch

		} catch (IllegalArgumentException e) {
			usage();
			return -1;
		}

		return 0;
		
	}

	/*
	 * Method: addCustomer
	 * 
	 * Execute the command to create customers. The customer 
	 * is inserted with an "active" status.
	 * 
	 * Input:
	 * args[0] = "customer"
	 * args[1] = "add" 
	 * args[2] = <name>
	 * args[3] = <phone>
	 * args[4] = <address>
	 * args[5] = <dob>
	 * args[6] = <gender>
	 * args[7] = <ssn>
	 *
	 * Returns:
	 * -1 = vendor not inserted
	 */
	private int addCustomer(String[] args) {
		
		Statement statement = null;
		int newID = 2001;
		String gender = "";
		String ssn = "";

		// do we have enough parameters to continue? note that ssn is optional
		if (args.length < 7) {
			System.out.println("Command Missing Parameters - usage: Customer Add <name> <phone> <address> <dob> <gender> [ssn]");
			return -1;
		}

		// validate input parameters
		try {
			// gender should be F or M
			if (args[6].equalsIgnoreCase("F") || args[6].equalsIgnoreCase("M")) {
				gender = args[6].toUpperCase();
			} else {
				System.out.println("Gender must equal F or M: " + args[6]);
				return -1;
			}
			
			// validate date of birth
			try {
				SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
				format.parse(args[5]);
				// TODO check year is say >1900
			} catch (Exception e) {
				System.out.println("Invalid Format Date of Birth: expecting dd-MMM-yyyy (ex 12-dec-1960) found "+args[5]);
				return -1;
			}
			
			// was the ssn supplied?
			if (args.length < 8) {
				// TODO handle no ssn
				ssn = "999-99-9999";
			} else {
				// TODO is valid format ssn?
				ssn = args[7];
			}
			
			// TODO schema has max phone length at 12 - this may noy be big enough (i.e. "(919) 123-1234" is 14)
			
			// TODO Gender, DOB, Phone, SSN, ADDRESS are all optional
			
		} catch (Exception e) {
			System.out.println("Invalid Format Parameter");
			return -1;
		}
		
		try {
			// Get the last ID assigned and add one to it to create a new ID for this vendor
			// TODO somehow lock others out of insert to prevent duplicate ID
			String sql = "SELECT MAX(id) AS max FROM Customer";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			if (result.next()) {
				newID = result.getInt("max");
				newID++;
			}

			// Create and execute the INSERT SQL statement
			// TODO address can be spread over multiple args - may be working with quotes on cmd line
			sql = "INSERT INTO Customer VALUES ("+newID+", '"+gender+"','"+args[5]+"', 'active', '"+args[3]+"','"+ssn+"', '"+args[2]+"', '"+args[4]+"')";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Customer was inserted and the ID
			System.out.println("Inserted "+ cnt + " Customer with ID " + newID + " into Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // addCustomer

	/*
	 * Method: allCustomers
	 * 
	 * Execute the command to dump Customers
	 * 
	 * Input:
	 * args[0] = "customer"
	 * args[1] = "all" 
	 *
	 * Returns:
	 * -1 = error processing request
	 */
	private int allCustomers(String[] args) {
		
		Statement statement = null;
		int cnt = 0;
		
		try {
			// Select all rows in the Customer table and sort by ID
			String sql = "SELECT * FROM Customer ORDER BY id";

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
				String gender = result.getString("gender");
				Date dob = result.getDate("dob");
				String status = result.getString("status");
				String ssn = result.getString("ssn");
				// TODO what happens if no gender, dob, phone, address, SSN?
				System.out.println(cnt+"\tID: "+id+"\tName: "+name+"\tPhone: "+phone+"\tAddress: "+address+"\tDOB: "+dob+"\tGender: "+gender+"\tSSN: "+ssn+"\tStatus: "+status);
			}

			System.out.println(cnt+" Row(s) Returned");

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}	

	} // allCustomers


	/*
	 * Method: deleteCustomer
	 * 
	 * Execute the command to delete a Customer by ID
	 * 
	 * Input:
	 * args[0] = "customer"
	 * args[1] = "delete" 
	 * args[2] = <id>
	 *
	 * Returns:
	 * -1 = customer not deleted
	 */
	private int deleteCustomer(String[] args) {

		Statement statement = null;
		
		// do we have enough parameters to continue?
		if (args.length < 3) {
			System.out.println("Command Missing Parameters - usage: Customer Delete <id>");
			return -1;
		}

		try { 

			// Create and execute the DELETE SQL statement
			// TODO validate id numeric
			String sql = "DELETE FROM Customer WHERE id="+args[2];

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Vendor was inserted and the ID
			System.out.println("Deleted "+ cnt + " Customer with ID " + args[2] + " from Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // deleteCustomer

	/*
	 * Method: listCustomer
	 * 
	 * Execute the command to list info about a Customer given ID
	 * 
	 * Input:
	 * args[0] = "customer"
	 * args[1] = "list"
	 * args[2] = <id> 
	 *
	 * Returns:
	 * -1 = error retrieving customer
	 */
	private int listCustomer(String[] args) {

		Statement statement = null;
		int cnt = 0;
		
		// do we have enough parameters to continue?
		if (args.length < 3) {
			System.out.println("Command Missing Parameters - usage: Customer List <id>");
			return -1;
		}

		try {
			// Select row in the vendor table with ID
			// TODO is ID numeric?
			String sql = "SELECT * FROM Customer WHERE id = "+ args[2];

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
				String gender = result.getString("gender");
				Date dob = result.getDate("dob");
				String status = result.getString("status");
				String ssn = result.getString("ssn");
				// TODO what happens if no gender, dob, phone, address, SSN?
				System.out.println("ID: "+id+"\tName: "+name+"\tPhone: "+phone+"\tAddress: "+address+"\tDOB: "+dob+"\tGender: "+gender+"\tSSN: "+ssn+"\tStatus: "+status);
			}

			System.out.println(cnt+" Row(s) Returned");
			
			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}	

	} // listCustomer

	/*
	 * Method: updateCustomer
	 * 
	 * Execute the command to update customer with ID with the given values
	 * 
	 * Input:
	 * args[0] = "vendor"
	 * args[1] = "update"
	 * args[2] = <id> 
	 * args[3] = <name>
	 * args[4] = <phone>
	 * args[5] = <address>
	 * args[6] = <dob>
	 * args[7] = <gender>
	 * args[8] = <ssn>
	 *
	 * Returns:
	 * -1 = vendor not updated
	 */
	private int updateCustomer(String[] args) {

		Statement statement = null;
		String gender = "";
		String ssn = "";
		
		// do we have enough parameters to continue?
		if (args.length < 8) {
			System.out.println("Command Missing Parameters - usage: Customer Update <id> <name> <phone> <address> <dob> <gender> [ssn]");
			return -1;
		}

		// validate input parameters
		try {
			// gender should be F or M
			if (args[7].equalsIgnoreCase("F") || args[7].equalsIgnoreCase("M")) {
				gender = args[7].toUpperCase();
			} else {
				System.out.println("Gender must equal F or M: " + args[7]);
				return -1;
			}
			
			// validate date of birth
			try {
				SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
				format.parse(args[6]);
				// TODO check year is say >1900
			} catch (Exception e) {
				System.out.println("Invalid Format Date of Birth: expecting dd-MMM-yyyy (ex 12-dec-1960) found "+args[6]);
				return -1;
			}
			
			// was the ssn supplied?
			if (args.length < 9) {
				// TODO handle no ssn
				ssn = "999-99-9999";
			} else {
				// TODO is valid format ssn?
				ssn = args[8];
			}
			
			// TODO schema has max phone length at 12 - this may noy be big enough (i.e. "(919) 123-1234" is 14)
			
			// TODO Gender, DOB, Phone, SSN, ADDRESS are all optional
			
		} catch (Exception e) {
			System.out.println("Invalid Format Parameter");
			return -1;
		}
		
		try {

			// Create and execute the UPDATE SQL statement
			// TODO don't require update of all attributes
			// TODO address may span multiple args
			String sql = "UPDATE Customer SET name='" + args[3]+"', phone='"+args[4]+"', address='"+args[5]+"', dob='"+args[6]+"', gender='"+gender+"', ssn='"+ssn+"' "+
				"WHERE id="+args[2];

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Book was inserted and the ID
			System.out.println("Updated "+ cnt + " Customer(s) with ID " + args[2] + " in Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // updateVendor
	
	private static void usage() {
		System.out.println("Subcommand Required. Legal values:");
		for (CustomerCmds t : CustomerCmds.values()) {
			System.out.println(t.toString());
		}
	}

}
