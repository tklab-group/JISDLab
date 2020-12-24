/** */
package jisd.debug;

import lombok.Getter;

/**
 * A point within the executing code of the target VM.
 *
 * @author sugiyama
 */
public class Location {
  @Getter String className;
  @Getter String methodName;
  @Getter int lineNumber;
  @Getter String varName;

  public Location(String className, String methodName, int lineNumber, String varName) {
    this.className = className;
    this.methodName = methodName;
    this.lineNumber = lineNumber;
    this.varName = varName;
  }
}
