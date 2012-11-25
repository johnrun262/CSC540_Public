
/*****************************************************************************************
 * 
 * Staff.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Staff.java maintains information about staff.
 * 
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;



public class Staff {

	private Connection connection = null;

	// this is the list of commands that can be done to a Staff
	private static enum StaffCmds {ADD, ALL, DELETE, UPDATE, LIST};

	// Constructor
	Staff(Connection connection){

		this.connection = connection; 

	}


	public int exec(String[] args){

		if (args.length < 2){
			usage();
			return -1;
		}

		try {
			switch (StaffCmds.valueOf(args[1].toUpperCase())) {
			case ADD:
				// Add a new staff member to the database

				return(addStaff(args));

			case ALL:
				// List all Staff members in the database

				return(allStaff(args));

			case DELETE:
				// Remove a staff member from the database

				return(deleteStaff(args));

			case UPDATE:
				// Update a staff member already in the database

				return(updateStaff(args));

			case LIST:
				// List information about a staff member already in the the database

				return(listStaff(args));

			} // switch

		} catch (IllegalArgumentException e) {
			usage();
			return -1;
		}

		return 0;

	}


	/*
	 * Method: addStaff
	 * 
	 * Execute the command to create staff record. 
	 * 
	 * Input:
	 * args[0] = "staff"
	 * args[1] = "add" 
	 * args[2] = <name>
	 * args[3] = <phone>
	 * args[4] = <address>
	 * args[5] = <dob>
	 * args[6] = <gender>
	 * args[7] = <job title>
	 * args[8] = <department>
	 * args[9] = <salary>
	 * args[10] = <work location>
	 *
	 * Returns:
	 * -1 = staff member not inserted
	 */
	private int addStaff(String[] args) {

		Statement statement = null;
		int newID = 1001;

		// do we have enough parameters to continue? 
		// TODO does DOS bat script pass all 11?
		if (args.length < 11) {
			System.out.println("Command Missing Parameters - usage: Staff Add <name> <phone> <address> <dob> <gender> <job title> <dept> <salary> <work location>");
			return -1;
		}

		String name = args[2];
		String phone = args[3];
		String address = args[4];
		String dob = args[5];
		String gender = args[6];
		String jobTitle = args[7];
		String dept = args[8];
		String salary = args[9];
		String workLoc = args[10];

		// validate input parameters
		try {

			// gender should be F or M
			if (gender.equalsIgnoreCase("F") || gender.equalsIgnoreCase("M")) {
				gender = gender.toUpperCase();
			} else {
				System.out.println("Gender must equal F or M: " + gender);
				return -1;
			}

			// validate date of birth
			try {
				SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
				format.parse(dob);
				// TODO check year is say >1900
			} catch (Exception e) {
				System.out.println("Invalid Format Date of Birth: expecting dd-MMM-yyyy (ex 12-dec-1960) found "+dob);
				return -1;
			}

			// The following are enforced by the schema but can also be checked here to give the user a better error message

			// check salary numeric and > 0
			if (Integer.parseInt(salary) <= 0) {
				System.out.println("Salary must be greater than zero");
				return -1;
			}

			// jobTitle must be Salesperson, Procurement, Accounting, Management
			if ((jobTitle = checkTitle(jobTitle)) == null) {
				return -1;
			}

			// department must be Sales, Procurement, Accounting, Warehouse, Management, Accounting
			if ((dept = checkDept(dept)) == null) {
				return -1;
			}
			
			// Work Location must be "Southpoint, Northgate, Airport Mall, Concord Mills, Jungle Jims, warehouse, HQ
			if ((workLoc = checkLoc(workLoc)) == null) {
				return -1;
			}
			
			// TODO schema has max phone length at 12 - this may noy be big enough (i.e. "(919) 123-1234" is 14)

		} catch (Exception e) {
			System.out.println("Invalid Format Parameter");
			return -1;
		}

		try {
			// Get the last ID assigned and add one to it to create a new ID for this vendor
			// TODO somehow lock others out of insert to prevent duplicate ID
			String sql = "SELECT MAX(id) AS max FROM Staff";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			if (result.next()) {
				newID = result.getInt("max");
				newID++;
			}

			// Create and execute the INSERT SQL statement
			// TODO address can be spread over multiple args - may be working with quotes on cmd line
			sql = "INSERT INTO Staff VALUES ("+newID+", '"+name+"', '"+gender+"', '"+dob+"', '"+jobTitle+"', '"+dept+"', '"+salary+"', '"+phone+"', '"+address+"', '"+workLoc+"')";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Customer was inserted and the ID
			System.out.println("Inserted "+ cnt + " Staff with ID " + newID + " into Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // addStaff

	/*
	 * Method: allStaff
	 * 
	 * Execute the command to dump all Staff members
	 * 
	 * Input:
	 * args[0] = "staff"
	 * args[1] = "all" 
	 *
	 * Returns:
	 * -1 = error processing request
	 */
	private int allStaff(String[] args) {

		Statement statement = null;
		int cnt = 0;

		try {
			// Select all rows in the Customer table and sort by ID
			String sql = "SELECT * FROM Staff ORDER BY id";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			// loop through the result set printing attributes
			while (result.next()) {
				cnt++;
				int id = result.getInt("id");
				String name = result.getString("name");
				String gender = result.getString("gender");
				Date dob = result.getDate("dob");
				String jobTitle = result.getString("jobTitle");
				String dept = result.getString("department");
				int salary = result.getInt("salary");
				String phone = result.getString("phone");
				String address = result.getString("address");
				String workLoc = result.getString("workLocation");
				System.out.println(cnt+"\tID: "+id+"\tName: "+name+"\tPhone: "+phone+"\tAddress: "+address+"\tDOB: "+dob+"\tGender: "+gender+"\tTitle: "+jobTitle+"\tDept: "+dept+"\tLocation: "+workLoc+"\tSalary: "+salary);
			}

			System.out.println(cnt+" Row(s) Returned");

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}	

	} // allStaff

	/*
	 * Method: deleteStaff
	 * 
	 * Execute the command to delete a Staff Member by ID
	 * 
	 * Input:
	 * args[0] = "staff"
	 * args[1] = "delete" 
	 * args[2] = <id>
	 *
	 * Returns:
	 * -1 = staff record not deleted
	 */
	private int deleteStaff(String[] args) {

		Statement statement = null;

		// do we have enough parameters to continue?
		if (args.length < 3) {
			System.out.println("Command Missing Parameters - usage: Staff Delete <id>");
			return -1;
		}

		try { 

			// Create and execute the DELETE SQL statement
			// TODO validate id numeric
			String sql = "DELETE FROM Staff WHERE id="+args[2];

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Vendor was inserted and the ID
			System.out.println("Deleted "+ cnt + " Staff Member with ID " + args[2] + " from Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // deleteStaff

	/*
	 * Method: listStaff
	 * 
	 * Execute the command to list info about a Staff Member given ID
	 * 
	 * Input:
	 * args[0] = "staff"
	 * args[1] = "list"
	 * args[2] = <id> 
	 *
	 * Returns:
	 * -1 = error retrieving staff
	 */
	private int listStaff(String[] args) {

		Statement statement = null;
		int cnt = 0;

		// do we have enough parameters to continue?
		if (args.length < 3) {
			System.out.println("Command Missing Parameters - usage: Staff List <id>");
			return -1;
		}

		try {
			// Select row in the vendor table with ID
			// TODO is ID numeric?
			String sql = "SELECT * FROM Staff WHERE id = "+ args[2];

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			// loop through the result set printing attributes
			while (result.next()) {
				cnt++;
				int id = result.getInt("id");
				String name = result.getString("name");
				String gender = result.getString("gender");
				Date dob = result.getDate("dob");
				String jobTitle = result.getString("jobTitle");
				String dept = result.getString("department");
				int salary = result.getInt("salary");
				String phone = result.getString("phone");
				String address = result.getString("address");
				String workLoc = result.getString("workLocation");
				System.out.println(cnt+"\tID: "+id+"\tName: "+name+"\tPhone: "+phone+"\tAddress: "+address+"\tDOB: "+dob+"\tGender: "+gender+"\tTitle: "+jobTitle+"\tDept: "+dept+"\tLocation: "+workLoc+"\tSalary: "+salary);
			}

			System.out.println(cnt+" Row(s) Returned");

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}	

	} // listStaff

	/*
	 * Method: updateStaff
	 * 
	 * Execute the command to update Staff Member with ID with the given values
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
	 * args[8] = <job title>
	 * args[9] = <department>
	 * args[10] = <salary>
	 * args[11] = <work location>
	 *
	 * Returns:
	 * -1 = staff not updated
	 */
	private int updateStaff(String[] args) {

		Statement statement = null;

		// do we have enough parameters to continue? 
		// TODO does DOS bat script pass all 11?
		if (args.length < 12) {
			System.out.println("Command Missing Parameters - usage: Staff Update <id> <name> <phone> <address> <dob> <gender> <job title> <dept> <salary> <work location>");
			return -1;
		}

		String name = args[3];
		String phone = args[4];
		String address = args[5];
		String dob = args[6];
		String gender = args[7];
		String jobTitle = args[8];
		String dept = args[9];
		String salary = args[10];
		String workLoc = args[11];

		// validate input parameters
		try {

			// gender should be F or M
			if (gender.equalsIgnoreCase("F") || gender.equalsIgnoreCase("M")) {
				gender = gender.toUpperCase();
			} else {
				System.out.println("Gender must equal F or M: " + gender);
				return -1;
			}

			// validate date of birth
			try {
				SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
				format.parse(dob);
				// TODO check year is say >1900
			} catch (Exception e) {
				System.out.println("Invalid Format Date of Birth: expecting dd-MMM-yyyy (ex 12-dec-1960) found "+dob);
				return -1;
			}

			// The following are enforced by the schema but can also be checked here to give the user a better error message

			// check salary numeric and > 0
			if (Integer.parseInt(salary) <= 0) {
				System.out.println("Salary must be greater than zero");
				return -1;
			}

			// jobTitle must be Salesperson, Procurement, Accounting, Management
			if ((jobTitle = checkTitle(jobTitle)) == null) {
				return -1;
			}

			// department must be Sales, Procurement, Accounting, Warehouse, Management, Accounting
			if ((dept = checkDept(dept)) == null) {
				return -1;
			}
			
			// Work Location must be "Southpoint, Northgate, Airport Mall, Concord Mills, Jungle Jims, warehouse, HQ
			if ((workLoc = checkLoc(workLoc)) == null) {
				return -1;
			}
			
			// TODO schema has max phone length at 12 - this may noy be big enough (i.e. "(919) 123-1234" is 14)

		} catch (Exception e) {
			System.out.println("Invalid Format Parameter");
			return -1;
		}

		try {

			// Create and execute the UPDATE SQL statement
			// TODO don't require update of all attributes
			// TODO address may span multiple args
			String sql = "UPDATE Staff SET name='" +name+"', gender='"+gender+"', dob='"+dob+"', jobTitle='"+jobTitle+"', department='"+dept+"', salary='"+salary+"', phone='"+phone+"', address='"+address+"', workLocation='"+workLoc+"' WHERE id="+args[2];

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Book was inserted and the ID
			System.out.println("Updated "+ cnt + " Staff record(s) with ID " + args[2] + " in Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // updateStaff

	private static void usage() {
		System.out.println("Subcommand Required. Legal values:");
		for (StaffCmds t : StaffCmds.values()) {
			System.out.println(t.toString());
		}
	} // usage

	// check the job title passed from user is valid
	private String checkTitle(String jobTitle) {

		try {
			if (jobTitle.toUpperCase().startsWith("S")) {
				return "Salesperson";
			}

			if (jobTitle.toUpperCase().startsWith("P")) {
				return "Procurement";
			}

			if (jobTitle.toUpperCase().startsWith("W")) {
				return "Warehouse staff";
			}
			
			if (jobTitle.toUpperCase().startsWith("A")) {
				return "Accounting";
			}

			if (jobTitle.toUpperCase().startsWith("M")) {
				return "Management";
			}

		} catch (Exception e) {
		}

		System.out.println("Invalid Job Title - Valid values are: (S)alesperson, (P)rocurement, (W)arehouse staff, (A)ccounting, (M)anagement ");
		return null;
		
	} // checkTitle

	// check the job title passed from user is valid
	private String checkDept(String dept) {

		try {
			if (dept.toUpperCase().startsWith("S")) {
				return "Sales";
			}

			if (dept.toUpperCase().startsWith("P")) {
				return "Procurement";
			}

			if (dept.toUpperCase().startsWith("W")) {
				return "Warehouse";
			}
			
			if (dept.toUpperCase().startsWith("A")) {
				return "Accounting";
			}

			if (dept.toUpperCase().startsWith("M")) {
				return "Management";
			}

		} catch (Exception e) {
		}

		System.out.println("Invalid Dept - Valid values are: (S)ales, (P)rocurement, (W)arehouse, (A)ccounting, (M)anagement ");
		return null;
		
	} // checkDept

	// check the job title passed from user is valid
	private String checkLoc(String loc) {

		try {
			if (loc.toUpperCase().startsWith("S")) {
				return "Southpoint";
			}

			if (loc.toUpperCase().startsWith("N")) {
				return "Northgate";
			}

			if (loc.toUpperCase().startsWith("A")) {
				return "Airport Mall";
			}
			
			// TODO this work location is too big - schema needs updating
/*			if (loc.toUpperCase().startsWith("C")) {
				return "Concord Mills";
			}*/

			if (loc.toUpperCase().startsWith("J")) {
				return "Jungle Jims";
			}

			if (loc.toUpperCase().startsWith("W")) {
				return "warehouse";
			}
			
			if (loc.toUpperCase().startsWith("H")) {
				return "HQ";
			}
			
		} catch (Exception e) {
		}

		// TODO Concord Mills is too big for attribute System.out.println("Invalid Work Location - Valid values are: (S)outhpoint, (N)orthgate, (A)irport Mall, (C)oncord Mills, (J)ungle Jims, (w)arehouse, (H)Q ");
		System.out.println("Invalid Work Location - Valid values are: (S)outhpoint, (N)orthgate, (A)irport Mall, (J)ungle Jims, (w)arehouse, (H)Q ");

		return null;
				
	} // checkLoc
	
}
