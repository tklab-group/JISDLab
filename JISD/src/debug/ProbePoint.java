/**
 * 
 */
package debug;

import java.util.ArrayList;

/**
 * @author sugiyama
 *
 */
public class ProbePoint extends BreakPoint {

  /**
   * Constructor
   * 
   * @param className  class name
   * @param lineNumber line number
   * @param varNames   variable names
   * @param isBreak    break or not at points
   */
  ProbePoint(String className, int lineNumber, ArrayList<String> varNames, boolean isBreak) {
    super(className, lineNumber, "", varNames, isBreak);
  }

  /**
   * Constructor
   * 
   * @param className  class name
   * @param methodName method name
   * @param varNames   variable names
   * @param isBreak    break or not at points
   */
  ProbePoint(String className, String methodName, ArrayList<String> varNames, boolean isBreak) {
    super(className, 0, methodName, varNames, isBreak);
  }

}
