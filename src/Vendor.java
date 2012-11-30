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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;



public class Vendor extends AbstractCommandHandler {

	private static String TABLE = "Vendor";

	/*
	 * Contruct a handler for vendor objects.
	 */
	public Vendor(Connection connection) { 
		super(connection);
	}

	/**
	 * Execute the command to create a vendor.
	 * 
	 * @param name
	 *   The vendor name
	 * @param phone
	 *   The vendor phone
	 * @param address
	 *   The vendor address.
	 */
	public void execAdd(
			@Param("name") String name, 
			@Param("phone") String phone, 
			@Param("address") String address) throws SQLException {

		// Automatically set start date today and end date to one year from now
		String sqlStartDate;
		String sqlEndDate;
		try {			
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
			// begin date
			Calendar now = new GregorianCalendar();
			sqlStartDate = format.format(now.getTime());
			// end date
			Calendar yearFromNow = new GregorianCalendar();
			yearFromNow.set(Calendar.YEAR, now.get(Calendar.YEAR)+1);
			sqlEndDate = format.format(yearFromNow.getTime());
		} catch (Exception e){
			System.out.println("Problem setting date of contract expiration: " + e.getMessage());
			return;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", name);
		params.put("phone", phone);
		params.put("address", address);
		params.put("startDate", sqlStartDate);
		params.put("endDate", sqlEndDate);

		int newID = insertRow(TABLE, "id", 3001, params);

		System.out.println("Inserted Vendor with ID " + newID + " into Database"); 

	}

	/**
	 * List all vendors in the system, ordered by the vendor id.
	 */
	public void execAll() throws SQLException {

		// Select all rows in the vendor table and sort by ID
		String sql = "SELECT * FROM " + TABLE + " ORDER BY id";

		Statement statement = createStatement();
		int cnt = displayVendors(statement.executeQuery(sql));

		System.out.println(cnt+" Row(s) Returned");

	}

	/**
	 * Delete the specified vendor
	 *
	 * @param id
	 *   The vendor id. Must be convertable to an integer.
	 */
	public void execDelete(@Param("vendor id") String id) throws SQLException {

		// validate Vendor ID parameter
		try {
			ValidationHelpers.checkId(connection, id, TABLE);
			ValidationHelpers.checkIdNotForeign(connection, id, "Purchase", "vendorId");
			ValidationHelpers.checkIdNotForeign(connection, id, "Stocks", "vendorId");
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		int count = deleteRow(TABLE, Integer.parseInt(id));

		System.out.println("Deleted "+ count + " Vendor with ID " + id + " from Database"); 

	}

	/**
	 * Display the properties of a specific vendor.
	 *
	 * @param id
	 *   The vendor id. Must be convertable to an integer.
	 */
	public void execList(@Param("vendor id") String id) throws SQLException {

		// validate Vendor ID parameter
		try {
			ValidationHelpers.checkId(connection, id, TABLE);
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		// Select row in the Vendor table with ID
		String sql = "SELECT * FROM " + TABLE + " WHERE id = "+ Integer.parseInt(id);

		Statement statement = createStatement();
		int cnt = displayVendors(statement.executeQuery(sql));

		System.out.println(cnt+" Row(s) Returned");

	}

	/**
	 * Update a Vendor with ID with the given values
	 *
	 * @param id
	 *   The vendor id. Must be convertable to an integer.
	 * @param name
	 *   The vendor name
	 * @param phone
	 *   The vendor phone
	 * @param address
	 *   The vendor address.
	 */
	public void execUpdate(
			@Param("vendor id") String id, 
			@Param("name") String name, 
			@Param("phone") String phone, 
			@Param("address") String address) throws SQLException {

		// validate Vendor ID parameter
		try {
			ValidationHelpers.checkId(connection, id, TABLE);
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		String sqlDate;

		// Automatically renew end date to one year from now
		try {
			Calendar now = new GregorianCalendar();
			Calendar yearFromNow = new GregorianCalendar();
			yearFromNow.set(Calendar.YEAR, now.get(Calendar.YEAR)+1);
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
			sqlDate = format.format(yearFromNow.getTime());
		} catch (Exception e){
			System.out.println("Problem setting date of contract expiration: " + e.getMessage());
			return;
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", name);
		params.put("phone", phone);
		params.put("address", address);
		params.put("endDate", sqlDate);

		updateRow(TABLE, "id", Integer.parseInt(id), params);

		System.out.println("Updated Vendor with ID " + id + " in Database"); 
	}

	/**
	 * Display the vendors from the result set and return the total count.
	 */
	private int displayVendors(ResultSet result) throws SQLException {

		int cnt = 0;
		// loop through the result set printing attributes
		while (result.next()) {
			cnt++;
			int id = result.getInt("id");
			String name = result.getString("name");
			String phone = result.getString("phone");
			String address = result.getString("address");
			Date startDate = result.getDate("StartDate");
			Date endDate = result.getDate("endDate");
			System.out.println(cnt+"\tID: "+id+"\tName: "+name+"\tPhone: "+phone+
					"\tAddress: "+address+"\tStart Date: "+startDate+"\tEnd Date: "+endDate);
			System.out.println();
		}
		return cnt;

	}

}
