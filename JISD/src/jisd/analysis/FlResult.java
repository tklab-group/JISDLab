package jisd.analysis;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class FlResult {
  @Getter String className;
  @Getter int line = 0;
  @Getter @Setter double score;
  @Getter @Setter String methodName = "";
  @Getter @Setter(AccessLevel.PACKAGE) int rank;

  public FlResult(String className, String methodName, int lineNumber, double score) {
    this.className = className;
    this.methodName = methodName;
    this.line = lineNumber;
    this.score = score;
  }
}
