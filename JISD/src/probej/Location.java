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
}
