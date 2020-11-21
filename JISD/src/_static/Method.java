package _static;

import java.util.ArrayList;

public class Method extends Static {
  public Method(String srcDir, String className, String methodName) {
    super(srcDir, className, methodName);
  }

  public Local local(String localName) {
    return new Local(srcDir, className, name, localName);
  }

  public ArrayList<String> locals() {
    return StaticFile.getLocalNames(className);
  }
}
