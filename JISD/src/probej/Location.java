/**
 * 
 */
package probej;

/**
 * @author sugiyama
 *
 */
public class Location {
  String className;
  String methodName;
  int lineNumber;
  String varName;
  Location(String className, String methodName, int lineNumber, String varName) {
    this.className = className;
    this.methodName = methodName;
    this.lineNumber = lineNumber;
    this.varName = varName;
  }
  /**
   * @return the className
   */
  public String getClassName() {
    return className;
  }
  /**
   * @return the methodName
   */
  public String getMethodName() {
    return methodName;
  }
  /**
   * @return the lineNumber
   */
  public int getLineNumber() {
    return lineNumber;
  }
  /**
   * @return the varName
   */
  public String getVarName() {
    return varName;
  }
}
