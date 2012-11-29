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
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;





public class Customer extends AbstractCommandHandler {

	/*
	 * Contruct a handler for Customer objects.
	 */
	public Customer(Connection connection) { 
		super(connection);
	}

	/**
	 * Execute the command to create a customer.
	 * 
	 * @param name
	 *   The customer name.
	 * @param phone
	 *   The customer phone. May be null.
	 * @param address
	 *   The customer address. May be null.
	 * @param dob
	 *   The customer date of birth. May be null.
	 * @param gender
	 *   The customer gender. May be null.
	 * @param ssn
	 *   The customer social security number. May be null.
	 */
	public void execAdd(
			@Param("name") String name, 
			@Param("address") String address,
			@Param(value="phone", optional=true) String phone, 
			@Param(value="dob", optional=true) String dob, 
			@Param(value="gender", optional=true) String gender, 
			@Param(value="ssn", optional=true) String ssn) throws SQLException {

		// pseudo optional params (not nullable in database)
		if (ssn == null) ssn = "999-99-9999";

		// validate input parameters
		try {
			if (gender != null) gender = ValidationHelpers.checkGender(gender);
			if (dob != null) ValidationHelpers.checkDateOfBirth(dob);

			// TODO schema has max phone length at 12 - this may noy be big enough (i.e. "(919) 123-1234" is 14)

		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", name);
		params.put("phone", phone);
		params.put("address", address);
		params.put("dob", dob);
		params.put("gender", gender);
		params.put("ssn", ssn);
		params.put("status", "active");

		int newID = insertRow(ValidationHelpers.TABLE_CUSTOMER, "id", 2001, params);

		System.out.println("Inserted Customer with ID " + newID + " into Database"); 

	}

	/**
	 * List all customers in the system, ordered by the customer id.
	 */
	public void execAll() throws SQLException {

		// Select all rows in the Customer table and sort by ID
		String sql = "SELECT * FROM " + ValidationHelpers.TABLE_CUSTOMER + " ORDER BY id";

		Statement statement = createStatement();
		int cnt = displayCustomers(statement.executeQuery(sql));

		System.out.println(cnt+" Row(s) Returned");

	}

	/**
	 * Delete the specified customer
	 *
	 * @param id
	 *   The customer id. Must be convertable to an integer.
	 */
	public void execDelete(@Param("customer id") String id) throws SQLException {

		// Validate the ID
		try {
			ValidationHelpers.checkId(connection, id, ValidationHelpers.TABLE_CUSTOMER);
			ValidationHelpers.checkIdNotForeign(connection, id, "Orders", "customerId");
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		int count = deleteRow(ValidationHelpers.TABLE_CUSTOMER, Integer.parseInt(id));

		System.out.println("Deleted "+ count + " Customer with ID " + id + " from Database"); 

	}

	/**
	 * Display the properties of a specific customer.
	 * This routine checks if the id is a valid customer ID in the database. If it
	 * is then the routine displays all customers with ID. If it is not then the
	 * routine attempts to match based on phone number.
	 *
	 * @param id
	 *   The customer id. Must be convertable to an integer.
	 */
	public void execList(@Param("customer id or partial phone number") String id) throws SQLException {

		boolean matchPhone = false;
		String exMsg = "";
		String sql;

		// Validate the ID - if ID validation fails try to match on phone
		try {
			ValidationHelpers.checkId(connection, id, ValidationHelpers.TABLE_CUSTOMER);
		} catch (ValidationException ex) {
			// try to match on phone
			matchPhone = true;
			// save the error message in case no phone matches
			exMsg = ex.getMessage();
		}

		if (matchPhone) {
			sql = "SELECT * FROM " + ValidationHelpers.TABLE_CUSTOMER + " WHERE phone LIKE '%"+id+"%' ORDER BY name";
		} else {
			// Select row in the Customer table with ID
			sql = "SELECT * FROM " + ValidationHelpers.TABLE_CUSTOMER + " WHERE id = "+ Integer.parseInt(id);
		}

		Statement statement = createStatement();
		int cnt = displayCustomers(statement.executeQuery(sql));

		if (matchPhone) {
			// no records returned so display previous error message
			if (cnt == 0) { 
				System.out.println("Validation Error: " + exMsg);
				return;	
			} 
			System.out.println("Customer ID not valid. Attempted to Match on Phone Number");
		}

		System.out.println(cnt+" Row(s) Returned");

	}

	/**
	 * Update a Customer with ID with the given values
	 *
	 * @param id
	 *   The custmer id. Must be convertable to an integer.
	 * @param name
	 *   The customer name.
	 * @param phone
	 *   The customer phone. May be null.
	 * @param address
	 *   The customer address. May be null.
	 * @param dob
	 *   The customer date of birth. May be null.
	 * @param gender
	 *   The customer gender. May be null.
	 * @param ssn
	 *   The customer social security number. May be null.
	 */
	public void execUpdate(
			@Param("customer id") String id, 
			@Param("name") String name, 
			@Param("address") String address,
			@Param(value="phone", optional=true) String phone, 
			@Param(value="dob", optional=true) String dob, 
			@Param(value="gender", optional=true) String gender,
			@Param(value="status", optional=true) String status, 
			@Param(value="ssn", optional=true) String ssn) throws SQLException {

		// pseudo optional params (not nullable in database)
		if (ssn == null) ssn = "999-99-9999";

		// TODO Gender, DOB, Phone, SSN, ADDRESS are all optional

		// validate input parameters
		try {
			ValidationHelpers.checkId(connection, id, ValidationHelpers.TABLE_CUSTOMER);

			if (gender != null) gender = ValidationHelpers.checkGender(gender);
			if (dob != null) ValidationHelpers.checkDateOfBirth(dob);
			status = checkStatus(status);
			// TODO schema has max phone length at 12 - this may noy be big enough (i.e. "(919) 123-1234" is 14)

		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			System.exit(-1);
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", name);
		params.put("address", address);

		// don't set optional parameters if not supplied
		if (phone != null)	params.put("phone", phone);
		if (dob != null)	params.put("dob", dob);
		if (gender != null)	params.put("gender", gender);
		if (ssn != null)	params.put("ssn", ssn);
		if (status != null) params.put("status", status);

		// do the update
		updateRow(ValidationHelpers.TABLE_CUSTOMER, "id", Integer.parseInt(id), params);

		System.out.println("Updated Customer with ID " + id + " in Database"); 

	}

	private static final String ACTIVE = "active";
	private static final String INACTIVE = "inactive";
	/*
	 * Check the status value is valid
	 */
	private String checkStatus(String status) throws ValidationException {

		if (status != null) {
			status = status.toLowerCase();

			if (!status.equals(ACTIVE) && !status.equals(INACTIVE)) {
				throw new ValidationException("Status must be "+ACTIVE+" or "+INACTIVE);
			}
		}

		return status;
	}

	/**
	 * Display the customers from the result set and return the total count.
	 */
	private int displayCustomers(ResultSet result) throws SQLException {

		int cnt = 0;
		// loop through the result set printing attributes
		while (result.next()) {
			cnt++;
			int id = result.getInt("id");
			String name = result.getString("name");
			String phone = result.getString("phone");
			String address = result.getString("address");
			String gender = result.getString("gender");
			Date dob = result.getDate("dob");
			String ssn = result.getString("ssn");
			String status = result.getString("status");
			System.out.println(cnt+"\tID: "+id+"\tName: "+name+"\tPhone: "+nullSafe(phone)+"\tAddress: "+nullSafe(address)+"\tDOB: "+nullSafe(dob)+"\tGender: "+nullSafe(gender)+"\tSSN: "+nullSafe(ssn)+"\tStatus: "+status);
			System.out.println();
		}
		return cnt;

	}

}
