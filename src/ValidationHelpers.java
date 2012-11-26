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
 
import java.text.SimpleDateFormat;
import java.util.Date;

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
  

 }