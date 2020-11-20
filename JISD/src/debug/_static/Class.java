package debug._static;

import java.util.ArrayList;

public class Class extends Static {
  public Class(String srcDir, String className) {
    super(srcDir, className, className);
  }

  public Field field(String name) {
    return new Field(srcDir, className, name);
  }

  public Method method(String name) {
    return new Method(srcDir, className, name);
  }

  public ArrayList<String> methods() {
    return StaticFile.getMethodNames(className);
  }

  public ArrayList<String> fields() {
    return StaticFile.getfieldNames(className);
  }
}
