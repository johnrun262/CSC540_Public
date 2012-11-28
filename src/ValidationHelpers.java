/*****************************************************************************************
 * 
 * ValidationHelpers.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * ValidationHelpers.java helps with validation.
 * 
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;

public class ValidationHelpers {
	/**
	 * Check the gender passed from user is valid. Throw a ValidationException if there is an error.
	 *
	 * @param gender
	 *   A gender. Must be either M or F.
	 */
	public static String checkGender(String gender) throws ValidationException {
		if (gender.equalsIgnoreCase("F") || gender.equalsIgnoreCase("M")) {
			return gender.toUpperCase();
		} else {
			throw new ValidationException("Gender must equal F or M: " + gender);
		}
	}

	/**
	 * Check the date of birth. Throw a ValidationException if there is an error.
	 *
	 * @param dob
	 *   A date of birth.  Must be a parseable date of the form dd-MMM-yyy.
	 */
	public static void checkDateOfBirth(String dob) throws ValidationException {
		// validate date of birth
		try {
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
			format.parse(dob);
			// TODO check year is say >1900
		} catch (Exception e) {
			throw new ValidationException("Invalid Format Date of Birth: expecting dd-MMM-yyyy (ex 12-dec-1960) found "+dob);
		}
	}

	/**
	 * Check the Id. Throw a ValidationException if there is an error.
	 *
	 * Must be a parseable integer greater than equal to zero and
	 * exist in the specified table.
	 *
	 * @param id
	 *   The id of the record for the table to be checked
	 */
	public static void checkId(Connection connection, String Id, String Table) throws ValidationException {
		try {
			int IdValue = Integer.parseInt(Id);
			if (IdValue <= 0) {
				throw new ValidationException(Table + " Id must be a positive integer: "+Id);
			}
		} catch (Exception e) {
			throw new ValidationException(Table + " Id must be a number: "+Id);
		}
		try {
			// ID must be in the table
			String sql = "SELECT id FROM "+Table+" Where id='"+Id+"'";

			Statement statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			if (result.next()) {
				int id = result.getInt("id");
				if (id != Integer.parseInt(Id)) {
					throw new ValidationException(Table+" Id must be in database: "+ Id);
				}
			} else {
				throw new ValidationException(Table+" Id must be in database: "+ Id);
			}

			return;

		} catch (Exception e) {
			throw new ValidationException("Exception Validating "+Table+" Id: " + e.getMessage());
		}

	} // checkId

}