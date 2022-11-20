/** */
package jisd.debug;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * A point within the executing code of the target VM.
 *
 * @author sugiyama
 */
public class Location {
  @Getter String className;
  @Getter String methodName;
  @Getter
  List<String> parameters = new ArrayList<>();
  @Getter int lineNumber;
  @Getter String varName;

  public Location(String className, String methodName, int lineNumber, String varName) {
    this.className = className;
    this.methodName = methodName;
    this.lineNumber = lineNumber;
    this.varName = varName;
  }
  public Location(String className, String methodName, List<String> parameters, int lineNumber, String varName) {
    this.className = className;
    this.methodName = methodName;
    this.parameters = parameters;
    this.lineNumber = lineNumber;
    this.varName = varName;
  }

  public String getMethodFullName() {
    var parasOpt = parameters.stream().reduce((a, b)->a+","+b);
    var paras = (parasOpt.isPresent()) ? parasOpt.get() : "";
    var str = className + "." + methodName + "("+ paras +")";
    return str;
  }
}
