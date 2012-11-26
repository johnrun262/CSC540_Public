/*****************************************************************************************
 * 
 * Param.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Param.java action method parameter.
 * 
 */
 
import  java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)  @interface Param {
   String value();
   boolean optional() default false;
}
