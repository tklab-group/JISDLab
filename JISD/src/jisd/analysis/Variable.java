package jisd.analysis;

import lombok.Getter;

public class Variable {
  @Getter String type;
  @Getter String name;
  @Getter int line;

  Variable(String type, String name, int line) {
    this.type = type;
    this.name = name;
    this.line = line;
  }
}
