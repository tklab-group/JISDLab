package jisd.analysis;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class FlResult {
  @Getter String className;
  @Getter int line;
  @Getter @Setter double score;
  @Getter @Setter(AccessLevel.PACKAGE) int rank;
  public FlResult(String className, int line, double score) {
    this.className = className;
    this.line = line;
    this.score = score;
  }
}
