/*****************************************************************************************
 * 
 * ReflectionCommandInvoker.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Attempt to invoke to a class via reflection
 * 
 */

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;

public class ReflectionCommandInvoker {

  private static final String ACTION_METHOD_PREFIX = "exec";
  
  private static Class[] operationClasses = new Class[] {
    Book.class,
    Staff.class,
    Vendor.class
  };
  
  private Connection connection;
  
  public ReflectionCommandInvoker(Connection connection) {
    this.connection = connection;
  }
  
  public boolean execute(String[] args) throws Exception {
    // Convert our params into respectable class and method names, we will
    // use reflection to invoke command line args on class.method.
    String handlerName = args[0].substring(0,1).toUpperCase() + args[0].substring(1);
    String actionName = "";
    if (args.length > 1) actionName = args[1].substring(0,1).toUpperCase() + args[1].substring(1);
        
    // Locate the handler class and action method via reflection
    // Step 1: Load the handler class and if found it's publicly declared action methods
    Class<?> handlerClass = null;
    List<Method> actionMethods = new ArrayList<Method>();
    try {
      handlerClass = Class.forName(handlerName);
      Method[] methods = handlerClass.getDeclaredMethods();
      for (int i = 0; i < methods.length; i++) {
        if (Modifier.isPublic(methods[i].getModifiers()) && methods[i].getName().indexOf(ACTION_METHOD_PREFIX) == 0) {
          actionMethods.add(methods[i]);
        }
      }
    } catch (Exception e) { }
    
    // If handler class wasn't found, message and exit
    if (handlerClass == null) {
      return false;
      //System.out.println("'" + handlerName + "' is not a valid bookstore command.");
      //usage();
      //System.exit(-1);
    }
    // Is it one we care about?
    String shortName = shortName(handlerClass);
    boolean careAbout = false;
    for (int i = 0; i < operationClasses.length; i++) {
      if (operationClasses[i].equals(handlerClass)) careAbout = true;
    }
    if (!careAbout) return false;
    
    // Search for a method matching action name
    String actionMethodName = ACTION_METHOD_PREFIX + actionName;
    Method actionMethod = null;
    for (int i = 0; i < actionMethods.size(); i++) {
      if (actionMethods.get(i).getName().equals(actionMethodName)) {
        actionMethod = actionMethods.get(i);
        break;
      }
    }
    
    // If action method wasn't found, display valid actions for handler and exit
    if (actionMethod == null) {
      System.out.println("'" + actionName + "' is not a valid command for '" + handlerName + "'.");
      System.out.println("Valid commands:");
      for (int i = 0; i < actionMethods.size(); i++) {
        String methodName = actionMethods.get(i).getName().substring(ACTION_METHOD_PREFIX.length());
        if (methodName.length() == 0) methodName = "<DEFAULT>";
        System.out.println("   " + methodName);
      }
      System.exit(-1);
    }
    
    // Get parameters of method and check against count of those passed.
    Class[] actionParams = actionMethod.getParameterTypes();
    if (args.length - 2 < actionParams.length) {
      System.out.println("Not enough parameters specified for action '" + handlerName + "." + actionMethodName + "()': " + actionParams.length + " are required.");
      Annotation[][] paramAnnotations = actionMethod.getParameterAnnotations();
      for (int i = 0; i < actionParams.length; i++) {
        String paramType = "{" + shortName(actionParams[i]) + "}";
        String paramName = "unnamed";
        for (int a = 0; a < paramAnnotations[i].length; a++) {
          Annotation paramAnnotation = paramAnnotations[i][a];
          if (paramAnnotation.annotationType() == Param.class) {
            paramName = Param.class.getMethod("value").invoke(paramAnnotation).toString();
            break;
          }
        }
        System.out.println("   " + paramName + " " + paramType);
      }
      System.exit(-1);
    }
    
    // Create the handler object and process command
    Constructor handlerConstructor = handlerClass.getConstructor(Connection.class);
    Object handler = handlerConstructor.newInstance(connection);
    if (handler == null) {
      System.out.println("Could not construct instance of '" + handlerName + "'.");
      System.exit(-1);
    }
    // Pack the program arguments into method arguments
    String[] params = new String[]{};
    if (args.length > 2) {
      params = new String[args.length - 2];
      for (int i = 2; i < args.length; i++) params[i-2] = args[i];
    }
    // Invoke the command!
    actionMethod.invoke(handler, (Object[])params);
      
    return true;
  }
  
  private String shortName(Class c) {
    String cName = c.getName();
    int cnloc = cName.lastIndexOf ('.') + 1; 
    return cName.substring(cnloc);  
  }
}