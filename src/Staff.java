
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
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;




public class Staff extends AbstractCommandHandler {

  public static String TABLE = "Staff";
  
  /*
   * Contruct a handler for staff objects.
   */
	public Staff(Connection connection) { 
    super(connection);
  }
  
  /**
	 * Execute the command to create a staff record.
	 * 
   * @param name
   *   The staff's name
   * @param phone
   *   The staff's phone
   * @param address
   *   The staff's address
   * @param dob
   *   The staff's date of birth
   * @param gender
   *   The staff's gender. Either "M" or "F".
   * @param jobTitle
   *   The staff's job title
   * @param dept
   *   The staff's work department
   * @param salary
   *   The staff's salary. Must be convertable to a double.
   * @param workLoc
   *   The staff's work location
	 */
	public void execAdd(
    @Param("name") String name, 
    @Param("phone") String phone, 
    @Param("address") String address,
    @Param("dob") String dob,
    @Param("gender") String gender,
    @Param("job title") String jobTitle,
    @Param("department") String dept,
    @Param("salary") String salary,
    @Param("work location") String workLoc) throws ValidationException, SQLException {

    Double salaryValue = null;
		// validate input parameters
    try {
      gender = ValidationHelpers.checkGender(gender);
      salaryValue = checkSalary(salary);
      ValidationHelpers.checkDateOfBirth(dob);
      // The following are enforced by the schema but can also be checked here to give the user a better error message
      jobTitle = checkTitle(jobTitle);
      dept = checkDept(dept);
      workLoc = checkLoc(workLoc);
    } catch (ValidationException ex) {
      System.out.println("Validation Error: " + ex.getMessage());
      System.exit(-1);
    }
			
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("name", name);
    params.put("gender", gender);
    params.put("dob", dob);
    params.put("jobTitle", jobTitle);
    params.put("department", dept);
    params.put("salary", salaryValue);
    params.put("phone", phone);
    params.put("address", address);
    params.put("workLocation", workLoc);
    
    int newID = insertRow(TABLE, "id", 1001, params);

    System.out.println("Inserted Staff with ID " + newID + " into Database"); 
	}

  /**
	 * List all staff in the system, ordered by the staff id.
	 */
	public void execAll() throws SQLException {
  
    // Select all rows in the staff table and sort by ID
    String sql = "SELECT * FROM " + TABLE + " ORDER BY id";

    Statement statement = createStatement();
    int cnt = displayStaff(statement.executeQuery(sql));

    System.out.println(cnt+" Row(s) Returned");
    
	}

  /**
	 * Delete the specified staff
   *
   * @param id
   *   The staff id. Must be convertable to an integer.
	 */
	public void execDelete(@Param("staff id") String id) throws SQLException {

    int count = deleteRow(TABLE, Integer.parseInt(id));

    System.out.println("Deleted "+ count + " Staff with ID " + id + " from Database"); 
    
	}

  /**
	 * Display the properties of a specific staff.
   *
   * @param id
   *   The staff id. Must be convertable to an integer.
	 */
	public void execList(@Param("staff id") String id) throws SQLException {
		
    // Select row in the Staff table with ID
    String sql = "SELECT * FROM " + TABLE + " WHERE id = "+ Integer.parseInt(id);

    Statement statement = createStatement();
    int cnt = displayStaff(statement.executeQuery(sql));

    System.out.println(cnt+" Row(s) Returned");

	}
  
	/**
	 * Update a Staff with ID with the given values
	 *
   * @param id
   *   The staff id. Must be convertable to an integer.
   * @param name
   *   The staff's name
   * @param phone
   *   The staff's phone
   * @param address
   *   The staff's address
   * @param dob
   *   The staff's date of birth
   * @param gender
   *   The staff's gender. Either "M" or "F".
   * @param jobTitle
   *   The staff's job title
   * @param dept
   *   The staff's work department
   * @param salary
   *   The staff's salary. Must be convertable to a double.
   * @param workLoc
   *   The staff's work location
	 */
	public void execUpdate(
    @Param("staff id") String id, 
    @Param("name") String name, 
    @Param("phone") String phone, 
    @Param("address") String address,
    @Param("dob") String dob,
    @Param("gender") String gender,
    @Param("job title") String jobTitle,
    @Param("department") String dept,
    @Param("salary") String salary,
    @Param("work location") String workLoc) throws ValidationException, SQLException {

    Double salaryValue = null;
		// validate input parameters
    try {
      gender = ValidationHelpers.checkGender(gender);
      salaryValue = checkSalary(salary);
      ValidationHelpers.checkDateOfBirth(dob);
      // The following are enforced by the schema but can also be checked here to give the user a better error message
      jobTitle = checkTitle(jobTitle);
      dept = checkDept(dept);
      workLoc = checkLoc(workLoc);
    } catch (ValidationException ex) {
      System.out.println("Validation Error: " + ex.getMessage());
      System.exit(-1);
    }
    
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("name", name);
    params.put("gender", gender);
    params.put("dob", dob);
    params.put("jobTitle", jobTitle);
    params.put("department", dept);
    params.put("salary", salaryValue);
    params.put("phone", phone);
    params.put("address", address);
    params.put("workLocation", workLoc);

    updateRow(TABLE, "id", Integer.parseInt(id), params);

    System.out.println("Updated Staff with ID " + id + " in Database"); 

	}

  /**
   * Check the salary. Throw a ValidationException if there is an error.
   *
   * Must be a parseable double greater than zero.
   *
   * @param salary
   *   The staff's salary.
   */
  private double checkSalary(String salary) throws ValidationException {
    try {
      double salaryValue = Double.parseDouble(salary);
      if (salaryValue <= 0) {
        throw new ValidationException("Salary must be greater than zero");
      }
      return salaryValue;
    } catch (Exception e) {
      throw new ValidationException("Salary must be a number");
    }
  }
  
  /**
   * Check the gender passed from user is valid. Throw a ValidationException if there is an error.
   *
   * Must be one of: M or F.
   *
   * @param gender
   *   The staff's gender
   */
  private String checkGender(String gender) throws ValidationException {
    if (gender.equalsIgnoreCase("F") || gender.equalsIgnoreCase("M")) {
      return gender.toUpperCase();
    } else {
      throw new ValidationException("Gender must equal F or M: " + gender);
    }
  }
  
	/**
   * Check the job title passed from user is valid. Throw a ValidationException if there is an error.
   *
   * Must be one of: Salesperson, Procurement, Accounting, Management
   *
   * @param jobTitle
   *   The staff's job title.
   */
	private String checkTitle(String jobTitle) throws ValidationException {

    Map<String, String> valid = new TreeMap<String, String>();
    valid.put("S", "Salesperson");
    valid.put("P", "Procurement");
    valid.put("W", "Warehouse staff");
    valid.put("A", "Accounting");
    valid.put("M", "Management");
    
    return validateCode(jobTitle, "Job Title", valid);
    
	}

	/**
   * Check the department passed from user is valid. Throw a ValidationException if there is an error.
   *
   * Must be one of: Sales, Procurement, Accounting, Warehouse, Management, Accounting
   *
   * @param dept
   *   The staff's work department
   */
	private String checkDept(String dept) throws ValidationException {

    Map<String, String> valid = new TreeMap<String, String>();
    valid.put("S", "Sales");
    valid.put("P", "Procurement");
    valid.put("W", "Warehouse");
    valid.put("A", "Accounting");
    valid.put("M", "Management");
    
    return validateCode(dept, "Department", valid);
		
	} // checkDept

	/**
   * Check the work location passed from user is valid. Throw a ValidationException if there is an error.
   *
   * Must be one of: Southpoint, Northgate, Airport Mall, Concord Mills, Jungle Jims, warehouse, HQ
   *
   * @param loc
   *   The staff's work location
   */
	private String checkLoc(String loc) throws ValidationException {

    Map<String, String> valid = new TreeMap<String, String>();
    valid.put("S", "Southpoint");
    valid.put("N", "Northgate");
    valid.put("A", "Airport Mall");
    // TODO this work location is too big - schema needs updating
    //valid.put("C", "Concord Mills");
    valid.put("J", "Jungle Jims");
    valid.put("W", "warehouse");
    valid.put("H", "HQ");
    
    return validateCode(loc, "Work Location", valid);
				
	} // checkLoc
	
  /**
   * Display the staff from the result set and return the total count.
   */
  private int displayStaff(ResultSet result) throws SQLException {
  
    int cnt = 0;
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
    return cnt;
    
  }
}
